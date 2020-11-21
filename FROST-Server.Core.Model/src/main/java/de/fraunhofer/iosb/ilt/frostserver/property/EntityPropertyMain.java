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

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_GEOJSONOBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_ID;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_LIST_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_LIST_UOM;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_MAP;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_OBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEINSTANT;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEINTERVAL;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEVALUE;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_UOM;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.geojson.GeoJsonObject;

/**
 * @author jab
 * @author scf
 * @param <P> The type of the value of the property.
 */
public class EntityPropertyMain<P> implements EntityProperty<P> {

    private static final Map<String, EntityPropertyMain> PROPERTY_BY_NAME = new HashMap<>();
    private static final Set<EntityPropertyMain> ALL_PROPERTIES = new LinkedHashSet<>();

    public static final EntityPropertyMain<TimeInstant> CREATIONTIME = registerProperty(new EntityPropertyMain<>("creationTime", TYPE_REFERENCE_TIMEINSTANT));
    public static final EntityPropertyMain<String> DESCRIPTION = registerProperty(new EntityPropertyMain<>("description", TYPE_REFERENCE_STRING));
    public static final EntityPropertyMain<String> DEFINITION = registerProperty(new EntityPropertyMain<>("definition", TYPE_REFERENCE_STRING));
    public static final EntityPropertyMain<String> ENCODINGTYPE = registerProperty(new EntityPropertyMain<>("encodingType", TYPE_REFERENCE_STRING));
    public static final EntityPropertyMain<Object> FEATURE = registerProperty(new EntityPropertyMain<>("feature", null, true, false));
    public static final EntityPropertyMain<Id> ID = registerProperty(new EntityPropertyMain<>(AT_IOT_ID, TYPE_REFERENCE_ID, "id"));
    public static final EntityPropertyMain<Object> LOCATION = registerProperty(new EntityPropertyMain<>("Location", null, true, false));
    public static final EntityPropertyMain<String> METADATA = registerProperty(new EntityPropertyMain<>("Metadata", TYPE_REFERENCE_STRING));
    public static final EntityPropertyMain<List<String>> MULTIOBSERVATIONDATATYPES = registerProperty(new EntityPropertyMain<>("MultiObservationDataTypes", TYPE_REFERENCE_LIST_STRING));
    public static final EntityPropertyMain<String> NAME = registerProperty(new EntityPropertyMain<>("Name", TYPE_REFERENCE_STRING));
    public static final EntityPropertyMain<String> OBSERVATIONTYPE = registerProperty(new EntityPropertyMain<>("ObservationType", TYPE_REFERENCE_STRING));
    public static final EntityPropertyMain<GeoJsonObject> OBSERVEDAREA = registerProperty(new EntityPropertyMain<>("ObservedArea", TYPE_REFERENCE_GEOJSONOBJECT));
    public static final EntityPropertyMain<TimeValue> PHENOMENONTIME = registerProperty(new EntityPropertyMain<>("PhenomenonTime", TYPE_REFERENCE_TIMEVALUE));
    public static final EntityPropertyMain<TimeInterval> PHENOMENONTIME_DS = new EntityPropertyMain<>("PhenomenonTime", TYPE_REFERENCE_TIMEINTERVAL);
    public static final EntityPropertyMain<Map<String, Object>> PARAMETERS = registerProperty(new EntityPropertyMain<>("Parameters", TYPE_REFERENCE_MAP, true, false));
    public static final EntityPropertyMain<Map<String, Object>> PROPERTIES = registerProperty(new EntityPropertyMain<>("Properties", TYPE_REFERENCE_MAP, true, false));
    public static final EntityPropertyMain<Object> RESULT = registerProperty(new EntityPropertyMain<>("Result", TYPE_REFERENCE_OBJECT, true, true));
    public static final EntityPropertyMain<TimeInstant> RESULTTIME = registerProperty(new EntityPropertyMain<>("ResultTime", TYPE_REFERENCE_TIMEINSTANT, false, true));
    // DS has a ResultTime property with type TimeInterval. This property is not registered, since only one Property ResultTime can be.
    public static final EntityPropertyMain<TimeInterval> RESULTTIME_DS = new EntityPropertyMain<>("ResultTime", TYPE_REFERENCE_TIMEINTERVAL, false, true);
    public static final EntityPropertyMain<Object> RESULTQUALITY = registerProperty(new EntityPropertyMain<>("ResultQuality", TYPE_REFERENCE_OBJECT, true, false));
    public static final EntityPropertyMain<String> SELFLINK = registerProperty(new EntityPropertyMain<>(AT_IOT_SELF_LINK, TYPE_REFERENCE_STRING, "SelfLink"));
    public static final EntityPropertyMain<Map<String, Object>> TASKINGPARAMETERS = registerProperty(new EntityPropertyMain<>("TaskingParameters", TYPE_REFERENCE_MAP, true, false));
    public static final EntityPropertyMain<TimeInstant> TIME = registerProperty(new EntityPropertyMain<>("Time", TYPE_REFERENCE_TIMEINSTANT));
    public static final EntityPropertyMain<UnitOfMeasurement> UNITOFMEASUREMENT = registerProperty(new EntityPropertyMain<>("UnitOfMeasurement", TYPE_REFERENCE_UOM, true, false));
    public static final EntityPropertyMain<List<UnitOfMeasurement>> UNITOFMEASUREMENTS = registerProperty(new EntityPropertyMain<>("UnitOfMeasurements", TYPE_REFERENCE_LIST_UOM, true, false));
    public static final EntityPropertyMain<TimeInterval> VALIDTIME = registerProperty(new EntityPropertyMain<>("ValidTime", TYPE_REFERENCE_TIMEINTERVAL));

