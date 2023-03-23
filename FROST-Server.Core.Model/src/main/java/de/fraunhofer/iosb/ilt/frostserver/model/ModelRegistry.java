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
package de.fraunhofer.iosb.ilt.frostserver.model;

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_STRING;

import de.fraunhofer.iosb.ilt.frostserver.path.ParserHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimpleCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
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
    public static final EntityPropertyMain<String> EP_SELFLINK = new EntityPropertyMain<String>(AT_IOT_SELF_LINK, EDM_STRING).setAliases("selfLink");
    /**
     * The global EntityProperty properties.
     */
    public static final EntityPropertyMain<Map<String, Object>> EP_PROPERTIES = new EntityPropertyMain<>("properties", TypeComplex.STA_MAP, false, true, true, false);
    /**
     * The global EntityProperty encodingType.
     */
    public static final EntityPropertyMain<String> EP_ENCODINGTYPE = new EntityPropertyMain<>("encodingType", EDM_STRING, true, false);

    /**
     * All entity types, by their entityName (both singular and plural).
     */
    private final Map<String, EntityType> entityTypesByName = new TreeMap<>();

    /**
     * All entity types.
     */
    private final Set<EntityType> entityTypes = new LinkedHashSet<>();

    /**
     * All entity types accessible to non-admin users.
     */
    private final Set<EntityType> entityTypesNonAdmin = new LinkedHashSet<>();

    /**
     * All property types by their name.
     */
    private final Map<String, PropertyType> propertyTypes = new TreeMap<>();

    private ParserHelper parserHelper;

    /**
     * Entities need queries, even when sent through messages.
     */
    private final EntityChangedMessage.QueryGenerator messageQueryGenerator = new EntityChangedMessage.QueryGenerator();

    /**
     * Register a new entity type. Registering the same type twice is a no-op,
     * registering a new entity type with a name that already exists causes an
     * {@link IllegalArgumentException}.
     *
     * @param type The entity type to register.
     * @return this ModelRegistry.
     */
    public final ModelRegistry registerEntityType(EntityType type) {
        EntityType existing = entityTypesByName.get(type.entityName);
        if (existing == type) {
            LOGGER.info("Entity type {} already registered.", type.entityName);
            return this;
        }
        if (existing != null) {
            LOGGER.error("Duplicate entity type name: {}", type.entityName);
            throw new IllegalArgumentException("An entity type named " + type.entityName + " is already registered");
        }
        entityTypesByName.put(type.entityName, type);
        entityTypesByName.put(type.plural, type);
        entityTypes.add(type);
        if (!type.isAdminOnly()) {
            entityTypesNonAdmin.add(type);
        }
        type.setModelRegistry(this);
        return this;
    }

    /**
     * Get the entity type with the given name, only taking non-admin-only
     * entity types into account.
     *
     * @param typeName The name of the entity type to find.
     * @return the entity type with the given name, or null.
     */
    public final EntityType getEntityTypeForName(String typeName) {
        return getEntityTypeForName(typeName, false);
    }

    /**
     * Get the entity type with the given name. If isAdmin is true, admin only
     * entity types can also be returned.
     *
     * @param typeName The name of the entity type to find.
     * @param isAdmin Flag indicating if the requester is admin.
     * @return the entity type with the given name, or null.
     */
    public final EntityType getEntityTypeForName(String typeName, boolean isAdmin) {
        final EntityType type = entityTypesByName.get(typeName);
        if (type == null) {
            return null;
        }
        if (!isAdmin && type.isAdminOnly()) {
            return null;
        }
        return type;
    }

    public final Set<EntityType> getEntityTypes() {
        return entityTypesNonAdmin;
    }

    public final Set<EntityType> getEntityTypes(boolean isAdmin) {
        if (isAdmin) {
            return entityTypes;
        }
        return entityTypesNonAdmin;
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
        type = TypeSimplePrimitive.getType(name);
        if (type != null) {
            return type;
        }
        type = TypeSimpleCustom.getType(name);
        if (type != null) {
            return type;
        }
        type = TypeComplex.getType(name);
        if (type != null) {
            return type;
        }
        throw new IllegalArgumentException("unknown property type: " + name);
    }

    public Map<String, PropertyType> getPropertyTypes() {
        return propertyTypes;
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
