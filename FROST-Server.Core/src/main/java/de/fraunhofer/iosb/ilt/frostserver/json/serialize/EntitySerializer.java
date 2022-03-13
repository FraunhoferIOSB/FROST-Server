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
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NAVIGATION_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NEXT_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles serialization of Entity objects. If a field is of type Entity and
 * contains a non-empty navigationLink the field will be renamed with the suffix
 * '@iot.navigationLink' and will only contain the navigationLink as String.
 *
 * @author jab
 * @author scf
 */
public class EntitySerializer extends JsonSerializer<Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntitySerializer.class.getName());

    private final String countField;
    private final String navLinkField;
    private final String nextLinkField;
    private final boolean serialiseAllNulls;
    private final Map<EntityPropertyMain, SimplePropertySerializer> propertySerializers = new HashMap<>();
    private final Map<PropertyType, SimplePropertySerializer> propertyTypeSerializers = new HashMap<>();

    public EntitySerializer() {
        this(false, AT_IOT_COUNT, AT_IOT_NAVIGATION_LINK, AT_IOT_NEXT_LINK, AT_IOT_SELF_LINK);
    }

    public EntitySerializer(boolean nulls, String countField, String navLinkField, String nextLinkField, String selfLinkField) {
        this.countField = countField;
        this.navLinkField = navLinkField;
        this.nextLinkField = nextLinkField;
        this.serialiseAllNulls = nulls;
        propertySerializers.put(ModelRegistry.EP_SELFLINK, (ep, entity, gen) -> {
            final String value = entity.getSelfLink();
            if (value != null) {
                gen.writeStringField(selfLinkField, value);
            }
        });
    }

    @Override
    public void serialize(Entity entity, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        try {
            writeContent(entity, gen);
        } catch (IOException | RuntimeException exc) {
            LOGGER.error("Failed to serialise entity.", exc);
            throw new IOException("could not serialize Entity");
        } finally {
            gen.writeEndObject();
        }
    }

    public void writeContent(Entity entity, JsonGenerator gen) throws IOException {
        Set<EntityPropertyMain> entityProps;
        Set<NavigationPropertyMain> navigationProps;
        List<Expand> expand;
        Query query = entity.getQuery();
        Metadata metadata = Metadata.DEFAULT;
        if (query == null) {
            navigationProps = Collections.emptySet();
            entityProps = entity.getEntityType().getEntityProperties();
            entityProps.add(ModelRegistry.EP_SELFLINK);
            expand = null;
        } else {
            metadata = query.getMetadata();
            if (metadata == Metadata.OFF || metadata == Metadata.NONE || metadata == Metadata.MINIMAL) {
                navigationProps = Collections.emptySet();
            } else {
                navigationProps = query.getSelectNavProperties(query.hasParentExpand());
            }
            entityProps = query.getSelectMainEntityProperties(query.hasParentExpand());
            expand = query.getExpand();
        }
        for (Iterator<EntityPropertyMain> it = entityProps.iterator(); it.hasNext();) {
            EntityPropertyMain ep = it.next();
            writeProperty(ep, entity, gen);
        }
        if (expand != null) {
            writeExpand(expand, entity, gen);
        }
        for (Iterator<NavigationPropertyMain> it = navigationProps.iterator(); it.hasNext();) {
            NavigationPropertyMain np = it.next();
            String navigationLink = np.getNavigationLink(entity);
            if (navigationLink != null && (np.isEntitySet() || entity.getProperty(np) != null)) {
                gen.writeStringField(np.getName() + navLinkField, navigationLink);
            }
            if (metadata == Metadata.INTERNAL_COMPARE) {
                writeExpand(null, entity, np, gen);
            }
        }
    }

    public void writeProperty(EntityPropertyMain ep, Entity entity, JsonGenerator gen) throws IOException {
        SimplePropertySerializer ser = propertySerializers.get(ep);
        if (ser != null) {
            ser.writeProperty(ep, entity, gen);
            return;
        }
        ser = propertyTypeSerializers.get(ep.getType());
        if (ser != null) {
            ser.writeProperty(ep, entity, gen);
            return;
        }
        final Object value = entity.getProperty(ep);
        if (serialiseAllNulls || value != null || ep.serialiseNull) {
            final String name = ep.name;
            if (serialiseAllNulls && "@iot.id".equals(name)) {
                gen.writeObjectField("id", value);
            } else {
                gen.writeObjectField(name, value);
            }
        }
    }

    private void writeExpand(List<Expand> expand, Entity entity, JsonGenerator gen) throws IOException {
        for (Expand exp : expand) {
            NavigationProperty np = exp.getPath();
            if (np instanceof NavigationPropertyMain) {
                writeExpand(exp, entity, (NavigationPropertyMain) np, gen);
            }
        }
    }

    private void writeExpand(Expand exp, Entity entity, NavigationPropertyMain np, JsonGenerator gen) throws IOException {
        Object entityOrSet = np.getFrom(entity);
        if (np.isEntitySet()) {
            EntitySet entitySet = (EntitySet) entityOrSet;
            writeEntitySet(np, entitySet, gen);
        } else {
            Entity expandedEntity = (Entity) entityOrSet;
            if (expandedEntity != null) {
                if (expandedEntity.getQuery() == null && exp != null) {
                    expandedEntity.setQuery(exp.getSubQuery());
                }
                gen.writeObjectField(np.getJsonName(), entityOrSet);
            }
        }
    }

    private void writeEntitySet(NavigationProperty np, EntitySet entitySet, JsonGenerator gen) throws IOException {
        String jsonName = np.getJsonName();
        if (entitySet == null) {
            gen.writeArrayFieldStart(jsonName);
            gen.writeEndArray();
            return;
        }
        long count = entitySet.getCount();
        if (count >= 0) {
            gen.writeNumberField(jsonName + countField, count);
        }
        gen.writeArrayFieldStart(jsonName);
        for (Object child : entitySet) {
            gen.writeObject(child);
        }
        gen.writeEndArray();
        String nextLink = entitySet.getNextLink();
        if (nextLink != null) {
            gen.writeStringField(jsonName + nextLinkField, nextLink);
        }
    }

    public <P> EntitySerializer addPropertySerializer(EntityPropertyMain<P> property, SimplePropertySerializer serializer) {
        propertySerializers.put(property, serializer);
        return this;
    }

    public EntitySerializer addPropertyTypeSerializer(PropertyType propertyType, SimplePropertySerializer serializer) {
        propertyTypeSerializers.put(propertyType, serializer);
        return this;
    }

    public static interface SimplePropertySerializer {

        public void writeProperty(EntityPropertyMain ep, Entity entity, JsonGenerator gen) throws IOException;
    }
}
