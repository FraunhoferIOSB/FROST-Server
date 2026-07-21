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
package de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom;

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Custom deserialiser for Entity.
 */
public class CustomEntityDeserializer extends ValueDeserializer<Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomEntityDeserializer.class.getName());
    private static final String BUT_FOUND = " but found: ";

    private static final Map<ModelRegistry, Map<EntityType, Map<Version, CustomEntityDeserializer>>> instancePerModelAndType = new HashMap<>();

    public static CustomEntityDeserializer getInstance(final ModelRegistry modelRegistry, final EntityType entityType, final Version version) {
        return instancePerModelAndType
                .computeIfAbsent(
                        modelRegistry,
                        m -> new HashMap<>())
                .computeIfAbsent(
                        entityType,
                        t -> new HashMap<>())
                .computeIfAbsent(
                        version,
                        v -> new CustomEntityDeserializer(modelRegistry, entityType, v));
    }

    private final Version version;
    private final EntityType entityType;
    private final ModelRegistry modelRegistry;

    public CustomEntityDeserializer(ModelRegistry modelRegistry, EntityType entityType, Version version) {
        this.modelRegistry = modelRegistry;
        this.entityType = entityType;
        this.version = version;
    }

    /**
     * Deserialises an Entity, consuming the Object start and end tokens.
     *
     * @param parser The parser to fetch tokens from.
     * @param ctxt The context to fetch settings from.
     * @return The deserialised Entity.
     * @throws JacksonException If deserialisation fails.
     */
    public Entity deserializeFull(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        parser.nextToken();
        Entity result = deserialize(parser, ctxt);
        parser.nextToken();
        return result;
    }

    @Override
    public Entity deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        Entity target = new DefaultEntity(entityType);

        boolean failOnUnknown = ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.PROPERTY_NAME) {
            String fieldName = parser.currentName();
            if (version.getSelfLinkName().equals(fieldName)) {
                deserializeSelfLink(parser, target);
            } else {
                deserializeProperty(parser, ctxt, target, fieldName, failOnUnknown);
            }
            currentToken = parser.nextToken();
        }

        return target;
    }

    private void deserializeSelfLink(JsonParser parser, Entity target) {
        String selfLink = parser.nextStringValue();
        if (selfLink == null) {
            final String message = "Failed to parse selflink " + version.getSelfLinkName() + ". Expected a string, got " + parser.currentToken();
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        UrlHelper.TypeAndKey typeAndKey = UrlHelper.parseSelfLinkToTypeAndKey(selfLink, modelRegistry, true);
        if (!typeAndKey.entityType().equals(target.getType())) {
            final String message = "Selflink is for a " + typeAndKey + ". Expected " + target.getType();
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        target.setPrimaryKeyValues(typeAndKey.pkValue());
    }

    private void deserializeProperty(JsonParser parser, DeserializationContext ctxt, Entity target, String fieldName, boolean failOnUnknown) throws JacksonException {
        final Property property = entityType.getProperty(fieldName);
        if (property == null) {
            if (failOnUnknown) {
                final String message = "Unknown field: " + fieldName + " on " + entityType.entityName + " expected one of: " + entityType.getProperties();
                throw new UnrecognizedPropertyException(parser, message, parser.currentLocation(), DefaultEntity.class, fieldName, null);
            } else {
                parser.nextValue();
                parser.readValueAsTree();
            }
        } else {
            deserializeProperty(parser, ctxt, property, target);
        }
    }

    private void deserializeProperty(JsonParser parser, DeserializationContext ctxt, Property property, Entity target) throws JacksonException {
        switch (property) {
            case EntityPropertyMain epm ->
                deserializeEntityProperty(parser, ctxt, epm, target);
            case NavigationPropertyEntity npe ->
                deserializeNavigationProperty(parser, ctxt, npe, target);
            case NavigationPropertyEntitySet npes ->
                deserializeNavigationProperty(parser, ctxt, npes, target);
            default -> {
                LOGGER.warn("Unknown property type: {}", property);
            }
        }
    }

    private void deserializeNavigationProperty(JsonParser parser, DeserializationContext ctxt, NavigationPropertyEntitySet npes, Entity result) throws JacksonException {
        final EntityType setType = npes.getEntityType();
        EntitySet entitySet = new EntitySetImpl(npes);
        CustomEntityDeserializer setEntityDeser = getInstance(modelRegistry, setType, version);
        result.setProperty(npes, entitySet);
        JsonToken curToken = parser.nextToken();
        if (curToken != JsonToken.START_ARRAY) {
            final String message = "Expected start of array for: " + npes.getName() + " on " + entityType.entityName + BUT_FOUND + curToken;
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        curToken = parser.nextToken();
        if (curToken != JsonToken.START_OBJECT && curToken != JsonToken.END_ARRAY) {
            final String message = "Expected object in array for: " + npes.getName() + " on " + entityType.entityName + BUT_FOUND + curToken;
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        while (curToken != null && curToken != JsonToken.END_ARRAY) {
            entitySet.add(setEntityDeser.deserialize(parser, ctxt));
            curToken = parser.nextToken();
        }
    }

    private void deserializeNavigationProperty(JsonParser parser, DeserializationContext ctxt, NavigationPropertyEntity npe, Entity target) throws JacksonException {
        final EntityType targetEntityType = npe.getEntityType();
        JsonToken nextToken = parser.nextToken();
        if (nextToken != JsonToken.START_OBJECT) {
            final String message = "Expected start of object for: " + npe.getName() + " on " + entityType.entityName + BUT_FOUND + nextToken;
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        Entity value = getInstance(modelRegistry, targetEntityType, version)
                .deserialize(parser, ctxt);
        npe.setOn(target, value);
    }

    private void deserializeEntityProperty(JsonParser parser, DeserializationContext ctxt, EntityPropertyMain epm, Entity target) throws JacksonException {
        parser.nextValue();
        final ValueDeserializer deserializer = epm.getType().getDeserializer();
        if (deserializer == null) {
            LOGGER.error("Missing deserialiser for {}/{}", entityType, epm);
            return;
        }
        Object value = deserializer.deserialize(parser, ctxt);
        epm.setOn(target, value);
    }

}
