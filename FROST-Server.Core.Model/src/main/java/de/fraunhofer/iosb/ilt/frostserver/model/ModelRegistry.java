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
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.path.ParserHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_STRING;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class ModelRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelRegistry.class.getName());

    /**
     * The global EntityProperty SelfLink.
     */
    public static final EntityPropertyMain<String> EP_SELFLINK = new EntityPropertyMain<>(AT_IOT_SELF_LINK, EDM_STRING, "selfLink");
    /**
     * The global EntityProperty properties.
     */
    public static final EntityPropertyMain<Map<String, Object>> EP_PROPERTIES = new EntityPropertyMain<>("properties", TypeComplex.STA_MAP, true, false);
    /**
     * The global EntityProperty encodingType.
     */
    public static final EntityPropertyMain<String> EP_ENCODINGTYPE = new EntityPropertyMain<>("encodingType", EDM_STRING);

    private final Map<String, EntityType> entityTypesByName = new TreeMap<>();
    private final Set<EntityType> entityTypes = new LinkedHashSet<>();

    private final Map<String, PropertyType> propertyTypes = new TreeMap<>();

    private ParserHelper parserHelper;

    /**
     * Entities need queries, even when sent through messages.
     */
    private final EntityChangedMessage.QueryGenerator messageQueryGenerator = new EntityChangedMessage.QueryGenerator();

    public ModelRegistry() {
    }

    public final ModelRegistry registerEntityType(EntityType type) {
        if (entityTypesByName.containsKey(type.entityName)) {
            throw new IllegalArgumentException("An entity type named " + type.entityName + " is already registered");
        }
        entityTypesByName.put(type.entityName, type);
        entityTypesByName.put(type.plural, type);
        entityTypes.add(type);
        type.setModelRegistry(this);
        return this;
    }

    public final EntityType getEntityTypeForName(String typeName) {
        return entityTypesByName.get(typeName);
    }

    public final Set<EntityType> getEntityTypes() {
        return entityTypes;
    }

    public ModelRegistry registerPropertyType(PropertyType type) {
        propertyTypes.put(type.getName(), type);
        return this;
    }

    public final PropertyType getPropertyType(String name) {
        PropertyType type = propertyTypes.get(name);
        if (type != null) {
            return type;
        }
        type = TypeSimplePrimitive.getPrimitiveType(name);
        if (type != null) {
            return type;
        }
        throw new IllegalArgumentException("unknown property type: " + name);
    }

    public EntityChangedMessage.QueryGenerator getMessageQueryGenerator() {
        return messageQueryGenerator;
    }

    public synchronized void initFinalise() {
        LOGGER.info("Finalising {} EntityTypes.", entityTypes.size());
        for (EntityType type : entityTypes) {
            type.init();
        }
    }

    public ParserHelper getParserHelper() {
        if (parserHelper == null) {
            parserHelper = new ParserHelper(this);
        }
        return parserHelper;
    }
}
