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
import java.util.Map;

/**
 *
 * @author hylke
 */
public class DefEntityType {

    private String name;
    private String plural;
    private Map<String, DefEntityProperty> entityProperties;
    private Map<String, DefNavigationProperty> navigationProperties;
    private String table;

    @JsonIgnore
    private EntityType entityType;

    public void init() {
        for (Map.Entry<String, DefEntityProperty> entry : entityProperties.entrySet()) {
            String typeName = entry.getKey();
            DefEntityProperty property = entry.getValue();
            if (property.getName() == null) {
                property.setName(typeName);
            }
            property.init();
        }
        for (Map.Entry<String, DefNavigationProperty> entry : navigationProperties.entrySet()) {
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
        }
        return entityType;
    }

    public void registerProperties(ModelRegistry modelRegistry) {
        for (DefEntityProperty defEp : entityProperties.values()) {
            modelRegistry.registerEntityProperty(defEp.getEntityPropertyMain());
        }
        for (DefNavigationProperty defNp : navigationProperties.values()) {
            modelRegistry.registerNavProperty(defNp.getNavigationProperty(modelRegistry));
        }
    }

    public void linkProperties(ModelRegistry modelRegistry) {
        entityType.registerProperty(ModelRegistry.EP_SELFLINK, false);
        for (DefEntityProperty defEp : entityProperties.values()) {
            entityType.registerProperty(defEp.getEntityPropertyMain(), defEp.isRequired());
        }
        for (DefNavigationProperty defNp : navigationProperties.values()) {
            entityType.registerProperty(defNp.getNavigationProperty(modelRegistry), defNp.isRequired());
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the plural
     */
    public String getPlural() {
        return plural;
    }

    /**
     * @param plural the plural to set
     */
    public void setPlural(String plural) {
        this.plural = plural;
    }

    /**
     * @return the entityProperties
     */
    public Map<String, DefEntityProperty> getEntityProperties() {
        return entityProperties;
    }

    /**
     * @param entityProperties the entityProperties to set
     */
    public void setEntityProperties(Map<String, DefEntityProperty> entityProperties) {
        this.entityProperties = entityProperties;
    }

    /**
     * @return the navigationProperties
     */
    public Map<String, DefNavigationProperty> getNavigationProperties() {
        return navigationProperties;
    }

    /**
     * @param navigationProperties the navigationProperties to set
     */
    public void setNavigationProperties(Map<String, DefNavigationProperty> navigationProperties) {
        this.navigationProperties = navigationProperties;
    }

    /**
     * @return the table
     */
    public String getTable() {
        return table;
    }

    /**
     * @param table the table to set
     */
    public void setTable(String table) {
        this.table = table;
    }

}
