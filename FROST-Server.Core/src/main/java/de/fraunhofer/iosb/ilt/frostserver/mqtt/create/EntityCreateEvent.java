/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.mqtt.create;

import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.EventObject;

/**
 *
 * @author jab
 */
public class EntityCreateEvent extends EventObject {

    private final String topic;
    private final String payload;
    private final PrincipalExtended principal;

    public EntityCreateEvent(Object source, String topic, String payload) {
        this(source, topic, payload, PrincipalExtended.ANONYMOUS_PRINCIPAL);
    }

    public EntityCreateEvent(Object source, String topic, String payload, PrincipalExtended principal) {
        super(source);
        this.topic = topic;
        this.payload = payload;
        this.principal = principal;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }

    public PrincipalExtended getPrincipal() {
        return principal;
    }

}
