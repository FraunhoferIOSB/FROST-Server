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
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorBoolean;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorSubclass;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class DefEntityProperty implements AnnotatedConfigurable<Void, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefEntityProperty.class.getName());

    /**
     * The name of the EntityProperty.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "Name", description = "The name of the EntityProperty.")
    @EditorString.EdOptsString()
    private String name;

    /**
     * The description of the EntityProperty.
     */
    @ConfigurableField(editor = EditorString.class, optional = true,
            label = "Description", description = "The description of the EntityProperty.")
    @EditorString.EdOptsString()
    private String description;

    /**
     * Aliases for the name of the Property.
     */
    @ConfigurableField(editor = EditorList.class, optional = true,
            label = "Aliases", description = "Aliases for the name of the Property.")
    @EditorList.EdOptsList(editor = EditorString.class)
    @EditorString.EdOptsString()
    private List<String> aliases;

    /**
     * The java type (Class) of the Property.
     */
    @ConfigurableField(editor = EditorString.class,
            label = "Type", description = "The java type (Class) of the Property.")
    @EditorString.EdOptsString(dflt = "Edm.String")
    private String type;

    /**
     * Flag indicating the property must be set.
     */
    @ConfigurableField(editor = EditorBoolean.class, optional = true,
            label = "Required", description = "Flag indicating the property must be set.")
    @EditorBoolean.EdOptsBool()
    private boolean required;

    /**
     * Flag indicating the property may be set to null.
     */
    @ConfigurableField(editor = EditorBoolean.class, optional = true,
            label = "Nullable", description = "Flag indicating the property may be set to null.")
    @EditorBoolean.EdOptsBool(dflt = true)
    private boolean nullable;

    /**
     * Flag indicating this property must always be serialised, even if it is null.
     */
    @ConfigurableField(editor = EditorBoolean.class, optional = true,
            label = "Serialise NULL", description = "Flag indicating this property must always be serialised, even if it is null.")
    @EditorBoolean.EdOptsBool()
    private boolean serialiseNull;

    /**
     * Flag indicating this property is a complex property with sub-properties that can be queried.
     */
    @ConfigurableField(editor = EditorBoolean.class, optional = true,
            label = "HasCustomProps", description = "Flag indicating this property is a complex property with sub-properties that can be queried.")
    @EditorBoolean.EdOptsBool()
    private boolean hasCustomProperties;

    /**
     * Handlers used to map the property to a persistence manager.
     */
    @ConfigurableField(editor = EditorList.class,
            label = "Handlers", description = "The handler(s) defining the database access.")
    @EditorList.EdOptsList(editor = EditorSubclass.class)
    @EditorSubclass.EdOptsSubclass(iface = PropertyPersistenceMapper.class, merge = true, nameField = "@class")
    private List<PropertyPersistenceMapper> handlers;

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
    @JsonIgnore
    private EntityPropertyMain entityProperty;

    public void init() {
        if (aliases == null) {
            aliases = Collections.emptyList();
        }
        if (handlers == null) {
            handlers = Collections.emptyList();
        }
        for (PropertyPersistenceMapper handler : handlers) {
            handler.setParent(this);
        }
    }

    public void registerProperties(ModelRegistry modelRegistry) {
        if (entityProperty == null) {
            PropertyType propType = modelRegistry.getPropertyType(type);
            entityProperty = new EntityPropertyMain<>(name, propType, required, nullable, hasCustomProperties, serialiseNull)
                    .setAliases(aliases.toArray(String[]::new))
                    .addAnnotations(annotations);
        }
        LOGGER.debug("    {} ({})", name, type);
        entityType.registerProperty(entityProperty);
    }

    public EntityPropertyMain getEntityProperty() {
        return entityProperty;
    }

    /**
     * The name of the EntityProperty.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * The name of the EntityProperty.
     *
     * @param name the name to set
     * @return this.
     */
    public DefEntityProperty setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * The description of the EntityProperty.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * The description of the EntityProperty.
     *
     * @param description the description to set.
     * @return this.
     */
    public DefEntityProperty setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Aliases for the name of the Property.
     *
     * @return the aliases
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Aliases for the name of the Property.
     *
     * @param aliases the aliases to set
     * @return this.
     */
    public DefEntityProperty setAliases(List<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    public DefEntityProperty addAlias(String alias) {
        if (aliases == null) {
            aliases = new ArrayList<>();
        }
        aliases.add(alias);
        return this;
    }

    /**
     * The java type (Class) of the Property.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * The java type (Class) of the Property.
     *
     * @param type the type to set
     * @return this.
     */
    public DefEntityProperty setType(String type) {
        this.type = type;
        return this;
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
     * @return this.
     */
    public DefEntityProperty setRequired(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Handlers used to map the property to a persistence manager.
     *
     * @return the handlers
     */
    public List<PropertyPersistenceMapper> getHandlers() {
        if (handlers == null) {
            handlers = new ArrayList<>();
        }
        return handlers;
    }

    /**
     * Handlers used to map the property to a persistence manager.
     *
     * @param handlers the handlers to set
     * @return this.
     */
    public DefEntityProperty setHandlers(List<PropertyPersistenceMapper> handlers) {
        this.handlers = handlers;
        return this;
    }

    public DefEntityProperty addHandler(PropertyPersistenceMapper handler) {
        getHandlers().add(handler);
        return this;
    }

    /**
     * Flag indicating this property must always be serialised, even if it is null.
     *
     * @return the serialiseNull
     */
    public boolean getSerialiseNull() {
        return serialiseNull;
    }

    /**
     * Flag indicating this property must always be serialised, even if it is null.
     *
     * @param serialiseNull the serialiseNull to set
     * @return this.
     */
    public DefEntityProperty setSerialiseNull(boolean serialiseNull) {
        this.serialiseNull = serialiseNull;
        return this;
    }

    /**
     * Flag indicating this property is a complex property with sub-properties that can be queried.
     *
     * @return the hasCustomProperties
     */
    public boolean getHasCustomProperties() {
        return hasCustomProperties;
    }

    /**
     * Flag indicating this property is a complex property with sub-properties that can be queried.
     *
     * @param hasCustomProperties the hasCustomProperties to set
     * @return this.
     */
    public DefEntityProperty setHasCustomProperties(boolean hasCustomProperties) {
        this.hasCustomProperties = hasCustomProperties;
        return this;
    }

    public DefEntityProperty setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public DefEntityProperty addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public DefEntityProperty addAnnotations(List<Annotation> annotationsToAdd) {
        annotations.addAll(annotationsToAdd);
        return this;
    }

}
