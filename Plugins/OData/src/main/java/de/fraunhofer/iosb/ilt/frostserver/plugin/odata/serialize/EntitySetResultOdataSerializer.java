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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serialises entity set.
 */
public class EntitySetResultOdataSerializer extends ValueSerializer<EntitySetResultOdata> {

    private final String contextField;
    private final String countField;
    private final String nextLinkField;

    public EntitySetResultOdataSerializer(String contextField, String countField, String nextLinkField) {
        this.contextField = contextField;
        this.countField = countField;
        this.nextLinkField = nextLinkField;
    }

    @Override
    public void serialize(EntitySetResultOdata value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        gen.writeStartObject();
        gen.writeStringProperty(contextField, value.getContext());

        long count = value.getValues().getCount();
        if (count >= 0) {
            gen.writeNumberProperty(countField, count);
        }

        gen.writeArrayPropertyStart("value");
        for (Entity child : value.getValues()) {
            gen.writePOJO(child);
        }
        gen.writeEndArray();

        String nextLink = value.getValues().getNextLink();
        if (nextLink != null) {
            gen.writeStringProperty(nextLinkField, nextLink);
        }
    }

    @Override
    public boolean isEmpty(SerializationContext provider, EntitySetResultOdata value) {
        return (value == null || value.getValues() == null);
    }

}
