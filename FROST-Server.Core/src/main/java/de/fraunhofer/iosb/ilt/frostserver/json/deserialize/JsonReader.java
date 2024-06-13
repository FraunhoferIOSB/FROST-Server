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
package de.fraunhofer.iosb.ilt.frostserver.json.deserialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import java.io.IOException;
import java.io.Reader;

/**
 * The interface for json parsers.
 *
 * @author hylke
 */
public interface JsonReader {

    /**
     * Get the ObjectMapper used by this JsonReader.
     *
     * @return the ObjectMapper used by this JsonReader.
     */
    public ObjectMapper getMapper();

    /**
     * Parse an entity of the given type from the given String.
     *
     * @param entityType The type of entity to read.
     * @param value The String to parse.
     * @return An entity of the given type, created from the given string.
     * @throws IOException If parsing fails.
     */
    public Entity parseEntity(EntityType entityType, String value) throws IOException;

    /**
     * Parse an entity of the given type from the given Reader.
     *
     * @param entityType The type of entity to read.
     * @param value The Reader to read data from.
     * @return An entity of the given type, created from the data in the given
     * reader.
     * @throws IOException If parsing fails.
     */
    public Entity parseEntity(EntityType entityType, Reader value) throws IOException;

}
