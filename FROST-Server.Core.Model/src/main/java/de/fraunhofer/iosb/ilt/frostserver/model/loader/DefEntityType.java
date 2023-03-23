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
package de.fraunhofer.iosb.ilt.frostserver.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.configurable.AnnotatedConfigurable;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableClass;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorBoolean;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorClass;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorSubclass;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
@ConfigurableClass
public class DefEntityType implements AnnotatedConfigurable<Void, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefEntityType.class.getName());

    /**
     * The name of the EntityType.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "Name", description = "The name of the Entity Type.")
    @EditorString.EdOptsString()
    private String name;

    /**
     * The plural name of the EntityType.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "Plural", description = "The name to use for for sets of this entity type.")
    @EditorString.EdOptsString()
    private String plural;

    /**
     * Flag indicating only admin users are allowed to see the target entity
     * type.
     */
    @ConfigurableField(editor = EditorBoolean.class,
            label = "Admin Only", description = "Flag indicating only admin users are allowed to see the target entity type.")
    @EditorBoolean.EdOptsBool()
    private boolean adminOnly;

    /**
     * The "table" that data for this EntityType is stored in. What this exactly
     * means depends on the PersistenceManager.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "Table", description = "The 'table' that data for this EntityType is stored in. What this exactly means depends on the PersistenceManager.")
    @EditorString.EdOptsString()
    private String table;

    /**
     * The EntityProperties of the EntityType.
     */
    @ConfigurableField(editor = EditorList.class,
            label = "EntityProps")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = DefEntityProperty.class)
    private List<DefEntityProperty> entityProperties;

    /**
     * The NavigationProperties of the EntityType.
     */
    @ConfigurableField(editor = EditorList.class,
            label = "NavProps")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = DefNavigationProperty.class)
    private List<DefNavigationProperty> navigationProperties;

    /**
     * Validators that are used to validate entities of this type.
     */
    @ConfigurableField(editor = EditorList.class, optional = true,
            label = "NavProps")
    @EditorList.EdOptsList(editor = EditorSubclass.class)
    @EditorSubclass.EdOptsSubclass(iface = DefValidator.class, merge = true, nameField = "@class")
    private List<DefValidator> validators = new ArrayList<>();

    /**
     * The (OData)annotations for this Element.
     */
    @ConfigurableField(editor = EditorList.class,
            label = "Annotations", description = "The (OData)annotations for this Element.")
    @EditorList.EdOptsList(editor = EditorSubclass.class)
    @EditorSubclass.EdOptsSubclass(iface = Annotation.class, merge = true, nameField = "@class")
    private final List<Annotation> annotations = new ArrayList<>();

    @JsonIgnore
    private EntityType entityType;

    public void init() {
        int idx = 0;
        int keyIdx = -1;
        for (DefEntityProperty property : getEntityProperties()) {
            property.init();
            if ("id".equalsIgnoreCase(property.getType())) {
                keyIdx = idx;
            }
            idx++;
        }
        if (keyIdx > 0) {
            LOGGER.debug("Moving Id column from {} to the front.", keyIdx);
            entityProperties.add(0, entityProperties.remove(keyIdx));
        }
        for (DefNavigationProperty property : getNavigationProperties()) {
            property.init();
        }
    }

    public EntityType getEntityType(ModelRegistry modelRegistry) {
        if (entityType == null) {
            entityType = modelRegistry.getEntityTypeForName(name, true);
            if (entityType == null) {
                entityType = new EntityType(name, plural, adminOnly);
            } else if (adminOnly && !entityType.isAdminOnly()) {
                entityType.setAdminOnly(adminOnly);
            }
            for (DefValidator validator : validators) {
                validator.createValidators(modelRegistry, entityType);
            }
            entityType.addAnnotations(annotations);
        }

        return entityType;
    }

    public void linkProperties(ModelRegistry modelRegistry) {
        LOGGER.debug("  {} ({})", name, plural);
        for (DefEntityProperty defEp : entityProperties) {
            defEp.setEntityType(entityType);
            defEp.registerProperties(modelRegistry);
        }
        for (DefNavigationProperty defNp : navigationProperties) {
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
    public List<DefEntityProperty> getEntityProperties() {
        if (entityProperties == null) {
            entityProperties = new ArrayList<>();
        }
        return entityProperties;
    }

    /**
     * The EntityProperties of the EntityType.
     *
     * @param entityProperties the entityProperties to set.
     * @return this.
     */
    public DefEntityType setEntityProperties(List<DefEntityProperty> entityProperties) {
        this.entityProperties = entityProperties;
        return this;
    }

    public DefEntityType addEntityProperty(DefEntityProperty entityProperty) {
        if ("id".equalsIgnoreCase(entityProperty.getType())) {
            LOGGER.debug("Inserting ID column at position 0");
            getEntityProperties().add(0, entityProperty);
        } else {
            getEntityProperties().add(entityProperty);
        }
        return this;
    }

    public DefEntityProperty getPrimaryKey() {
        for (DefEntityProperty property : getEntityProperties()) {
            if ("id".equalsIgnoreCase(property.getType())) {
                return property;
            }
        }
        LOGGER.warn("No primary key defined for {}", getName());
        return null;
    }

    /**
     * The NavigationProperties of the EntityType.
     *
     * @return the navigationProperties.
     */
    public List<DefNavigationProperty> getNavigationProperties() {
        if (navigationProperties == null) {
            navigationProperties = new ArrayList<>();
        }
        return navigationProperties;
    }

    /**
     * The NavigationProperties of the EntityType.
     *
     * @param navigationProperties the navigationProperties to set.
     * @return this.
     */
    public DefEntityType setNavigationProperties(List<DefNavigationProperty> navigationProperties) {
        this.navigationProperties = navigationProperties;
        return this;
    }

    public DefEntityType addNavigationProperty(DefNavigationProperty navigationProperty) {
        getNavigationProperties().add(navigationProperty);
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
    public List<DefValidator> getValidators() {
        return validators;
    }

    /**
     * Validators that are used to validate entities of this type.
     *
     * @param validators the validators to set
     * @return this.
     */
    public DefEntityType setValidators(List<DefValidator> validators) {
        this.validators = validators;
        return this;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public DefEntityType addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public DefEntityType addAnnotations(List<Annotation> annotations) {
        this.annotations.addAll(annotations);
        return this;
    }

    /**
     * Flag indicating only admin users are allowed to see the target entity
     * type.
     *
     * @return the adminOnly
     */
    public boolean isAdminOnly() {
        return adminOnly;
    }

    /**
     * Flag indicating only admin users are allowed to see the target entity
     * type.
     *
     * @param adminOnly the adminOnly to set
     */
    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

}