    public static Set<EntityPropertyMain> values() {
        return ALL_PROPERTIES;
    }

    public static EntityPropertyMain valueOf(String name) {
        return PROPERTY_BY_NAME.get(name);
    }

    public static final <T> EntityPropertyMain<T> registerProperty(EntityPropertyMain<T> property) {
        if (PROPERTY_BY_NAME.containsKey(property.name)) {
            if (PROPERTY_BY_NAME.get(property.name) == property) {
                // This exact property is already registered
                return property;
            } else {
                throw new IllegalArgumentException("A property named " + property.name + " is already registered");
            }
        }
        PROPERTY_BY_NAME.put(property.name, property);
        for (String alias : property.aliases) {
            PROPERTY_BY_NAME.put(alias, property);
        }
        ALL_PROPERTIES.add(property);
        return property;
    }

    /**
     * The entitiyName of this property.
     */
    public final String name;

    /**
     * The type(class) of the type of the value of this property.
     */
    private final TypeReference<P> type;

    public final boolean hasCustomProperties;
    /**
     * Flag indicating a null value should not be ignored, but serialised as
     * Json NULL.
     */
    public final boolean serialiseNull;

    private final Collection<String> aliases;

    public EntityPropertyMain(String name, TypeReference<P> type) {
        this(name, type, false, false);
    }

    public EntityPropertyMain(String name, TypeReference<P> type, boolean hasCustomProperties, boolean serialiseNull) {
        this.type = type;
        this.aliases = new ArrayList<>();
        this.aliases.add(name);
        this.name = StringHelper.deCapitalize(name);
        this.hasCustomProperties = hasCustomProperties;
        this.serialiseNull = serialiseNull;
    }

    public EntityPropertyMain(String name, TypeReference<P> type, String... aliases) {
        this.type = type;
        this.aliases = new ArrayList<>();
        this.name = name;
        this.aliases.add(name);
        this.aliases.addAll(Arrays.asList(aliases));
        this.hasCustomProperties = false;
        this.serialiseNull = false;
    }

    public static EntityPropertyMain fromString(String propertyName) {
        EntityPropertyMain property = PROPERTY_BY_NAME.get(propertyName);
        if (property == null) {
            throw new IllegalArgumentException("no entity property with name '" + propertyName + "'");
        }
        return property;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJsonName() {
        return name;
    }

    @Override
    public TypeReference<P> getType() {
        return type;
    }

    @Override
    public P getFrom(Entity entity) {
        return entity.getProperty(this);
    }

    @Override
    public void setOn(Entity entity, P value) {
        entity.setProperty(this, value);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        return entity.isSetProperty(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityPropertyMain<?> other = (EntityPropertyMain<?>) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
