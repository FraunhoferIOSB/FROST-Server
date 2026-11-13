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
package de.fraunhofer.iosb.ilt.frostserver.mcp.util;

import io.modelcontextprotocol.server.McpNotificationHandler;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Workarounds for gaps in the MCP SDK (2.1.0), all via guarded reflection so
 * the server keeps working when the SDK internals change: none of these may
 * fail the boot, and each falls back to the previous behaviour.
 *
 * <p>
 * Known SDK issue: the server registers no handler for the
 * "notifications/cancelled" client notification, so every cancelled tool call
 * logs a WARN ("No handler registered for notification method:
 * notifications/cancelled"). The handler map built by
 * McpAsyncServer.prepareNotificationHandlers is a plain HashMap that the
 * DefaultMcpStreamableServerSessionFactory shares, by reference, with every
 * session it creates. Installing a no-op handler in that map therefore covers
 * all future sessions of the transport provider.</p>
 */
public final class McpSdkWorkarounds {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpSdkWorkarounds.class);

    /**
     * The client notification sent when a call is cancelled; the SDK has no
     * constant for it and registers no default handler.
     */
    private static final String METHOD_NOTIFICATION_CANCELLED = "notifications/cancelled";

    private McpSdkWorkarounds() {
        // Utility class.
    }

    /**
     * Installs a no-op handler for the "notifications/cancelled" notification
     * in the transport provider's session factory, silencing the benign WARN
     * the SDK logs for every client-side cancellation. Call after building the
     * McpServer (the factory is wired during build()), before serving requests.
     * Best-effort: on any reflection failure the server runs unchanged, only
     * the WARN stays.
     *
     * @param provider The transport provider the McpServer was built on. May
     * not be null.
     */
    public static void installCancelledNotificationHandler(HttpServletStreamableServerTransportProvider provider) {
        try {
            final Object factory = readField(provider, "sessionFactory");
            if (factory == null) {
                LOGGER.debug("No session factory (yet); not installing the cancelled handler.");
                return;
            }
            @SuppressWarnings("unchecked")
            final Map<String, McpNotificationHandler> handlers = (Map<String, McpNotificationHandler>) readField(factory, "notificationHandlers");
            if (handlers == null) {
                LOGGER.debug("No notification handlers map found; not installing the cancelled handler.");
                return;
            }
            handlers.putIfAbsent(METHOD_NOTIFICATION_CANCELLED, (exchange, params) -> {
                LOGGER.info("Client cancelled a running tool call; the tool result will be discarded.");
                return Mono.empty();
            });
            LOGGER.debug("Installed no-op handler for {}.", METHOD_NOTIFICATION_CANCELLED);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            LOGGER.warn("Could not install the notifications/cancelled handler (SDK internals changed?): {}", ex.getMessage());
            LOGGER.trace("Failure details:", ex);
        }
    }

    /**
     * A predicate telling whether the transport provider currently knows an MCP
     * session id. Used to avoid pushing notifications into dead sessions; on
     * any reflection failure the predicate always says true, so notifications
     * are still attempted and the send-failure path cleans up.
     *
     * @param provider The transport provider the McpServer was built on. May
     * not be null.
     * @return a session-presence predicate, never null.
     */
    public static Predicate<String> sessionPresence(HttpServletStreamableServerTransportProvider provider) {
        try {
            @SuppressWarnings("unchecked")
            final Map<String, ?> sessions = (Map<String, ?>) readField(provider, "sessions");
            if (sessions != null) {
                return sessionId -> sessions.containsKey(sessionId);
            }
        } catch (Throwable ex) {
            LOGGER.warn("Could not access the session map (SDK internals changed?): {}", ex.getMessage());
            LOGGER.trace("Failure details:", ex);
        }
        return sessionId -> true;
    }

    private static Object readField(Object target, String name) throws ReflectiveOperationException {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                final Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ex) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name + " not found on " + target.getClass().getName());
    }

}
