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

import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.ACTUATOR;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.DATASTREAM;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.FEATURE_OF_INTEREST;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.HISTORICAL_LOCATION;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.LOCATION;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.MULTI_DATASTREAM;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.OBSERVATION;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.OBSERVED_PROPERTY;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.SENSOR;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.TASK;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.TASKING_CAPABILITY;
import static de.fraunhofer.iosb.ilt.frostserver.model.EntityType.THING;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.Assert;
import org.junit.Before;
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
    private final Map<Property, Object> propertyValues = new HashMap<>();
    private final Map<Property, Object> propertyValuesAlternative = new HashMap<>();

    @Before
    public void setUp() {
        propertyValues.put(EntityPropertyMain.CREATIONTIME, TimeInstant.now());
        propertyValues.put(EntityPropertyMain.DEFINITION, "MyDefinition");
        propertyValues.put(EntityPropertyMain.DESCRIPTION, "My description");
        propertyValues.put(EntityPropertyMain.ENCODINGTYPE, "My EncodingType");
        propertyValues.put(EntityPropertyMain.FEATURE, new Point(8, 42));
        propertyValues.put(EntityPropertyMain.ID, new IdLong(1));
        propertyValues.put(EntityPropertyMain.LOCATION, new Point(9, 43));
        propertyValues.put(EntityPropertyMain.METADATA, "my meta data");
        propertyValues.put(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, Arrays.asList("Type 1", "Type 2"));
        propertyValues.put(EntityPropertyMain.NAME, "myName");
        propertyValues.put(EntityPropertyMain.OBSERVATIONTYPE, "my Type");
        propertyValues.put(EntityPropertyMain.OBSERVEDAREA, new Polygon(new LngLatAlt(0, 0), new LngLatAlt(1, 0), new LngLatAlt(1, 1)));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValues.put(EntityPropertyMain.PARAMETERS, parameters);
        propertyValues.put(EntityPropertyMain.PHENOMENONTIME, TimeInstant.now());
        propertyValuesAlternative.put(EntityPropertyMain.PHENOMENONTIME, TimeInterval.parse("2014-03-02T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(EntityPropertyMain.PROPERTIES, parameters);
        propertyValues.put(EntityPropertyMain.RESULT, 42);
        propertyValues.put(EntityPropertyMain.RESULTQUALITY, "myQuality");
        propertyValues.put(EntityPropertyMain.RESULTTIME, TimeInstant.now());
        propertyValuesAlternative.put(EntityPropertyMain.RESULTTIME, TimeInterval.parse("2014-03-01T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(EntityPropertyMain.SELFLINK, "http://my.self/link");
        propertyValues.put(EntityPropertyMain.TIME, TimeInstant.now());
        propertyValues.put(EntityPropertyMain.TASKINGPARAMETERS, parameters);
        UnitOfMeasurement unit1 = new UnitOfMeasurement("unitName", "unitSymbol", "unitDefinition");
        UnitOfMeasurement unit2 = new UnitOfMeasurement("unitName2", "unitSymbol2", "unitDefinition2");
        propertyValues.put(EntityPropertyMain.UNITOFMEASUREMENT, unit1);
        propertyValues.put(EntityPropertyMain.UNITOFMEASUREMENTS, Arrays.asList(unit1, unit2));
        propertyValues.put(EntityPropertyMain.VALIDTIME, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"));

        for (EntityPropertyMain ep : EntityPropertyMain.values()) {
            Assert.assertTrue("Missing value for " + ep, propertyValues.containsKey(ep));
        }

        int nextId = 100;
        propertyValues.put(NavigationPropertyMain.ACTUATOR, new DefaultEntity(ACTUATOR, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.DATASTREAM, new DefaultEntity(DATASTREAM, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.FEATUREOFINTEREST, new DefaultEntity(FEATURE_OF_INTEREST, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.LOCATION, new DefaultEntity(LOCATION, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.MULTIDATASTREAM, new DefaultEntity(MULTI_DATASTREAM, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.OBSERVEDPROPERTY, new DefaultEntity(OBSERVED_PROPERTY, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.SENSOR, new DefaultEntity(SENSOR, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.TASK, new DefaultEntity(TASK, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.TASKINGCAPABILITY, new DefaultEntity(TASKING_CAPABILITY, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.THING, new DefaultEntity(THING, new IdLong(nextId++)));

        EntitySetImpl actuators = new EntitySetImpl(ACTUATOR);
        actuators.add(new DefaultEntity(ACTUATOR, new IdLong(nextId++)));
        actuators.add(new DefaultEntity(ACTUATOR, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.ACTUATORS, actuators);

        EntitySetImpl datastreams = new EntitySetImpl(DATASTREAM);
        datastreams.add(new DefaultEntity(DATASTREAM, new IdLong(nextId++)));
        datastreams.add(new DefaultEntity(DATASTREAM, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.DATASTREAMS, datastreams);

        EntitySetImpl histLocations = new EntitySetImpl(HISTORICAL_LOCATION);
        histLocations.add(new DefaultEntity(HISTORICAL_LOCATION, new IdLong(nextId++)));
        histLocations.add(new DefaultEntity(HISTORICAL_LOCATION, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.HISTORICALLOCATIONS, histLocations);

        EntitySetImpl locations = new EntitySetImpl(LOCATION);
        locations.add(new DefaultEntity(LOCATION, new IdLong(nextId++)));
        locations.add(new DefaultEntity(LOCATION, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.LOCATIONS, locations);

        EntitySetImpl multiDatastreams = new EntitySetImpl(MULTI_DATASTREAM);
        multiDatastreams.add(new DefaultEntity(MULTI_DATASTREAM, new IdLong(nextId++)));
        multiDatastreams.add(new DefaultEntity(MULTI_DATASTREAM, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.MULTIDATASTREAMS, multiDatastreams);

        EntitySetImpl observations = new EntitySetImpl(OBSERVATION);
        observations.add(new DefaultEntity(OBSERVATION, new IdLong(nextId++)));
        observations.add(new DefaultEntity(OBSERVATION, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.OBSERVATIONS, observations);

        EntitySetImpl obsProperties = new EntitySetImpl(OBSERVED_PROPERTY);
        obsProperties.add(new DefaultEntity(OBSERVED_PROPERTY, new IdLong(nextId++)));
        obsProperties.add(new DefaultEntity(OBSERVED_PROPERTY, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.OBSERVEDPROPERTIES, obsProperties);

        EntitySetImpl tasks = new EntitySetImpl(EntityType.TASK);
        tasks.add(new DefaultEntity(TASK, new IdLong(nextId++)));
        tasks.add(new DefaultEntity(TASK, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.TASKS, tasks);

        EntitySetImpl taskingCapabilities = new EntitySetImpl(TASKING_CAPABILITY);
        taskingCapabilities.add(new DefaultEntity(TASKING_CAPABILITY, new IdLong(nextId++)));
        taskingCapabilities.add(new DefaultEntity(TASKING_CAPABILITY, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.TASKINGCAPABILITIES, taskingCapabilities);

        EntitySetImpl things = new EntitySetImpl(EntityType.THING);
        things.add(new DefaultEntity(THING, new IdLong(nextId++)));
        things.add(new DefaultEntity(THING, new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.THINGS, things);

        for (NavigationPropertyMain np : NavigationPropertyMain.values()) {
            Assert.assertTrue("Missing value for " + np, propertyValues.containsKey(np));
        }

    }

    @Test
    public void testEntityBuilders() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (EntityType type : EntityType.getEntityTypes()) {
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
        Entity entity = new DefaultEntity(DATASTREAM);
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
        Assert.assertEquals(shouldBeSet, datastream.isSetProperty(NavigationPropertyMain.OBSERVEDPROPERTY));
        Assert.assertEquals(shouldBeSet, datastream.isSetProperty(EntityPropertyMain.UNITOFMEASUREMENT));
    }

    @Test
    public void testFeatureOfInterest() {
        Entity entity = new DefaultEntity(FEATURE_OF_INTEREST);
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
        Assert.assertEquals(shouldBeSet, featureOfInterest.isSetProperty(EntityPropertyMain.ENCODINGTYPE));
        Assert.assertEquals(shouldBeSet, featureOfInterest.isSetProperty(EntityPropertyMain.FEATURE));
    }

    @Test
    public void testHistoricalLocation() {
        Entity entity = new DefaultEntity(HISTORICAL_LOCATION);
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
        Assert.assertEquals(shouldBeSet, hl.isSetProperty(NavigationPropertyMain.THING));
        Assert.assertEquals(shouldBeSet, hl.isSetProperty(EntityPropertyMain.TIME));
    }

    @Test
    public void testLocation() {
        Entity entity = new DefaultEntity(LOCATION);
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
        Assert.assertEquals(shouldBeSet, location.isSetProperty(EntityPropertyMain.ENCODINGTYPE));
        Assert.assertEquals(shouldBeSet, location.isSetProperty(EntityPropertyMain.LOCATION));
    }

    @Test
    public void testMultiDatastream() {
        Entity entity = new DefaultEntity(MULTI_DATASTREAM);
        testIsSetPropertyMultiDatastream(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyMultiDatastream(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyMultiDatastream(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyMultiDatastream(true, true, entity);
    }

    private void testIsSetPropertyMultiDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Entity mds) {
        testIsSetPropertyAbstractDatastream(shouldBeSet, shouldIdBeSet, mds);
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(EntityPropertyMain.UNITOFMEASUREMENTS));
    }

    private void testIsSetPropertyAbstractDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Entity mds) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, mds);
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(EntityPropertyMain.OBSERVATIONTYPE));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(EntityPropertyMain.OBSERVEDAREA));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(EntityPropertyMain.PHENOMENONTIME));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(EntityPropertyMain.RESULTTIME));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(NavigationPropertyMain.SENSOR));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(NavigationPropertyMain.THING));
    }

    @Test
    public void testObservation() {
        Entity entity = new DefaultEntity(OBSERVATION);
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
        Assert.assertEquals(shouldBeSet, o.isSetProperty(NavigationPropertyMain.DATASTREAM));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(NavigationPropertyMain.FEATUREOFINTEREST));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(NavigationPropertyMain.MULTIDATASTREAM));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(EntityPropertyMain.PARAMETERS));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(EntityPropertyMain.PHENOMENONTIME));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(EntityPropertyMain.RESULT));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(EntityPropertyMain.RESULTQUALITY));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(EntityPropertyMain.RESULTTIME));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(EntityPropertyMain.VALIDTIME));
    }

    @Test
    public void testObservedProperty() {
        Entity entity = new DefaultEntity(OBSERVED_PROPERTY);
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
        Assert.assertEquals(shouldBeSet, op.isSetProperty(EntityPropertyMain.DEFINITION));
    }

    @Test
    public void testSensor() {
        Entity entity = new DefaultEntity(SENSOR);
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
        Assert.assertEquals(shouldBeSet, sensor.isSetProperty(EntityPropertyMain.ENCODINGTYPE));
        Assert.assertEquals(shouldBeSet, sensor.isSetProperty(EntityPropertyMain.METADATA));
    }

    @Test
    public void testThing() {
        Entity entity = new DefaultEntity(THING);
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
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(EntityPropertyMain.DESCRIPTION));
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(EntityPropertyMain.NAME));
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(EntityPropertyMain.PROPERTIES));
    }

    private void testIsSetPropertyAbstractEntity(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        Assert.assertEquals(shouldIdBeSet, entity.isSetProperty(EntityPropertyMain.ID));
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(EntityPropertyMain.SELFLINK));
    }
}
