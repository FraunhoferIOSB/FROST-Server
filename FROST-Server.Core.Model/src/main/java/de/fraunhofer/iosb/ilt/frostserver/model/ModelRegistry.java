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
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.path.CustomLinksHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.Exceptions;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for a data model.
 */
public class ModelRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelRegistry.class.getName());

    public static final String DEFAULT_NAMESPACE = "de.FROST";

    private final Set<String> namespaces = new LinkedHashSet<>();

    /**
     * All entity types, by their entityName (both singular and plural).
     */
    private final Map<String, EntityType> entityTypesByName = new TreeMap<>();

    /**
     * All entity types.
     */
    private final Set<EntityType> entityTypesAll = new TreeSet<>();

    /**
     * All entity types accessible to non-admin users.
     */
    private final Set<EntityType> entityTypesNonAdmin = new TreeSet<>();

    /**
     * All property types by their name.
     */
    private final Map<String, PropertyType> propertyTypes = new TreeMap<>();

    private CustomLinksHelper customLinksHelper;

    /**
     * Entities need queries, even when sent through messages.
     */
    private final EntityChangedMessage.QueryGenerator messageQueryGenerator = new EntityChangedMessage.QueryGenerator();

    private String ensureNamespace(EntityType type) {
        String namespace = type.getNamespace();
        if (StringHelper.isNullOrEmpty(namespace)) {
            namespace = maybeAddNamespace(namespace);
            type.setNamespace(namespace);
            return namespace;
        }
        return maybeAddNamespace(namespace);
    }

    private String maybeAddNamespace(String namespace) {
        if (StringHelper.isNullOrEmpty(namespace)) {
            namespace = DEFAULT_NAMESPACE;
        }
        if (namespaces.add(namespace)) {
            LOGGER.info("Registered namespace {}", namespace);
        }
        return namespace;
    }

    private boolean hasNamespace(String name) {
        return name.contains(".");
    }

    public Set<String> getNamespaces() {
        return namespaces;
    }

    /**
     * Register a new entity type. Registering the same type twice is a no-op,
     * registering a new entity type with a name that already exists causes an
     * {@link IllegalArgumentException}.
     *
     * @param type The entity type to register.
     * @return this ModelRegistry.
     */
    public final ModelRegistry registerEntityType(EntityType type) {
        String namespace = ensureNamespace(type);
        final String fullName = namespace + '.' + type.entityName;
        final String fullPlural = namespace + '.' + type.plural;

        EntityType existing = entityTypesByName.get(fullName);
        if (existing == type) {
            LOGGER.info("Entity type {} already registered.", fullName);
            return this;
        }
        Exceptions.illegalArgumentIf(existing != null, "Duplicate entity type name: {}", fullName);

        entityTypesByName.put(fullName, type);
        entityTypesByName.put(fullPlural, type);
        entityTypesAll.add(type);
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
        EntityType type = entityTypesByName.get(typeName);
        if (type != null) {
            return checkAdmin(type, isAdmin);
        }

        if (hasNamespace(typeName)) {
            LOGGER.debug("No entity type found for name {}", typeName);
            return null;
        }

        for (String namespace : namespaces) {
            final String fullName = namespace + '.' + typeName;
            type = entityTypesByName.get(fullName);
            if (type != null) {
                LOGGER.info("Resolved entity type {} to {}", typeName, fullName);
                entityTypesByName.put(typeName, type);
                return checkAdmin(type, isAdmin);
            }
        }
        LOGGER.debug("No entity type found for name {}", typeName);
        return null;
    }

    private EntityType checkAdmin(EntityType type, boolean isAdmin) {
        if (type.isAdminOnly() && !isAdmin) {
            return null;
        }
        return type;
    }

    public final Set<EntityType> getEntityTypes() {
        return entityTypesNonAdmin;
    }

    public final Set<EntityType> getEntityTypes(boolean isAdmin) {
        if (isAdmin) {
            return entityTypesAll;
        }
        return entityTypesNonAdmin;
    }

    public ModelRegistry registerPropertyType(PropertyType type) {
        String fullName = fullName(type.getNamespace(), type.getName());
        PropertyType old = propertyTypes.put(fullName, type);
        if (old != null && old != type) {
            LOGGER.warn("Overwritten the property type {}", type);
        }
        return this;
    }

    public final PropertyType getPropertyType(String fullName) {
        PropertyType type = propertyTypes.get(fullName);
        if (type != null) {
            return type;
        }
        type = TypeSimplePrimitive.getType(fullName);
        if (type != null) {
            return type;
        }
        type = StandardProperties.getType(fullName);
        Exceptions.illegalArgumentIf(type == null, "Unknown property type {}", fullName);
        // This provided custom type was not registered yet, do it now.
        registerPropertyType(type);
        return type;
    }

    public Map<String, PropertyType> getPropertyTypes() {
        return propertyTypes;
    }

    public EntityChangedMessage.QueryGenerator getMessageQueryGenerator() {
        return messageQueryGenerator;
    }

    public synchronized void initFinalise() {
        LOGGER.info("Finalising {} EntityTypes.", entityTypesAll.size());
        for (EntityType type : entityTypesAll) {
            type.init();
        }
    }

    public CustomLinksHelper getCustomLinksHelper() {
        if (customLinksHelper == null) {
            customLinksHelper = new CustomLinksHelper(this, false, 0);
        }
        return customLinksHelper;
    }

    public static final String fullName(String namespace, String name) {
        return namespace == null ? name : namespace + '.' + name;
    }
}
