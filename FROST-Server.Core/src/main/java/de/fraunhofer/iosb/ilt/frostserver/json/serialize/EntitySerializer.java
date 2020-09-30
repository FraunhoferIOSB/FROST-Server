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
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NAVIGATION_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NEXT_LINK;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

    @Override
    public void serialize(Entity entity, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        try {
            Set<EntityPropertyMain> entityProps;
            Set<NavigationPropertyMain> navigationProps;
            List<Expand> expand;
            Query query = entity.getQuery();
            if (query == null) {
                entityProps = entity.getEntityType().getEntityProperties();
                navigationProps = Collections.emptySet();
                expand = null;
            } else {
                entityProps = query.getSelectMainEntityProperties(query.hasParentExpand());
                navigationProps = query.getSelectNavProperties(query.hasParentExpand());
                expand = query.getExpand();
            }
            // Ensure selfLink is initialised.
            if (entityProps.contains(EntityPropertyMain.SELFLINK)) {
                entity.getSelfLink();
            }
            for (Iterator<EntityPropertyMain> it = entityProps.iterator(); it.hasNext();) {
                EntityPropertyMain ep = it.next();
                Object value = entity.getProperty(ep);
                if (value != null || ep.serialiseNull) {
                    gen.writeObjectField(ep.jsonName, value);
                }
            }
            if (expand != null) {
                writeExpand(expand, entity, gen);
            }
            for (Iterator<NavigationPropertyMain> it = navigationProps.iterator(); it.hasNext();) {
                NavigationPropertyMain np = it.next();
                gen.writeStringField(np.getName() + AT_IOT_NAVIGATION_LINK, np.getNavigationLink(entity));
            }

        } catch (IOException | RuntimeException exc) {
            LOGGER.error("Failed to serialise entity.", exc);
            throw new IOException("could not serialize Entity");
        } finally {
            gen.writeEndObject();
        }
    }

    private void writeExpand(List<Expand> expand, Entity entity, JsonGenerator gen) throws IOException {
        for (Expand exp : expand) {
            NavigationProperty np = exp.getPath();
            if (np instanceof NavigationPropertyCustom) {
                continue;
            }
            Object entityOrSet = np.getFrom(entity);
            if (np.isEntitySet()) {
                EntitySet entitySet = (EntitySet) entityOrSet;
                writeEntitySet(np, entitySet, gen);
            } else {
                Entity expandedEntity = (Entity) entityOrSet;
                if (expandedEntity == null) {
                    return;
                }
                if (expandedEntity.getQuery() == null) {
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
            gen.writeNumberField(jsonName + AT_IOT_COUNT, count);
        }
        String nextLink = entitySet.getNextLink();
        if (nextLink != null) {
            gen.writeStringField(jsonName + AT_IOT_NEXT_LINK, nextLink);
        }
        gen.writeArrayFieldStart(jsonName);
        for (Object child : entitySet.asList()) {
            gen.writeObject(child);
        }
        gen.writeEndArray();
    }

}
