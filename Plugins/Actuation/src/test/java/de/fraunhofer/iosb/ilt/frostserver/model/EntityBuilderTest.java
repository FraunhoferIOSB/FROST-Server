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
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.plugin.actuation.PluginActuation;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class EntityBuilderTest {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBuilderTest.class);

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static PluginCoreModel pluginCoreModel;
    private static PluginActuation pluginActuation;

    private final Map<Property, Object> propertyValues = new HashMap<>();
    private final Map<Property, Object> propertyValuesAlternative = new HashMap<>();

    @BeforeClass
    public static void initClass() {
        if (queryDefaults == null) {
            coreSettings = new CoreSettings();
            modelRegistry = coreSettings.getModelRegistry();
            queryDefaults = coreSettings.getQueryDefaults();
            queryDefaults.setUseAbsoluteNavigationLinks(false);
            pluginCoreModel = new PluginCoreModel();
            pluginCoreModel.init(coreSettings);
            pluginActuation = new PluginActuation();
            pluginActuation.init(coreSettings);
            coreSettings.getPluginManager().registerPlugin(pluginActuation);
            coreSettings.getPluginManager().initPlugins(coreSettings, null);
        }
    }

    @Before
    public void setUp() {
        propertyValues.put(pluginCoreModel.EP_CREATIONTIME, TimeInstant.now());
        propertyValues.put(pluginCoreModel.EP_DEFINITION, "MyDefinition");
        propertyValues.put(pluginCoreModel.EP_DESCRIPTION, "My description");
        propertyValues.put(ModelRegistry.EP_ENCODINGTYPE, "My EncodingType");
        propertyValues.put(pluginCoreModel.EP_FEATURE, new Point(8, 42));
        propertyValues.put(ModelRegistry.EP_ID, new IdLong(1));
        propertyValues.put(pluginCoreModel.EP_LOCATION, new Point(9, 43));
        propertyValues.put(pluginCoreModel.EP_METADATA, "my meta data");
        propertyValues.put(pluginCoreModel.EP_NAME, "myName");
        propertyValues.put(pluginCoreModel.EP_OBSERVATIONTYPE, "my Type");
        propertyValues.put(pluginCoreModel.EP_OBSERVEDAREA, new Polygon(new LngLatAlt(0, 0), new LngLatAlt(1, 0), new LngLatAlt(1, 1)));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValues.put(pluginCoreModel.EP_PARAMETERS, parameters);
        propertyValues.put(pluginCoreModel.EP_PHENOMENONTIME, TimeInstant.now());
        propertyValuesAlternative.put(pluginCoreModel.EP_PHENOMENONTIME, TimeInterval.parse("2014-03-02T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(ModelRegistry.EP_PROPERTIES, parameters);
        propertyValues.put(pluginCoreModel.EP_RESULT, 42);
        propertyValues.put(pluginCoreModel.EP_RESULTQUALITY, "myQuality");
        propertyValues.put(pluginCoreModel.EP_RESULTTIME, TimeInstant.now());
        propertyValuesAlternative.put(pluginCoreModel.EP_RESULTTIME, TimeInterval.parse("2014-03-01T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");
        propertyValues.put(pluginActuation.EP_TASKINGPARAMETERS, parameters);
        propertyValues.put(pluginCoreModel.EP_TIME, TimeInstant.now());
        UnitOfMeasurement unit1 = new UnitOfMeasurement("unitName", "unitSymbol", "unitDefinition");
        UnitOfMeasurement unit2 = new UnitOfMeasurement("unitName2", "unitSymbol2", "unitDefinition2");
        propertyValues.put(pluginCoreModel.EP_UNITOFMEASUREMENT, unit1);
        propertyValues.put(pluginCoreModel.EP_VALIDTIME, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"));

        for (EntityPropertyMain ep : modelRegistry.getEntityProperties()) {
            Assert.assertTrue("Missing value for " + ep, propertyValues.containsKey(ep));
        }

        int nextId = 100;
        propertyValues.put(pluginActuation.NP_ACTUATOR, new DefaultEntity(pluginActuation.ACTUATOR, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_DATASTREAM, new DefaultEntity(pluginCoreModel.DATASTREAM, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_FEATUREOFINTEREST, new DefaultEntity(pluginCoreModel.FEATURE_OF_INTEREST, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_HISTORICALLOCATION, new DefaultEntity(pluginCoreModel.HISTORICAL_LOCATION, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_LOCATION, new DefaultEntity(pluginCoreModel.LOCATION, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_OBSERVATION, new DefaultEntity(pluginCoreModel.OBSERVATION, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_OBSERVEDPROPERTY, new DefaultEntity(pluginCoreModel.OBSERVED_PROPERTY, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_SENSOR, new DefaultEntity(pluginCoreModel.SENSOR, new IdLong(nextId++)));
        propertyValues.put(pluginActuation.NP_TASK, new DefaultEntity(pluginActuation.TASK, new IdLong(nextId++)));
        propertyValues.put(pluginActuation.NP_TASKINGCAPABILITY, new DefaultEntity(pluginActuation.TASKING_CAPABILITY, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_THING, new DefaultEntity(pluginCoreModel.THING, new IdLong(nextId++)));

        EntitySetImpl actuators = new EntitySetImpl(pluginActuation.ACTUATOR);
        actuators.add(new DefaultEntity(pluginActuation.ACTUATOR, new IdLong(nextId++)));
        actuators.add(new DefaultEntity(pluginActuation.ACTUATOR, new IdLong(nextId++)));
        propertyValues.put(pluginActuation.NP_ACTUATORS, actuators);

        EntitySetImpl datastreams = new EntitySetImpl(pluginCoreModel.DATASTREAM);
        datastreams.add(new DefaultEntity(pluginCoreModel.DATASTREAM, new IdLong(nextId++)));
        datastreams.add(new DefaultEntity(pluginCoreModel.DATASTREAM, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_DATASTREAMS, datastreams);

        EntitySetImpl features = new EntitySetImpl(pluginCoreModel.FEATURE_OF_INTEREST);
        features.add(new DefaultEntity(pluginCoreModel.FEATURE_OF_INTEREST, new IdLong(nextId++)));
        features.add(new DefaultEntity(pluginCoreModel.FEATURE_OF_INTEREST, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_FEATURESOFINTEREST, features);

        EntitySetImpl histLocations = new EntitySetImpl(pluginCoreModel.HISTORICAL_LOCATION);
        histLocations.add(new DefaultEntity(pluginCoreModel.HISTORICAL_LOCATION, new IdLong(nextId++)));
        histLocations.add(new DefaultEntity(pluginCoreModel.HISTORICAL_LOCATION, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_HISTORICALLOCATIONS, histLocations);

        EntitySetImpl locations = new EntitySetImpl(pluginCoreModel.LOCATION);
        locations.add(new DefaultEntity(pluginCoreModel.LOCATION, new IdLong(nextId++)));
        locations.add(new DefaultEntity(pluginCoreModel.LOCATION, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_LOCATIONS, locations);

        EntitySetImpl observations = new EntitySetImpl(pluginCoreModel.OBSERVATION);
        observations.add(new DefaultEntity(pluginCoreModel.OBSERVATION, new IdLong(nextId++)));
        observations.add(new DefaultEntity(pluginCoreModel.OBSERVATION, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_OBSERVATIONS, observations);

        EntitySetImpl obsProperties = new EntitySetImpl(pluginCoreModel.OBSERVED_PROPERTY);
        obsProperties.add(new DefaultEntity(pluginCoreModel.OBSERVED_PROPERTY, new IdLong(nextId++)));
        obsProperties.add(new DefaultEntity(pluginCoreModel.OBSERVED_PROPERTY, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_OBSERVEDPROPERTIES, obsProperties);

        EntitySetImpl sensors = new EntitySetImpl(pluginCoreModel.SENSOR);
        sensors.add(new DefaultEntity(pluginCoreModel.SENSOR, new IdLong(nextId++)));
        sensors.add(new DefaultEntity(pluginCoreModel.SENSOR, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_SENSORS, sensors);

        EntitySetImpl tasks = new EntitySetImpl(pluginActuation.TASK);
        tasks.add(new DefaultEntity(pluginActuation.TASK, new IdLong(nextId++)));
        tasks.add(new DefaultEntity(pluginActuation.TASK, new IdLong(nextId++)));
        propertyValues.put(pluginActuation.NP_TASKS, tasks);

        EntitySetImpl taskingCapabilities = new EntitySetImpl(pluginActuation.TASKING_CAPABILITY);
        taskingCapabilities.add(new DefaultEntity(pluginActuation.TASKING_CAPABILITY, new IdLong(nextId++)));
        taskingCapabilities.add(new DefaultEntity(pluginActuation.TASKING_CAPABILITY, new IdLong(nextId++)));
        propertyValues.put(pluginActuation.NP_TASKINGCAPABILITIES, taskingCapabilities);

        EntitySetImpl things = new EntitySetImpl(pluginCoreModel.THING);
        things.add(new DefaultEntity(pluginCoreModel.THING, new IdLong(nextId++)));
        things.add(new DefaultEntity(pluginCoreModel.THING, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_THINGS, things);

        for (NavigationPropertyMain np : modelRegistry.getNavProperties()) {
            Assert.assertTrue("Missing value for " + np, propertyValues.containsKey(np));
        }

    }

    @Test
    public void testEntityBuilders() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Set<EntityType> entityTypes = modelRegistry.getEntityTypes();
        Assert.assertTrue("Actuation entities not registered.", entityTypes.contains(pluginActuation.ACTUATOR));
        for (EntityType type : entityTypes) {
            testEntityType(type, type.getPropertySet());
        }
    }

    private void testEntityType(EntityType type, Set<Property> collectedProperties) {
        String pName = "";
        try {

            Entity entity = new DefaultEntity(type);
            Entity entity2 = new DefaultEntity(type);
            for (Property p : collectedProperties) {
                pName = p.toString();
                addPropertyToObject(entity, p);
                Assert.assertNotEquals("Property " + pName + " should influence equals.", entity, entity2);

                addPropertyToObject(entity2, p);
                Assert.assertEquals("Entities should be the same after adding " + pName + " to both.", entity, entity2);

                getPropertyFromObject(entity, p);
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Failed create entity.", ex);
            Assert.fail("Failed create entity: " + ex.getMessage());
        }
    }

    private void addPropertyToObject(Entity entity, Property property) {
        try {
            addPropertyToObject(entity, property, propertyValues);
        } catch (IllegalArgumentException ex) {
            addPropertyToObject(entity, property, propertyValuesAlternative);
        }
    }

    private void addPropertyToObject(Entity entity, Property property, Map<Property, Object> valuesToUse) {
        Object value = valuesToUse.get(property);
        try {
            property.setOn(entity, value);
        } catch (NullPointerException ex) {
            LOGGER.error("Failed to set property " + property, ex);
            Assert.fail("Failed to set property " + property + ": " + ex.getMessage());
        }
    }

    private void getPropertyFromObject(Entity entity, Property property) {
        try {
            if (!(property instanceof NavigationPropertyMain) && !entity.isSetProperty(property)) {
                Assert.fail("Property " + property + " returned false for isSet on entity type " + entity.getEntityType());
            }
            Object value = propertyValues.get(property);
            Object value2 = propertyValuesAlternative.get(property);
            Object setValue = property.getFrom(entity);

            if (!(Objects.equals(value, setValue) || Objects.equals(value2, setValue))) {
                Assert.fail("Getter did not return set value for property " + property + " on entity type " + entity.getEntityType());
            }
        } catch (SecurityException | IllegalArgumentException ex) {
            LOGGER.error("Failed to set property", ex);
            Assert.fail("Failed to set property: " + ex.getMessage());
        }
    }

}
