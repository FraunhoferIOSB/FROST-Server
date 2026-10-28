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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.jsonarray;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JsonGenerator;

/**
 * A JSON format that is more compact for large results.
 */
public class JsonArrayResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonArrayResult.class);
    private Query query;
    private Structure structure;
    private EntitySet entitySet;

    public void serialiseData(JsonGenerator gen) {
        gen.writeStartObject();
        gen.writeName("structure");
        structure.serialise(gen);

        long count = entitySet.getCount();
        if (count >= 0) {
            gen.writeNumberProperty("@count", count);
        }
        if (entitySet != null) {
            gen.writeName("value");

            gen.writeStartArray();
            for (Entity e : entitySet) {
                gen.writeStartArray();
                for (var element : structure.elements) {
                    element.serialiseData(e, gen);
                }
                gen.writeEndArray();
            }
            gen.writeEndArray();
        }

        if (query.getMetadata() != Metadata.OFF) {
            String nextLink = entitySet.getNextLink();
            if (nextLink != null) {
                gen.writeStringProperty("@nextLink", nextLink);
            }
        }
        gen.writeEndObject();
    }

    private static class Structure {

        private final List<StructureElement> elements = new ArrayList<>();

        public void serialise(JsonGenerator gen) {
            gen.writeStartArray();
            for (var element : elements) {
                element.serialiseStructure(gen);
            }
            gen.writeEndArray();
        }

        public static Structure of(TypeComplex tc) {
            Structure target = new Structure();
            for (var property : tc.getEntityProperties()) {
                handleProperty(property, target);
            }
            return target;
        }

        public static Structure of(Query query) {
            Structure result = new Structure();
            final Set<Property> select = query.getSelect();
            if (StringHelper.isNullOrEmpty(select)) {
                select.addAll(query.getEntityType().getEntityProperties());
            }
            for (var property : select) {
                handleProperty(property, result);
            }
            for (var expand : query.getExpand()) {
                NavigationProperty np = expand.getPath();
                if (np instanceof NavigationPropertyEntity npe) {
                    result.elements.add(new ElementExpandEntity(npe, expand.getSubQuery()));
                } else if (np instanceof NavigationPropertyEntitySet npes) {
                    result.elements.add(new ElementExpandSet(npes, expand.getSubQuery()));
                } else {
                    LOGGER.warn("Not supported: {}", np);
                }
            }
            return result;
        }

        private static void handleProperty(Property property, Structure target) {
            PropertyType type = property.getType();
            if (property instanceof EntityPropertyMain epm) {
                if (epm.hasCustomProperties) {
                    target.elements.add(new ElementSimple(epm));
                } else if (type instanceof TypeComplex tc) {
                    target.elements.add(new ElementComplex(epm, tc));
                } else {
                    target.elements.add(new ElementSimple(epm));
                }
            } else if (property instanceof EntityPropertyCustomSelect epcs) {
                target.elements.add(new ElementCustom(epcs));
            } else {
                LOGGER.warn("Unhandled: {}", property);
            }
        }
    }

    private static interface StructureElement {

        public void serialiseStructure(JsonGenerator gen);

        public void serialiseData(ComplexValue vcItem, JsonGenerator gen);
    }

    private static class ElementSimple implements StructureElement {

        private final EntityPropertyMain property;

        public ElementSimple(EntityPropertyMain property) {
            this.property = property;
        }

        @Override
        public void serialiseStructure(JsonGenerator gen) {
            gen.writeString(property.getJsonName());
        }

        @Override
        public void serialiseData(ComplexValue vcItem, JsonGenerator gen) {
            if (vcItem == null) {
                gen.writeNull();
            } else {
                gen.writePOJO(vcItem.getProperty(property));
            }
        }
    }

    private static class ElementCustom implements StructureElement {

        private final EntityPropertyCustomSelect property;

        public ElementCustom(EntityPropertyCustomSelect property) {
            this.property = property;
        }

        @Override
        public void serialiseStructure(JsonGenerator gen) {
            gen.writeString(property.getName());
        }

        @Override
        public void serialiseData(ComplexValue vcItem, JsonGenerator gen) {
            if (vcItem == null) {
                gen.writeNull();
            } else {
                gen.writePOJO(property.getFrom(vcItem));
            }
        }
    }

    private static class ElementComplex implements StructureElement {

        private final EntityPropertyMain property;
        private final Structure structure;

        public ElementComplex(EntityPropertyMain property, TypeComplex tc) {
            this.property = property;
            this.structure = Structure.of(tc);
        }

        @Override
        public void serialiseStructure(JsonGenerator gen) {
            gen.writeStartObject();
            gen.writeStringProperty("attribute", property.getJsonName());
            gen.writeName("structure");
            structure.serialise(gen);
            gen.writeEndObject();
        }

        @Override
        public void serialiseData(ComplexValue vcItem, JsonGenerator gen) {
            Object value = vcItem.getProperty(property);
            if (value == null) {
                gen.writeNull();
                return;
            }
            if (value instanceof ComplexValue cv) {
                gen.writeStartArray();
                for (var element : structure.elements) {
                    element.serialiseData(cv, gen);
                }
                gen.writeEndArray();
                return;
            }
            LOGGER.error("Found non-complex value: {}", value);
            gen.writePOJO(value);
        }
    }

    private static class ElementExpandSet implements StructureElement {

        private final NavigationPropertyEntitySet property;
        private final Structure structure;
        private final Query query;
        private boolean writeNextLink;

        public ElementExpandSet(NavigationPropertyEntitySet property, Query query) {
            this.query = query;
            this.property = property;
            this.structure = Structure.of(query);
        }

        @Override
        public void serialiseStructure(JsonGenerator gen) {
            if (query.isCountOrDefault()) {
                gen.writeString(property.getJsonName() + "@count");
            }
            gen.writeStartObject();
            gen.writeStringProperty("attribute", property.getJsonName());
            gen.writeName("structure");
            gen.writeStartArray();
            for (var element : structure.elements) {
                element.serialiseStructure(gen);
            }
            gen.writeEndArray();
            gen.writeEndObject();
            if (query.getMetadata() != Metadata.OFF) {
                writeNextLink = true;
                gen.writeString(property.getJsonName() + "@nextLink");
            }
        }

        @Override
        public void serialiseData(ComplexValue vcItem, JsonGenerator gen) {
            Object value = vcItem.getProperty(property);
            if (value == null) {
                gen.writeNull();
                return;
            }

            if (value instanceof EntitySet es) {
                long count = es.getCount();
                if (count >= 0) {
                    gen.writeNumberProperty(property.getJsonName() + "@count", count);
                }
                gen.writeStartArray();
                for (Entity e : es) {
                    gen.writeStartArray();
                    for (var element : structure.elements) {
                        element.serialiseData(e, gen);
                    }
                    gen.writeEndArray();
                }
                gen.writeEndArray();
                if (writeNextLink) {
                    String nextLink = es.getNextLink();
                    if (nextLink == null) {
                        gen.writeNull();
                    } else {
                        gen.writeString(nextLink);
                    }
                }
                return;
            }
            LOGGER.error("Found non-EntitySet value: {}", value);
            gen.writePOJO(value);
        }
    }

    private static class ElementExpandEntity implements StructureElement {

        private final NavigationPropertyEntity property;
        private final Structure structure;
        private final Query query;

        public ElementExpandEntity(NavigationPropertyEntity property, Query query) {
            this.query = query;
            this.property = property;
            this.structure = Structure.of(query);
        }

        @Override
        public void serialiseStructure(JsonGenerator gen) {
            gen.writeStartObject();
            gen.writeStringProperty("attribute", property.getJsonName());
            gen.writeName("structure");
            structure.serialise(gen);
            gen.writeEndObject();
        }

        @Override
        public void serialiseData(ComplexValue vcItem, JsonGenerator gen) {
            Object value = vcItem.getProperty(property);
            if (value == null) {
                gen.writeNull();
                return;
            }

            if (value instanceof Entity e) {
                gen.writeStartArray();
                for (var element : structure.elements) {
                    element.serialiseData(e, gen);
                }
                gen.writeEndArray();
                return;
            }
            LOGGER.error("Found non-Entity value: {}", value);
            gen.writePOJO(value);
        }
    }

    public static JsonArrayResult of(Query query, EntitySet entitySet) {

        JsonArrayResult result = new JsonArrayResult();
        result.query = query;
        result.structure = Structure.of(query);
        result.entitySet = entitySet;
        return result;
    }
}
