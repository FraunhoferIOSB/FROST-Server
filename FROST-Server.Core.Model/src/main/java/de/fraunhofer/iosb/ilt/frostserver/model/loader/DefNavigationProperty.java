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
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class DefNavigationProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefNavigationProperty.class.getName());

    /**
     * The name of the NavigationProperty.
     */
    private String name;
    /**
     * Flag indicating the NavigationProperty points to an EntitySet.
     */
    private boolean entitySet;
    /**
     * The entity type of the entity(set) this NavigationProperty points to.
     */
    private String entityType;
    /**
     * Flag indicating the property must be set.
     */
    private boolean required;
    /**
     * Handlers used to map the property to a persistence manager.
     */
    private Inverse inverse;

    private List<PropertyPersistenceMapper> handlers;

    @JsonIgnore
    private EntityType sourceEntityType;
    @JsonIgnore
    private EntityType targetEntityType;
    @JsonIgnore
    private NavigationPropertyMain navProp;
    @JsonIgnore
    private NavigationPropertyMain navPropInverse;

    public void init() {
        if (inverse == null) {
            LOGGER.error("Inverse relation not defined for navLink {} on entityType {}", name, entityType);
        }
        if (handlers == null) {
            handlers = Collections.emptyList();
        }
        if (entityType == null) {
            entityType = name;
        }
        for (PropertyPersistenceMapper handler : handlers) {
            handler.setParent(this);
        }
    }

    public void registerProperties(ModelRegistry modelRegistry) {
        if (navProp != null) {
            return;
        }

        if (entitySet) {
            navProp = new NavigationPropertyMain.NavigationPropertyEntitySet(name);
        } else {
            navProp = new NavigationPropertyMain.NavigationPropertyEntity(name);
        }
        targetEntityType = modelRegistry.getEntityTypeForName(entityType);
        navProp.setEntityType(targetEntityType);
        sourceEntityType.registerProperty(navProp, required);

        navPropInverse = targetEntityType.getNavigationProperty(inverse.name);

        if (navPropInverse == null) {
            if (inverse.entitySet) {
                navPropInverse = new NavigationPropertyMain.NavigationPropertyEntitySet(inverse.name, navProp);
            } else {
                navPropInverse = new NavigationPropertyMain.NavigationPropertyEntity(inverse.name, navProp);
            }
            navPropInverse.setEntityType(sourceEntityType);
            targetEntityType.registerProperty(navPropInverse, inverse.required);
        }
        navProp.setInverses(navPropInverse);
    }

    public NavigationPropertyMain getNavigationProperty() {
        return navProp;
    }

    public NavigationPropertyMain getNavigationPropertyInverse() {
        return navPropInverse;
    }

    /**
     * The name of the NavigationProperty.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * The name of the NavigationProperty.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Flag indicating the NavigationProperty points to an EntitySet.
     *
     * @return the entitySet
     */
    public boolean isEntitySet() {
        return entitySet;
    }

    /**
     * Flag indicating the NavigationProperty points to an EntitySet.
     *
     * @param entitySet the entitySet to set
     */
    public void setEntitySet(boolean entitySet) {
        this.entitySet = entitySet;
    }

    /**
     * The entity type of the entity(set) this NavigationProperty points to.
     *
     * @return the entityType
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * The entity type of the entity(set) this NavigationProperty points to.
     *
     * @param entityType the entityType to set
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * Flag indicating the property must be set.
     *
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Flag indicating the property must be set.
     *
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Handlers used to map the property to a persistence manager.
     *
     * @return the handlers
     */
    public List<PropertyPersistenceMapper> getHandlers() {
        return handlers;
    }

    /**
     * Handlers used to map the property to a persistence manager.
     *
     * @param handlers the handlers to set
     */
    public void setHandlers(List<PropertyPersistenceMapper> handlers) {
        this.handlers = handlers;
    }

    /**
     * Handlers used to map the property to a persistence manager.
     *
     * @return the inverse
     */
    public Inverse getInverse() {
        return inverse;
    }

    /**
     * Handlers used to map the property to a persistence manager.
     *
     * @param inverse the inverse to set
     */
    public void setInverse(Inverse inverse) {
        this.inverse = inverse;
    }

    /**
     * @return the sourceEntityType
     */
    public EntityType getSourceEntityType() {
        return sourceEntityType;
    }

    /**
     * @param sourceEntityType the sourceEntityType to set
     */
    public void setSourceEntityType(EntityType sourceEntityType) {
        this.sourceEntityType = sourceEntityType;
    }

    /**
     * @return the targetEntityType
     */
    public EntityType getTargetEntityType() {
        return targetEntityType;
    }

    /**
     * @param targetEntityType the targetEntityType to set
     */
    public void setTargetEntityType(EntityType targetEntityType) {
        this.targetEntityType = targetEntityType;
    }

    public class Inverse {

        /**
         * The name of the NavigationProperty.
         */
        private String name;
        /**
         * Flag indicating the NavigationProperty points to an EntitySet.
         */
        private boolean entitySet;
        /**
         * Flag indicating the property must be set.
         */
        private boolean required;

        /**
         * The name of the NavigationProperty.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * The name of the NavigationProperty.
         *
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Flag indicating the NavigationProperty points to an EntitySet.
         *
         * @return the entitySet
         */
        public boolean isEntitySet() {
            return entitySet;
        }

        /**
         * Flag indicating the NavigationProperty points to an EntitySet.
         *
         * @param entitySet the entitySet to set
         */
        public void setEntitySet(boolean entitySet) {
            this.entitySet = entitySet;
        }

        /**
         * Flag indicating the property must be set.
         *
         * @return the required
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Flag indicating the property must be set.
         *
         * @param required the required to set
         */
        public void setRequired(boolean required) {
            this.required = required;
        }

    }

}
