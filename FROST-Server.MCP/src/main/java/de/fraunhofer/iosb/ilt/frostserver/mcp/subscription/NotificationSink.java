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
import reactor.core.publisher.Mono;

/**
 * The delivery target for subscription notifications. In production this is a
 * thin adapter over the transport provider's notifyClient; tests capture the
 * notifications instead. Deliveries are Monos: the caller triggers them
 * without blocking the message-bus worker thread, and treats errors as a
 * signal to drop the subscriptions of the failing session.
 */
public interface NotificationSink {

    /**
     * Deliver a logging notification to a session.
     *
     * @param sessionId The MCP session to deliver to.
     * @param notification The notification to deliver.
     * @return a Mono completing when the transport accepted the notification
     * for sending; erroring when delivery is not possible (e.g. the session
     * is gone).
     */
    public Mono<Void> deliver(String sessionId, McpSchema.LoggingMessageNotification notification);

}
