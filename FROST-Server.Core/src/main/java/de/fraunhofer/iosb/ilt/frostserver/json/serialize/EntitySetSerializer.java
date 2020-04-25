/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.json.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import java.io.IOException;

/**
 * Defines how an EntitySet is serialized. If an EntitySet has a navigationLink
 * property EntityJsonSerializer makes sure that it is only serialized as a
 * String, otherwise the EntitySet will be serialized using this class and only
 * the content of the EntitySet will be serialized.
 *
 * @author jab
 */
public class EntitySetSerializer extends JsonSerializer<EntitySet<? extends Entity>> {

    @Override
    public void serialize(EntitySet value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("value");
        gen.writeObject(value.asList());
        gen.writeEndObject();
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, EntitySet value) {
        return (value == null || value.isEmpty());
    }

}
