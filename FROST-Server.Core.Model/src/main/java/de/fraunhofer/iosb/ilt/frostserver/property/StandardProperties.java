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
package de.fraunhofer.iosb.ilt.frostserver.property;

import static de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain.CUSTOM_PROPS;
import static de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain.SERIALISE_NULLS;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.NULLABLE;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DATETIMEOFFSET;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_GEOMETRY;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_STRING;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.type.ParserUtils;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimpleCustom;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.TreeNode;

/**
 * A list of standard Properties and PropertyTypes.
 */
public class StandardProperties {

    public static final String NAMESPACE = "org.OGC.STA";

    public static final String STA_GEOJSON_NAME = "Geometry";
    public static final String STA_MAP_NAME = "Object";

    public static final String STA_TM_INSTANT_NAME = "TM_Instant";
    public static final String STA_TM_INSTANT_ALIAS = "TimeInstant";

    public static final String STA_TIMEINTERVAL_NAME = "TM_Period";
    public static final String STA_TIMEINTERVAL_ALIAS = "TimeInterval";
    public static final String STA_TIMEVALUE_NAME = "TM_Object";
    public static final String STA_TIMEVALUE_ALIAS = "TimeValue";
    public static final String STA_TIMEVALUE_ALIAS2 = "TimeObject";

    public static final String NAME_INTERVAL_START = "start";
    public static final String NAME_INTERVAL_END = "end";

    public static final String NAME_DEFINITION = "definition";
    public static final String NAME_NAME = "name";
    public static final String NAME_SYMBOL = "symbol";

    public static final PropertyType STA_LOCATION = new TypeSimpleCustom(STA_GEOJSON_NAME, "A Free Location object", EDM_GEOMETRY).setDeserializer(ParserUtils.getLocationDeserializer());
    public static final PropertyType STA_TM_INSTANT = new TypeSimpleCustom(STA_TM_INSTANT_NAME, "A Time Instant", EDM_DATETIMEOFFSET);

    public static final TypeComplex STA_MAP = new TypeComplex(STA_MAP_NAME, "A free object that can contain anything", true, MapValue::new, ParserUtils.getTreeNodeDeserializer(), ParserUtils.getTreeNodeSerializer());

    /**
     * The global EntityProperty SelfLink.
     */
    public static final EntityPropertyMain<String> EP_SELFLINK = new EntityPropertyMain<String>(AT_IOT_SELF_LINK, EDM_STRING).setAliases("selfLink");

    /**
     * The global EntityProperty properties.
     */
    public static final EntityPropertyMain<TreeNode> EP_PROPERTIES = new EntityPropertyMain<>("properties", STA_MAP, NULLABLE, CUSTOM_PROPS);

    /**
     * The global EntityProperty encodingType.
     */
    public static final EntityPropertyMain<String> EP_ENCODINGTYPE = new EntityPropertyMain<>("encodingType", EDM_STRING, REQUIRED);

    public static final EntityPropertyMain<String> EP_NAME = new EntityPropertyMain<>(NAME_NAME, EDM_STRING, NULLABLE, SERIALISE_NULLS);
    public static final EntityPropertyMain<String> EP_DEFINITION = new EntityPropertyMain<>(NAME_DEFINITION, EDM_STRING, NULLABLE, SERIALISE_NULLS);
    public static final EntityPropertyMain<String> EP_SYMBOL = new EntityPropertyMain<>(NAME_SYMBOL, EDM_STRING, NULLABLE, SERIALISE_NULLS);
    public static final EntityPropertyMain<TimeInstant> EP_START_TIME = new EntityPropertyMain<>(NAME_INTERVAL_START, EDM_DATETIMEOFFSET, REQUIRED);
    public static final EntityPropertyMain<TimeInstant> EP_INTERVAL_END_TIME = new EntityPropertyMain<>(NAME_INTERVAL_END, EDM_DATETIMEOFFSET, REQUIRED);
    public static final EntityPropertyMain<TimeInstant> EP_VALUE_END_TIME = new EntityPropertyMain<>(NAME_INTERVAL_END, EDM_DATETIMEOFFSET, NULLABLE);

    public static final TypeComplex STA_TIMEINTERVAL = new TypeComplex(STA_TIMEINTERVAL_NAME, "An ISO time interval.", false, t -> new TimeInterval(), TypeReferencesHelper.TYPE_REFERENCE_TIMEINTERVAL)
            .registerProperty(EP_START_TIME)
            .registerProperty(EP_INTERVAL_END_TIME);

    public static final TypeComplex STA_TIMEVALUE = new TypeComplex(STA_TIMEVALUE_NAME, "An ISO time instant or time interval.", false, t -> new TimeValue(), TypeReferencesHelper.TYPE_REFERENCE_TIMEVALUE)
            .registerProperty(EP_START_TIME)
            .registerProperty(EP_VALUE_END_TIME);

    public static final TypeComplex TYPE_UOM = new TypeComplex("UnitOfMeasurement", "The Unit Of Measurement Type", false)
            .registerProperty(EP_NAME)
            .registerProperty(EP_DEFINITION)
            .registerProperty(EP_SYMBOL);

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardProperties.class.getName());
    private static final Map<String, PropertyType> TYPES = new HashMap<>();

    static {
        for (Field field : FieldUtils.getAllFields(StandardProperties.class)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                Object fieldInstance = FieldUtils.readStaticField(field, false);
                if (fieldInstance instanceof PropertyType pt) {
                    pt.setNamespace(NAMESPACE);
                    final String namespace = pt.getNamespace();
                    final String name = pt.getName();
                    final String fullName = ModelRegistry.fullName(namespace, name);
                    TYPES.put(fullName, pt);
                    TYPES.put(name, pt);
                    LOGGER.debug("Registered type: {}", name);
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.error("Failed to initialise: {}", field, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.trace("Failed to initialise: {}", field, ex);
            }
        }
        TYPES.put(STA_TM_INSTANT_ALIAS, STA_TM_INSTANT);
        TYPES.put(STA_TIMEINTERVAL_ALIAS, STA_TIMEINTERVAL);
        TYPES.put(STA_TIMEVALUE_ALIAS, STA_TIMEVALUE);
        TYPES.put(STA_TIMEVALUE_ALIAS2, STA_TIMEVALUE);
    }

    public static PropertyType getType(String name) {
        return TYPES.get(name);
    }

    private StandardProperties() {
    }

}
