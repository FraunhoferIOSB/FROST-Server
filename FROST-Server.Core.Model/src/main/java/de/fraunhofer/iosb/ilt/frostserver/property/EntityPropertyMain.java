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
package de.fraunhofer.iosb.ilt.frostserver.property;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Convert away from enum.
 *
 * @author jab
 */
public enum EntityPropertyMain implements EntityProperty {
    CREATIONTIME("CreationTime"),
    DESCRIPTION("Description"),
    DEFINITION("Definition"),
    ENCODINGTYPE("EncodingType"),
    FEATURE("Feature", true, false),
    ID("id", AT_IOT_ID),
    LOCATION("Location", true, false),
    METADATA("Metadata"),
    MULTIOBSERVATIONDATATYPES("MultiObservationDataTypes"),
    NAME("Name"),
    OBSERVATIONTYPE("ObservationType"),
    OBSERVEDAREA("ObservedArea"),
    PHENOMENONTIME("PhenomenonTime"),
    PARAMETERS("Parameters", true, false),
    PROPERTIES("Properties", true, false),
    RESULT("Result", true, true),
    RESULTTIME("ResultTime", false, true),
    RESULTQUALITY("ResultQuality", true, false),
    SELFLINK(AT_IOT_SELF_LINK, AT_IOT_SELF_LINK),
    TASKINGPARAMETERS("TaskingParameters", true, false),
    TIME("Time"),
    UNITOFMEASUREMENT("UnitOfMeasurement", true, false),
    UNITOFMEASUREMENTS("UnitOfMeasurements", true, false),
    VALIDTIME("ValidTime");

    private static final Map<String, EntityPropertyMain> PROPERTY_BY_NAME = new HashMap<>();

    static {
        for (EntityPropertyMain property : EntityPropertyMain.values()) {
            for (String alias : property.aliases) {
                PROPERTY_BY_NAME.put(alias.toLowerCase(), property);
            }
        }
    }

    /**
     * The entitiyName of this property as used in URLs.
     */
    public final String entitiyName;
    /**
     * The name of this property as used in json.
     */
    public final String jsonName;

    public final boolean hasCustomProperties;
    /**
     * Flag indicating a null value should not be ignored, but serialised as
     * Json NULL.
     */
    public final boolean serialiseNull;

    private final Collection<String> aliases;

    private final List<GetterSetterSet> gettersSetters = new ArrayList<>();

    private EntityPropertyMain(String codeName) {
        this(codeName, false, false);
    }

    private EntityPropertyMain(String codeName, boolean hasCustomProperties, boolean serialiseNull) {
        this.aliases = new ArrayList<>();
        this.aliases.add(codeName);
        this.entitiyName = StringHelper.deCapitalize(codeName);
        this.jsonName = entitiyName;
        this.hasCustomProperties = hasCustomProperties;
        this.serialiseNull = serialiseNull;
    }

    private EntityPropertyMain(String pathName, String jsonName, String... aliases) {
        this.aliases = new ArrayList<>();
        this.entitiyName = pathName;
        this.jsonName = jsonName;
        this.aliases.add(name());
        this.aliases.add(jsonName);
        this.aliases.addAll(Arrays.asList(aliases));
        this.hasCustomProperties = false;
        this.serialiseNull = false;
    }

    public void addGetterSetterCombo(GetterSetterSet gsc) {
        gettersSetters.add(gsc);
    }

    public static EntityPropertyMain fromString(String propertyName) {
        EntityPropertyMain property = PROPERTY_BY_NAME.get(propertyName.toLowerCase());
        if (property == null) {
            throw new IllegalArgumentException("no entity property with name '" + propertyName + "'");
        }
        return property;
    }

    @Override
    public String getName() {
        return entitiyName;
    }

    @Override
    public String getJsonName() {
        return jsonName;
    }

    @Override
    public Object getFrom(Entity entity) {
        return entity.getProperty(this);
    }

    @Override
    public void setOn(Entity entity, Object value) {
        entity.setProperty(this, value);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        return entity.isSetProperty(this);
    }

    public static interface PropertyGet<T> {

        public Object getFrom(T entity);
    }

    public static interface PropertySet<T> {

        public void setOn(T entity, Object value);
    }

    public static interface PropertyIsSet<T> {

        public boolean isSetOn(T entity);
    }

    public static class GetterSetterSet<T> {

        public final EntityType forType;
        public final PropertyGet<T> getter;
        public final PropertySet<T> setter;
        public final PropertyIsSet<T> isSetter;

        public GetterSetterSet(EntityType forType, PropertyGet<T> getter, PropertySet<T> setter, PropertyIsSet<T> isSetter) {
            this.forType = forType;
            this.getter = getter;
            this.setter = setter;
            this.isSetter = isSetter;
        }

    }
}
