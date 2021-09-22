/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import java.util.HashMap;
import java.util.Map;

/**
 * The possible id generation modes.
 */
public enum IdGenerationType {
    SERVER_GENERATED_ONLY,
    SERVER_AND_CLIENT_GENERATED,
    CLIENT_GENERATED_ONLY;

    private static final Map<String, IdGenerationType> aliases = new HashMap<>();

    static {
        for (IdGenerationType type : IdGenerationType.values()) {
            aliases.put(type.name(), type);
            aliases.put(type.name().replace("_", "").toLowerCase(), type);
        }
    }

    public static IdGenerationType findType(String input) {
        return aliases.get(input.toLowerCase());
    }

}
