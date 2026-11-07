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

/**
 * The outcome of a subscribe call: the subscription id, the canonical topic,
 * and the flags of the tool result. The tool layer renders this as structured
 * content; parse/gate failures travel as McpError (-32602) instead.
 *
 * @param subscriptionId The server-assigned subscription id.
 * @param topic The canonical topic: version path plus the validated query
 * string, or null when the query carries no options.
 * @param alreadyExisted True when an identical subscription already existed
 * for the session; no new registration was created.
 * @param sessionActive Whether the session is currently known to the
 * transport.
 */
public record FrostSubscriptionResult(long subscriptionId, String topic, boolean alreadyExisted, boolean sessionActive) {

    /**
     * Renders the result as structured content for the tool result.
     *
     * @return the structured content map.
     */
    public java.util.Map<String, Object> toStructuredContent() {
        final java.util.Map<String, Object> content = new java.util.LinkedHashMap<>();
        content.put("subscriptionId", subscriptionId);
        content.put("topic", topic);
        if (alreadyExisted) {
            content.put("note", "An identical subscription already existed; no new subscription created.");
        }
        content.put("sessionActive", sessionActive);
        return content;
    }

}
