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
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.Arrays;
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
    private static PluginMultiDatastream pluginMultiDatastream;

    private final Map<Property, Object> propertyValues = new HashMap<>();
    private final Map<Property, Object> propertyValuesAlternative = new HashMap<>();

    @BeforeClass
    public static void beforeClass() {
        if (queryDefaults == null) {
            coreSettings = new CoreSettings();
            modelRegistry = coreSettings.getModelRegistry();
            queryDefaults = coreSettings.getQueryDefaults();
            queryDefaults.setUseAbsoluteNavigationLinks(false);
            pluginCoreModel = new PluginCoreModel();
            pluginCoreModel.init(coreSettings);
            pluginMultiDatastream = new PluginMultiDatastream();
            pluginMultiDatastream.init(coreSettings);
            coreSettings.getPluginManager().registerPlugin(pluginMultiDatastream);
            coreSettings.getPluginManager().initPlugins(null);
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
        propertyValues.put(pluginMultiDatastream.EP_MULTIOBSERVATIONDATATYPES, Arrays.asList("Type 1", "Type 2"));
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
        UnitOfMeasurement unit1 = new UnitOfMeasurement("unitName", "unitSymbol", "unitDefinition");
        UnitOfMeasurement unit2 = new UnitOfMeasurement("unitName2", "unitSymbol2", "unitDefinition2");
        propertyValues.put(pluginCoreModel.EP_UNITOFMEASUREMENT, unit1);
        propertyValues.put(pluginMultiDatastream.EP_UNITOFMEASUREMENTS, Arrays.asList(unit1, unit2));
        propertyValues.put(pluginCoreModel.EP_VALIDTIME, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"));

        for (EntityPropertyMain ep : modelRegistry.getEntityProperties()) {
            Assert.assertTrue("Missing value for " + ep, propertyValues.containsKey(ep));
        }

        int nextId = 100;
        propertyValues.put(pluginCoreModel.NP_DATASTREAM, new DefaultEntity(pluginCoreModel.DATASTREAM, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_FEATUREOFINTEREST, new DefaultEntity(pluginCoreModel.FEATURE_OF_INTEREST, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_HISTORICALLOCATION, new DefaultEntity(pluginCoreModel.HISTORICAL_LOCATION, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_LOCATION, new DefaultEntity(pluginCoreModel.LOCATION, new IdLong(nextId++)));
        propertyValues.put(pluginMultiDatastream.NP_MULTIDATASTREAM, new DefaultEntity(pluginMultiDatastream.MULTI_DATASTREAM, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_OBSERVATION, new DefaultEntity(pluginCoreModel.OBSERVATION, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_OBSERVEDPROPERTY, new DefaultEntity(pluginCoreModel.OBSERVED_PROPERTY, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_SENSOR, new DefaultEntity(pluginCoreModel.SENSOR, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.NP_THING, new DefaultEntity(pluginCoreModel.THING, new IdLong(nextId++)));

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

        EntitySetImpl multiDatastreams = new EntitySetImpl(pluginMultiDatastream.MULTI_DATASTREAM);
        multiDatastreams.add(new DefaultEntity(pluginMultiDatastream.MULTI_DATASTREAM, new IdLong(nextId++)));
        multiDatastreams.add(new DefaultEntity(pluginMultiDatastream.MULTI_DATASTREAM, new IdLong(nextId++)));
        propertyValues.put(pluginMultiDatastream.NP_MULTIDATASTREAMS, multiDatastreams);

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

    private void testIsSetPropertyDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyAbstractDatastream(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.NP_OBSERVEDPROPERTY);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_UNITOFMEASUREMENT);
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

    private void testIsSetPropertyFeatureOfInterest(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, ModelRegistry.EP_ENCODINGTYPE);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_FEATURE);
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

    private void testIsSetPropertyHistoricalLocation(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyAbstractEntity(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.NP_THING);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_TIME);
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

    private void testIsSetPropertyLocation(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, ModelRegistry.EP_ENCODINGTYPE);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_LOCATION);
    }

    @Test
    public void testMultiDatastream() {
        Entity entity = new DefaultEntity(pluginMultiDatastream.MULTI_DATASTREAM);
        testIsSetPropertyMultiDatastream(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyMultiDatastream(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyMultiDatastream(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyMultiDatastream(true, true, entity);
    }

    private void testIsSetPropertyMultiDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyAbstractDatastream(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginMultiDatastream.EP_MULTIOBSERVATIONDATATYPES);
        testIsSetProperty(shouldBeSet, entity, pluginMultiDatastream.EP_UNITOFMEASUREMENTS);
    }

    private void testIsSetPropertyAbstractDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_OBSERVATIONTYPE);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_OBSERVEDAREA);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_PHENOMENONTIME);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_RESULTTIME);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.NP_SENSOR);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.NP_THING);
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

    private void testIsSetPropertyObservation(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyAbstractEntity(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.NP_DATASTREAM);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.NP_FEATUREOFINTEREST);
        testIsSetProperty(shouldBeSet, entity, pluginMultiDatastream.NP_MULTIDATASTREAM);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_PARAMETERS);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_PHENOMENONTIME);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_RESULT);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_RESULTQUALITY);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_RESULTTIME);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_VALIDTIME);
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

    private void testIsSetPropertyObservedProperty(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_DEFINITION);
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

    private void testIsSetPropertySensor(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, ModelRegistry.EP_ENCODINGTYPE);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_METADATA);
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
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_DESCRIPTION);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.EP_NAME);
        testIsSetProperty(shouldBeSet, entity, ModelRegistry.EP_PROPERTIES);
    }

    private void testIsSetPropertyAbstractEntity(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetProperty(shouldIdBeSet, entity, ModelRegistry.EP_ID);
        testIsSetProperty(shouldBeSet, entity, ModelRegistry.EP_SELFLINK);
    }

    private void testIsSetProperty(boolean shouldBeSet, Entity entity, Property property) {
        Assert.assertEquals(property + " incorrect status on " + entity.getEntityType() + ": " + shouldBeSet, shouldBeSet, entity.isSetProperty(property));
    }
}
