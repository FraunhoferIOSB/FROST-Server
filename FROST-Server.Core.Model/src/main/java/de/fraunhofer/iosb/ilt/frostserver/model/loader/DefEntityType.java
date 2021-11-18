/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntityValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hylke
 */
public class DefEntityType {

    /**
     * The name of the EntityType.
     */
    private String name;
    /**
     * The plural name of the EntityType.
     */
    private String plural;
    /**
     * The EntityProperties of the EntityType.
     */
    private Map<String, DefEntityProperty> entityProperties;
    /**
     * The NavigationProperties of the EntityType.
     */
    private Map<String, DefNavigationProperty> navigationProperties;
    /**
     * The "table" that data for this EntityType is stored in. What this exactly
     * means depends on the PersistenceManager.
     */
    private String table;
    /**
     * Validators that are used to validate entities of this type.
     */
    private List<EntityValidator> validators = new ArrayList<>();

    @JsonIgnore
    private EntityType entityType;

    public void init() {
        for (Map.Entry<String, DefEntityProperty> entry : getEntityProperties().entrySet()) {
            String typeName = entry.getKey();
            DefEntityProperty property = entry.getValue();
            if (property.getName() == null) {
                property.setName(typeName);
            }
            property.init();
        }
        for (Map.Entry<String, DefNavigationProperty> entry : getNavigationProperties().entrySet()) {
            String typeName = entry.getKey();
            DefNavigationProperty property = entry.getValue();
            if (property.getName() == null) {
                property.setName(typeName);
            }
            property.init();
        }
    }

    public EntityType getEntityType() {
        if (entityType == null) {
            entityType = new EntityType(name, plural);
            for (EntityValidator validator : validators) {
                entityType.addValidator(validator);
            }
        }
        return entityType;
    }

    public void linkProperties(ModelRegistry modelRegistry) {
        for (DefEntityProperty defEp : entityProperties.values()) {
            defEp.setEntityType(entityType);
            defEp.registerProperties(modelRegistry);
        }
        for (DefNavigationProperty defNp : navigationProperties.values()) {
            defNp.setSourceEntityType(entityType);
            defNp.registerProperties(modelRegistry);
        }
    }

    /**
     * The name of the EntityType.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * The name of the EntityType.
     *
     * @param name the name to set.
     * @return this.
     */
    public DefEntityType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * The plural name of the EntityType.
     *
     * @return the plural.
     */
    public String getPlural() {
        return plural;
    }

    /**
     * The plural name of the EntityType.
     *
     * @param plural the plural to set.
     * @return this.
     */
    public DefEntityType setPlural(String plural) {
        this.plural = plural;
        return this;
    }

    /**
     * The EntityProperties of the EntityType.
     *
     * @return the entityProperties.
     */
    public Map<String, DefEntityProperty> getEntityProperties() {
        if (entityProperties == null) {
            entityProperties = new LinkedHashMap<>();
        }
        return entityProperties;
    }

    /**
     * The EntityProperties of the EntityType.
     *
     * @param entityProperties the entityProperties to set.
     * @return this.
     */
    public DefEntityType setEntityProperties(Map<String, DefEntityProperty> entityProperties) {
        this.entityProperties = entityProperties;
        return this;
    }

    public DefEntityType addEntityProperty(DefEntityProperty entityProperty) {
        getEntityProperties().put(entityProperty.getName(), entityProperty);
        return this;
    }

    /**
     * The NavigationProperties of the EntityType.
     *
     * @return the navigationProperties.
     */
    public Map<String, DefNavigationProperty> getNavigationProperties() {
        if (navigationProperties == null) {
            navigationProperties = new LinkedHashMap<>();
        }
        return navigationProperties;
    }

    /**
     * The NavigationProperties of the EntityType.
     *
     * @param navigationProperties the navigationProperties to set.
     * @return this.
     */
    public DefEntityType setNavigationProperties(Map<String, DefNavigationProperty> navigationProperties) {
        this.navigationProperties = navigationProperties;
        return this;
    }

    public DefEntityType addNavigationProperty(DefNavigationProperty navigationProperty) {
        getNavigationProperties().put(navigationProperty.getName(), navigationProperty);
        return this;
    }

    /**
     * The "table" that data for this EntityType is stored in. What this exactly
     * means depends on the PersistenceManager.
     *
     * @return the table
     */
    public String getTable() {
        return table;
    }

    /**
     * The "table" that data for this EntityType is stored in. What this exactly
     * means depends on the PersistenceManager.
     *
     * @param table the table to set
     * @return this.
     */
    public DefEntityType setTable(String table) {
        this.table = table;
        return this;
    }

    /**
     * Validators that are used to validate entities of this type.
     *
     * @return the validators
     */
    public List<EntityValidator> getValidators() {
        return validators;
    }

    /**
     * Validators that are used to validate entities of this type.
     *
     * @param validators the validators to set
     * @return this.
     */
    public DefEntityType setValidators(List<EntityValidator> validators) {
        this.validators = validators;
        return this;
    }

}
