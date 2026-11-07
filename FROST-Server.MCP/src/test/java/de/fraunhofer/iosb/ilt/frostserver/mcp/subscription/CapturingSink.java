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

import io.modelcontextprotocol.spec.McpSchema;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import reactor.core.publisher.Mono;

/**
 * A notification sink for tests: records all deliveries, optionally failing
 * for configured sessions to simulate dead sessions. The getter
 * {@link #get(int)} is stable across concurrent bus workers, since each
 * captured record maps to one completed emit.
 */
public final class CapturingSink implements NotificationSink {

    private final ConcurrentLinkedQueue<Captured> notifications = new ConcurrentLinkedQueue<>();
    private final Set<String> failingSessions;

    /**
     * Creates a sink that fails deliveries for the given sessions.
     *
     * @param failingSessions Sessions whose deliveries error, or null.
     */
    public CapturingSink(Set<String> failingSessions) {
        this.failingSessions = failingSessions == null ? Set.of() : failingSessions;
    }

    /**
     * A single captured delivery.
     *
     * @param sessionId The session id.
     * @param notification The notification.
     */
    public record Captured(String sessionId, McpSchema.LoggingMessageNotification notification) {

    }

    @Override
    public Mono<Void> deliver(String sessionId, McpSchema.LoggingMessageNotification notification) {
        if (failingSessions.contains(sessionId)) {
            return Mono.error(new IllegalStateException("session " + sessionId + " not connected"));
        }
        notifications.add(new Captured(sessionId, notification));
        return Mono.empty();
    }

    /**
     * The captured notifications, oldest first.
     *
     * @return the queue.
     */
    public ConcurrentLinkedQueue<Captured> getNotifications() {
        return notifications;
    }

    /**
     * The number of captured notifications.
     *
     * @return the size.
     */
    public int size() {
        return notifications.size();
    }

    /**
     * Clears captured notifications.
     */
    public void clear() {
        notifications.clear();
    }

}
