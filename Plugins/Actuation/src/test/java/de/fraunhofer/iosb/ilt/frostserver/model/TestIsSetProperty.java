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
public class TestIsSetProperty {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestIsSetProperty.class);

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
        propertyValues.put(pluginCoreModel.EP_TIME, TimeInstant.now());
        propertyValues.put(pluginActuation.EP_TASKINGPARAMETERS, parameters);
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

        EntitySetImpl tasks = new EntitySetImpl(pluginActuation.TASK);
        tasks.add(new DefaultEntity(pluginActuation.TASK, new IdLong(nextId++)));
        tasks.add(new DefaultEntity(pluginActuation.TASK, new IdLong(nextId++)));
        propertyValues.put(pluginActuation.NP_TASKS, tasks);

        EntitySetImpl taskingCapabilities = new EntitySetImpl(pluginActuation.TASKING_CAPABILITY);
        taskingCapabilities.add(new DefaultEntity(pluginActuation.TASKING_CAPABILITY, new IdLong(nextId++)));
        taskingCapabilities.add(new DefaultEntity(pluginActuation.TASKING_CAPABILITY, new IdLong(nextId++)));
        propertyValues.put(pluginActuation.NP_TASKINGCAPABILITIES, taskingCapabilities);

        EntitySetImpl sensors = new EntitySetImpl(pluginCoreModel.SENSOR);
        sensors.add(new DefaultEntity(pluginCoreModel.SENSOR, new IdLong(nextId++)));
        sensors.add(new DefaultEntity(pluginCoreModel.SENSOR, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_SENSORS, sensors);

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
        for (EntityType type : modelRegistry.getEntityTypes()) {
            testEntityType(type, type.getPropertySet());
            testEntityCompare(type, type.getPropertySet());
        }
    }

    private void testEntityType(EntityType type, Set<Property> collectedProperties) {
        Entity entity = new DefaultEntity(type);
        entity.setEntityPropertiesSet();
        for (Property p : collectedProperties) {
            isSetPropertyOnObject(entity, p, true);
        }
        entity.setEntityPropertiesSet(false, false);
        for (Property p : collectedProperties) {
            isSetPropertyOnObject(entity, p, false);
        }
        entity.setEntityPropertiesSet(true, false);
        for (Property p : collectedProperties) {
            isSetPropertyOnObject(entity, p, true);
        }
    }

    private void testEntityCompare(EntityType type, Set<Property> collectedProperties) {
        try {

            Entity entity = new DefaultEntity(type);
            for (Property p : collectedProperties) {
                addPropertyToObject(entity, p);
            }
            Entity entityEmpty = new DefaultEntity(type);

            EntityChangedMessage message = new EntityChangedMessage();
            entityEmpty.setEntityPropertiesSet(entity, message);
            testPropertiesChanged(message, collectedProperties, entity, true);

            message = new EntityChangedMessage();
            entityEmpty.setEntityPropertiesSet(entityEmpty, message);
            testPropertiesChanged(message, collectedProperties, entityEmpty, false);

        } catch (NoSuchMethodException ex) {
            LOGGER.error("Failed to access property.", ex);
            Assert.fail("Failed to access property: " + ex.getMessage());
        }
    }

    private void testPropertiesChanged(EntityChangedMessage message, Set<Property> collectedProperties, Entity entity, boolean shouldBeChanged) {
        Set<Property> changedFields = message.getFields();
        for (Property p : collectedProperties) {
            if (p instanceof NavigationPropertyMain) {
                NavigationPropertyMain nProp = (NavigationPropertyMain) p;
                if (nProp.isEntitySet()) {
                    continue;
                }
            }
            if (shouldBeChanged && !changedFields.contains(p)) {
                Assert.fail("Diff claims that Property: " + entity.getEntityType() + "/" + p + " did not change.");
            }
            if (!shouldBeChanged && changedFields.contains(p)) {
                Assert.fail("Diff claims that Property: " + entity.getEntityType() + "/" + p + " did change.");
            }
            isSetPropertyOnObject(entity, p, shouldBeChanged);
        }
    }

    private void addPropertyToObject(Entity entity, Property property) throws NoSuchMethodException {
        try {
            addPropertyToObject(entity, property, propertyValues);
        } catch (IllegalArgumentException ex) {
            addPropertyToObject(entity, property, propertyValuesAlternative);
        }
    }

    private void addPropertyToObject(Entity entity, Property property, Map<Property, Object> valuesToUse) throws NoSuchMethodException {
        Object value = valuesToUse.get(property);
        entity.setProperty(property, value);
    }

    private void isSetPropertyOnObject(Entity entity, Property property, boolean shouldBeSet) {
        try {
            if (property instanceof NavigationPropertyMain) {
                return;
            }
            if (shouldBeSet != entity.isSetProperty(property)) {
                Assert.fail("Property " + property + " returned false for isSet on entity type " + entity.getEntityType());
            }
        } catch (SecurityException | IllegalArgumentException ex) {
            LOGGER.error("Failed to set property", ex);
            Assert.fail("Failed to set property: " + ex.getMessage());
        }
    }

    @Test
    public void testDatastream() {
        Entity entity = new DefaultEntity(pluginCoreModel.DATASTREAM);
        testIsSetPropertyDatastream(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyDatastream(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyDatastream(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyDatastream(true, true, entity);
    }

    private void testIsSetPropertyDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Entity datastream) {
        testIsSetPropertyAbstractDatastream(shouldBeSet, shouldIdBeSet, datastream);
        Assert.assertEquals(shouldBeSet, datastream.isSetProperty(pluginCoreModel.NP_OBSERVEDPROPERTY));
        Assert.assertEquals(shouldBeSet, datastream.isSetProperty(pluginCoreModel.EP_UNITOFMEASUREMENT));
    }

    @Test
    public void testFeatureOfInterest() {
        Entity entity = new DefaultEntity(pluginCoreModel.FEATURE_OF_INTEREST);
        testIsSetPropertyFeatureOfInterest(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyFeatureOfInterest(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyFeatureOfInterest(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyFeatureOfInterest(true, true, entity);
    }

    private void testIsSetPropertyFeatureOfInterest(boolean shouldBeSet, boolean shouldIdBeSet, Entity featureOfInterest) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, featureOfInterest);
        Assert.assertEquals(shouldBeSet, featureOfInterest.isSetProperty(ModelRegistry.EP_ENCODINGTYPE));
        Assert.assertEquals(shouldBeSet, featureOfInterest.isSetProperty(pluginCoreModel.EP_FEATURE));
    }

    @Test
    public void testHistoricalLocation() {
        Entity entity = new DefaultEntity(pluginCoreModel.HISTORICAL_LOCATION);
        testIsSetPropertyHistoricalLocation(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyHistoricalLocation(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyHistoricalLocation(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyHistoricalLocation(true, true, entity);
    }

    private void testIsSetPropertyHistoricalLocation(boolean shouldBeSet, boolean shouldIdBeSet, Entity hl) {
        testIsSetPropertyAbstractEntity(shouldBeSet, shouldIdBeSet, hl);
        Assert.assertEquals(shouldBeSet, hl.isSetProperty(pluginCoreModel.NP_THING));
        Assert.assertEquals(shouldBeSet, hl.isSetProperty(pluginCoreModel.EP_TIME));
    }

    @Test
    public void testLocation() {
        Entity entity = new DefaultEntity(pluginCoreModel.LOCATION);
        testIsSetPropertyLocation(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyLocation(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyLocation(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyLocation(true, true, entity);
    }

    private void testIsSetPropertyLocation(boolean shouldBeSet, boolean shouldIdBeSet, Entity location) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, location);
        Assert.assertEquals(shouldBeSet, location.isSetProperty(ModelRegistry.EP_ENCODINGTYPE));
        Assert.assertEquals(shouldBeSet, location.isSetProperty(pluginCoreModel.EP_LOCATION));
    }

    private void testIsSetPropertyAbstractDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Entity mds) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, mds);
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.EP_OBSERVATIONTYPE));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.EP_OBSERVEDAREA));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.EP_PHENOMENONTIME));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.EP_RESULTTIME));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.NP_SENSOR));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.NP_THING));
    }

    @Test
    public void testObservation() {
        Entity entity = new DefaultEntity(pluginCoreModel.OBSERVATION);
        testIsSetPropertyObservation(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyObservation(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyObservation(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyObservation(true, true, entity);
    }

    private void testIsSetPropertyObservation(boolean shouldBeSet, boolean shouldIdBeSet, Entity o) {
        testIsSetPropertyAbstractEntity(shouldBeSet, shouldIdBeSet, o);
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.NP_DATASTREAM));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.NP_FEATUREOFINTEREST));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.EP_PARAMETERS));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.EP_PHENOMENONTIME));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.EP_RESULT));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.EP_RESULTQUALITY));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.EP_RESULTTIME));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.EP_VALIDTIME));
    }

    @Test
    public void testObservedProperty() {
        Entity entity = new DefaultEntity(pluginCoreModel.OBSERVED_PROPERTY);
        testIsSetPropertyObservedProperty(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyObservedProperty(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyObservedProperty(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyObservedProperty(true, true, entity);
    }

    private void testIsSetPropertyObservedProperty(boolean shouldBeSet, boolean shouldIdBeSet, Entity op) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, op);
        Assert.assertEquals(shouldBeSet, op.isSetProperty(pluginCoreModel.EP_DEFINITION));
    }

    @Test
    public void testSensor() {
        Entity entity = new DefaultEntity(pluginCoreModel.SENSOR);
        testIsSetPropertySensor(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertySensor(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertySensor(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertySensor(true, true, entity);
    }

    private void testIsSetPropertySensor(boolean shouldBeSet, boolean shouldIdBeSet, Entity sensor) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, sensor);
        Assert.assertEquals(shouldBeSet, sensor.isSetProperty(ModelRegistry.EP_ENCODINGTYPE));
        Assert.assertEquals(shouldBeSet, sensor.isSetProperty(pluginCoreModel.EP_METADATA));
    }

    @Test
    public void testThing() {
        Entity entity = new DefaultEntity(pluginCoreModel.THING);
        testIsSetPropertyThing(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyThing(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyThing(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyThing(true, true, entity);
    }

    private void testIsSetPropertyThing(boolean shouldBeSet, boolean shouldIdBeSet, Entity thing) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, thing);
    }

    private void testIsSetPropertyNamedEntity(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyAbstractEntity(shouldBeSet, shouldIdBeSet, entity);
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(pluginCoreModel.EP_DESCRIPTION));
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(pluginCoreModel.EP_NAME));
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(ModelRegistry.EP_PROPERTIES));
    }

    private void testIsSetPropertyAbstractEntity(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        Assert.assertEquals("Failed isSet for ID", shouldIdBeSet, entity.isSetProperty(ModelRegistry.EP_ID));
        Assert.assertEquals("Failed isSet for SelfLink", shouldBeSet, entity.isSetProperty(ModelRegistry.EP_SELFLINK));
    }
}
