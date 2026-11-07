/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.mcp.subscription;

import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageListener;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.Subscription;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.McpSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Maps FROST message-bus EntityChangedMessages to MCP logging
 * notifications, per session. Subscriptions are created by the
 * frost_subscribe tool and delivered as MCP "notifications/message"
 * notifications with logger "frost": the notification data is a JSON
 * envelope carrying the subscription id, the event type, the entity type
 * and the formatted entity.
 *
 * <p>Concurrency: the registry is a MessageBus listener (worker threads) and
 * receives subscribe/unsubscribe calls from HTTP threads. The registration
 * mutations are synchronized on the registry; the event path uses
 * concurrency-friendly collections and one shared persistence manager per
 * event.</p>
 */
public class FrostSubscriptionRegistry implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrostSubscriptionRegistry.class.getName());

    /**
     * The logger name clients see on subscription notifications.
     */
    public static final String LOGGER_NAME = "frost";

    private final CoreSettings coreSettings;
    private final McpSettings mcpSettings;
    private final NotificationSink sink;
    private final Predicate<String> sessionPresence;
    private final FrostSubscriptionFactory factory;

    private final Map<EntityType, List<Registration>> byType = new ConcurrentHashMap<>();
    private final Map<String, List<Registration>> bySession = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong();
    private final AtomicBoolean listening = new AtomicBoolean(false);

    /**
     * A single subscription registration.
     *
     * @param id The server-assigned subscription id.
     * @param sessionId The MCP session that subscribed.
     * @param subscription The subscription.
     */
    private record Registration(long id, String sessionId, Subscription subscription) {

        @Override
        public String toString() {
            return sessionId + ":" + id + " on " + subscription;
        }

    }

    /**
     * Creates a new registry.
     *
     * @param coreSettings The server settings.
     * @param sink The delivery target for notifications.
     * @param sessionPresence Tells whether a session id is currently served;
     * used to drop subscriptions of gone sessions.
     */
    public FrostSubscriptionRegistry(CoreSettings coreSettings, NotificationSink sink, Predicate<String> sessionPresence) {
        this.coreSettings = coreSettings;
        this.mcpSettings = coreSettings.getMcpSettings();
        this.sink = sink;
        this.sessionPresence = sessionPresence;
        this.factory = new FrostSubscriptionFactory(coreSettings);
    }

    /**
     * Subscribes a session. Parse and gate failures surface as
     * IllegalArgumentException for the tool layer to map to MCP -32602; cap
     * and visibility failures as FrostSubscriptionException.
     *
     * @param sessionId The MCP session id. May not be null.
     * @param principal The principal of the session.
     * @param version The resolved STA version. May not be null.
     * @param staPath The STA path, relative to the version root.
     * @param filter The raw $filter expression, without prefix. May be null.
     * @param select The $select items. May be null or empty.
     * @return the subscription result.
     * @throws FrostSubscriptionException on cap or visibility failures.
     */
    public synchronized FrostSubscriptionResult subscribe(
            String sessionId, PrincipalExtended principal, Version version,
            String staPath, String filter, List<String> select) throws FrostSubscriptionException {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(version, "version");
        principal = PrincipalExtended.fromPrincipal(principal);
        final Subscription subscription = factory.create(principal, version, staPath, filter, select);

        final Registration existing = findRegistration(sessionId, subscription);
        if (existing != null) {
            return new FrostSubscriptionResult(existing.id(), existing.subscription().getTopic(), true, sessionPresence.test(sessionId));
        }

        final int cap = mcpSettings.getMaxSubscriptionsPerSession();
        if (countForSession(sessionId) >= cap) {
            throw new FrostSubscriptionException(429, "Subscription limit reached: at most " + cap + " active subscriptions per session (mcp.maxSubscriptionsPerSession).");
        }
        if (subscription instanceof FrostEntitySubscription entitySubscription) {
            requireEntityVisible(sessionId, principal, entitySubscription);
        }
        final long newId = idCounter.incrementAndGet();
        addRegistration(new Registration(newId, sessionId, subscription));
        startListening();
        return new FrostSubscriptionResult(newId, subscription.getTopic(), false, sessionPresence.test(sessionId));
    }

    /**
     * Removes subscriptions of a session. With a null id all subscriptions
     * of the session are removed.
     *
     * @param sessionId The MCP session id.
     * @param subscriptionId The subscription id, or null for all.
     * @return the number of registrations removed.
     */
    public synchronized int unsubscribe(String sessionId, Long subscriptionId) {
        Objects.requireNonNull(sessionId, "sessionId");
        final List<Registration> sessionRegs = bySession.get(sessionId);
        if (sessionRegs == null) {
            return 0;
        }
        if (subscriptionId == null) {
            int count = 0;
            for (Registration reg : List.copyOf(sessionRegs)) {
                if (removeRegistration(reg)) {
                    count++;
                }
            }
            return count;
        }
        for (Registration reg : List.copyOf(sessionRegs)) {
            if (reg.id() == subscriptionId) {
                return removeRegistration(reg) ? 1 : 0;
            }
        }
        return 0;
    }

    /**
     * The ids of the subscriptions of a session (for tests and the
     * unsubscribe result).
     *
     * @param sessionId The MCP session id.
     * @return an unmodifiable copy of the ids, never null.
     */
    public synchronized List<Long> subscriptionIds(String sessionId) {
        final List<Registration> sessionRegs = bySession.get(sessionId);
        if (sessionRegs == null) {
            return List.of();
        }
        return sessionRegs.stream().map(Registration::id).toList();
    }

    /**
     * The number of live registrations (for tests and status pages).
     *
     * @return the total registration count.
     */
    public synchronized int registrationCount() {
        return bySession.values().stream().mapToInt(List::size).sum();
    }

    @Override
    public void messageReceived(EntityChangedMessage message) {
        final EntityType entityType;
        final Entity entity;
        try {
            entityType = message.getEntityType();
            entity = message.getEntity();
            if (entityType == null || entity == null) {
                return;
            }
        } catch (RuntimeException ex) {
            LOGGER.trace("Malformed change message: {}", ex.getMessage());
            return;
        }
        final List<Registration> registrations = byType.get(entityType);
        if (registrations == null || registrations.isEmpty()) {
            return;
        }
        final Set<Property> changedFields = message.getFields();
        try (PersistenceManager persistenceManager = PersistenceManagerFactory.getInstance(coreSettings).create()) {
            Version lastVersion = null;
            for (Registration reg : registrations) {
                final Subscription subscription = reg.subscription();
                try {
                    if (!subscription.matches(persistenceManager, entity, changedFields)) {
                        continue;
                    }
                    if (lastVersion != subscription.getVersion()) {
                        entity.setSelfLink(null);
                        lastVersion = subscription.getVersion();
                    }
                    final Entity newEntity = subscription.fetchExpand(persistenceManager, entity);
                    final String envelope = buildEnvelope(reg.id(), message, subscription, newEntity);
                    if (envelope == null) {
                        continue;
                    }
                    deliverTo(reg, envelope);
                } catch (RuntimeException ex) {
                    LOGGER.warn("Failed to handle subscription {}: {}", reg, ex.getMessage());
                    LOGGER.trace("Failure details:", ex);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error handling change message for {}.", entityType, ex);
        }
    }

    private String buildEnvelope(long subscriptionId, EntityChangedMessage message, Subscription subscription, Entity entity) {
        final String formatted;
        try {
            formatted = subscription.formatMessage(entity);
        } catch (IOException ex) {
            LOGGER.warn("Failed to format subscription message for subscription {}: {}", subscriptionId, ex.getMessage());
            return null;
        }
        final boolean tooLarge = formatted != null
                && formatted.getBytes(StandardCharsets.UTF_8).length > mcpSettings.getMaxResultSize();
        final Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("subscriptionId", subscriptionId);
        envelope.put("eventType", message.getEventType().label);
        envelope.put("entityType", subscription.getEntityType().plural);
        envelope.put("truncated", tooLarge);
        if (!tooLarge) {
            envelope.put("entity", StaJsonHelper.parseOrNull(formatted));
        }
        return StaJsonHelper.writeValueAsString(envelope);
    }

    private void deliverTo(Registration reg, String envelope) {
        if (!sessionPresence.test(reg.sessionId())) {
            LOGGER.debug("Session {} is gone; dropping its registrations.", reg.sessionId());
            removeSession(reg.sessionId());
            return;
        }
        final McpSchema.LoggingMessageNotification notification = McpSchema.LoggingMessageNotification.builder()
                .level(McpSchema.LoggingLevel.INFO)
                .logger(LOGGER_NAME)
                .data(envelope)
                .build();
        final Mono<Void> delivery = sink.deliver(reg.sessionId(), notification);
        if (delivery == null) {
            return;
        }
        delivery.subscribe(
                ok -> {},
                err -> handleSendFailure(reg.sessionId(), err));
    }

    private void handleSendFailure(String sessionId, Throwable error) {
        LOGGER.info("Delivering subscription notification to session {} failed: {}", sessionId, error.getMessage());
        removeSession(sessionId);
    }

    private void requireEntityVisible(String sessionId, PrincipalExtended principal, FrostEntitySubscription subscription) throws FrostSubscriptionException {
        final PathElementEntity lastElement = (PathElementEntity) subscription.getPath().getLastElement();
        final Object result;
        try (PersistenceManager persistenceManager = PersistenceManagerFactory.getInstance(coreSettings).create()) {
            PrincipalExtended oldLocalPrincipal = PrincipalExtended.getLocalPrincipal();
            try {
                PrincipalExtended.setLocalPrincipal(principal);
                result = persistenceManager.get(subscription.getEntityType(), lastElement.getPkValues());
            } finally {
                PrincipalExtended.setLocalPrincipal(oldLocalPrincipal);
            }
        } catch (RuntimeException ex) {
            LOGGER.warn("Visibility check for subscription to {} failed: {}", subscription.getTopic(), ex.getMessage());
            throw new FrostSubscriptionException(404, "Entity '" + subscription.getTopic() + "' not found or not readable.");
        }
        if (result == null) {
            final int code = isAnonymous(principal) ? 404 : 403;
            throw new FrostSubscriptionException(code, "Entity '" + subscription.getTopic() + "' not found or not readable.");
        }
    }

    private static boolean isAnonymous(PrincipalExtended principal) {
        return principal == PrincipalExtended.ANONYMOUS_PRINCIPAL
                || principal.getName() == null
                || principal.getName().isEmpty()
                || PrincipalExtended.USER_NAME_ANONYMOUS.equals(principal.getName());
    }

    private int countForSession(String sessionId) {
        final List<Registration> sessionRegs = bySession.get(sessionId);
        return sessionRegs == null ? 0 : sessionRegs.size();
    }

    private Registration findRegistration(String sessionId, Subscription subscription) {
        final List<Registration> sessionRegs = bySession.get(sessionId);
        if (sessionRegs != null) {
            for (Registration reg : sessionRegs) {
                if (reg.subscription().equals(subscription)) {
                    return reg;
                }
            }
        }
        return null;
    }

    private void addRegistration(Registration reg) {
        byType.computeIfAbsent(reg.subscription().getEntityType(), t -> new CopyOnWriteArrayList<>()).add(reg);
        bySession.computeIfAbsent(reg.sessionId(), s -> new CopyOnWriteArrayList<>()).add(reg);
        LOGGER.debug("Added subscription registration {}.", reg);
    }

    private boolean removeRegistration(Registration reg) {
        boolean removed = false;
        final List<Registration> typeList = byType.get(reg.subscription().getEntityType());
        if (typeList != null && typeList.remove(reg)) {
            removed = true;
            if (typeList.isEmpty()) {
                byType.remove(reg.subscription().getEntityType(), typeList);
            }
        }
        final List<Registration> sessionList = bySession.get(reg.sessionId());
        if (sessionList != null && sessionList.remove(reg)) {
            removed = true;
            if (sessionList.isEmpty()) {
                bySession.remove(reg.sessionId(), sessionList);
            }
        }
        if (removed) {
            LOGGER.debug("Removed subscription registration {}.", reg);
        }
        return removed;
    }

    private synchronized void removeSession(String sessionId) {
        final List<Registration> sessionRegs = bySession.remove(sessionId);
        if (sessionRegs == null) {
            return;
        }
        for (Registration reg : sessionRegs) {
            removeFromType(reg);
        }
    }

    private void removeFromType(Registration reg) {
        final List<Registration> typeList = byType.get(reg.subscription().getEntityType());
        if (typeList != null) {
            typeList.remove(reg);
            if (typeList.isEmpty()) {
                byType.remove(reg.subscription().getEntityType(), typeList);
            }
        }
    }

    private void startListening() {
        if (listening.compareAndSet(false, true)) {
            coreSettings.getMessageBus().addMessageListener(this);
        }
    }

    /**
     * Stops listening and clears all registrations. Call before shutting
     * down the bus or transport.
     */
    public synchronized void shutdown() {
        if (listening.compareAndSet(true, false)) {
            coreSettings.getMessageBus().removeMessageListener(this);
        }
        byType.clear();
        bySession.clear();
    }

}
