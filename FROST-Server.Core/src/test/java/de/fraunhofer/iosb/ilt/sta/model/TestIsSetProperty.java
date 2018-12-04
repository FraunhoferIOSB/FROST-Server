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
package de.fraunhofer.iosb.ilt.sta.model;

import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.core.IdLong;
import de.fraunhofer.iosb.ilt.sta.model.core.NamedEntity;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
    private Map<Property, Object> propertyValues = new HashMap<>();
    private Map<Property, Object> propertyValuesAlternative = new HashMap<>();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        propertyValues.put(EntityProperty.DEFINITION, "MyDefinition");
        propertyValues.put(EntityProperty.DESCRIPTION, "My description");
        propertyValues.put(EntityProperty.ENCODINGTYPE, "My EncodingType");
        propertyValues.put(EntityProperty.FEATURE, new Point(8, 42));
        propertyValues.put(EntityProperty.ID, new IdLong(1));
        propertyValues.put(EntityProperty.LOCATION, new Point(9, 43));
        propertyValues.put(EntityProperty.METADATA, "my meta data");
        propertyValues.put(EntityProperty.MULTIOBSERVATIONDATATYPES, Arrays.asList("Type 1", "Type 2"));
        propertyValues.put(EntityProperty.NAME, "myName");
        propertyValues.put(EntityProperty.OBSERVATIONTYPE, "my Type");
        propertyValues.put(EntityProperty.OBSERVEDAREA, new Polygon(new LngLatAlt(0, 0), new LngLatAlt(1, 0), new LngLatAlt(1, 1)));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValues.put(EntityProperty.PARAMETERS, parameters);
        propertyValues.put(EntityProperty.PHENOMENONTIME, TimeInstant.now());
        propertyValuesAlternative.put(EntityProperty.PHENOMENONTIME, TimeInterval.parse("2014-03-02T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(EntityProperty.PROPERTIES, parameters);
        propertyValues.put(EntityProperty.RESULT, 42);
        propertyValues.put(EntityProperty.RESULTQUALITY, "myQuality");
        propertyValues.put(EntityProperty.RESULTTIME, TimeInstant.now());
        propertyValuesAlternative.put(EntityProperty.RESULTTIME, TimeInterval.parse("2014-03-01T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(EntityProperty.SELFLINK, "http://my.self/link");
        propertyValues.put(EntityProperty.TIME, TimeInstant.now());
        UnitOfMeasurement unit1 = new UnitOfMeasurement("unitName", "unitSymbol", "unitDefinition");
        UnitOfMeasurement unit2 = new UnitOfMeasurement("unitName2", "unitSymbol2", "unitDefinition2");
        propertyValues.put(EntityProperty.UNITOFMEASUREMENT, unit1);
        propertyValues.put(EntityProperty.UNITOFMEASUREMENTS, Arrays.asList(unit1, unit2));
        propertyValues.put(EntityProperty.VALIDTIME, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"));

        for (EntityProperty ep : EntityProperty.values()) {
            Assert.assertTrue("Missing value for " + ep, propertyValues.containsKey(ep));
        }

        int nextId = 100;
        propertyValues.put(NavigationProperty.DATASTREAM, new Datastream(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.FEATUREOFINTEREST, new FeatureOfInterest(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.LOCATION, new Location(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.MULTIDATASTREAM, new MultiDatastream(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.OBSERVEDPROPERTY, new ObservedProperty(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.SENSOR, new Sensor(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.THING, new Thing(new IdLong(nextId++)));

        EntitySetImpl<Datastream> datastreams = new EntitySetImpl<>(EntityType.DATASTREAM);
        datastreams.add(new Datastream(new IdLong(nextId++)));
        datastreams.add(new Datastream(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.DATASTREAMS, datastreams);

        EntitySetImpl<HistoricalLocation> histLocations = new EntitySetImpl<>(EntityType.HISTORICALLOCATION);
        histLocations.add(new HistoricalLocation(new IdLong(nextId++)));
        histLocations.add(new HistoricalLocation(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.HISTORICALLOCATIONS, histLocations);

        EntitySetImpl<Location> locations = new EntitySetImpl<>(EntityType.LOCATION);
        locations.add(new Location(new IdLong(nextId++)));
        locations.add(new Location(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.LOCATIONS, locations);

        EntitySetImpl<MultiDatastream> multiDatastreams = new EntitySetImpl<>(EntityType.MULTIDATASTREAM);
        multiDatastreams.add(new MultiDatastream(new IdLong(nextId++)));
        multiDatastreams.add(new MultiDatastream(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.MULTIDATASTREAMS, multiDatastreams);

        EntitySetImpl<Observation> observations = new EntitySetImpl<>(EntityType.OBSERVATION);
        observations.add(new Observation(new IdLong(nextId++)));
        observations.add(new Observation(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.OBSERVATIONS, observations);

        EntitySetImpl<ObservedProperty> obsProperties = new EntitySetImpl<>(EntityType.OBSERVEDPROPERTY);
        obsProperties.add(new ObservedProperty(new IdLong(nextId++)));
        obsProperties.add(new ObservedProperty(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.OBSERVEDPROPERTIES, obsProperties);

        EntitySetImpl<Thing> things = new EntitySetImpl<>(EntityType.THING);
        things.add(new Thing(new IdLong(nextId++)));
        things.add(new Thing(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.THINGS, things);

        for (NavigationProperty np : NavigationProperty.values()) {
            Assert.assertTrue("Missing value for " + np, propertyValues.containsKey(np));
        }

    }

    @Test
    public void testEntityBuilders() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (EntityType type : EntityType.values()) {
            testEntityType(type, type.getPropertySet());
            testEntityCompare(type, type.getPropertySet());
        }
    }

    private void testEntityType(EntityType type, Set<Property> collectedProperties) {
        try {
            Class<? extends Entity> typeClass = type.getImplementingClass();
            Entity entity = typeClass.newInstance();
            entity.setEntityPropertiesSet();
            for (Property p : collectedProperties) {
                isSetPropertyOnObject(entity, p, true);
            }
            entity.setEntityPropertiesSet(false);
            for (Property p : collectedProperties) {
                isSetPropertyOnObject(entity, p, false);
            }
            entity.setEntityPropertiesSet(true);
            for (Property p : collectedProperties) {
                isSetPropertyOnObject(entity, p, true);
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            LOGGER.error("Failed to access property.", ex);
            Assert.fail("Failed to access property: " + ex.getMessage());
        }
    }

    private void testEntityCompare(EntityType type, Set<Property> collectedProperties) {
        try {
            Class<? extends Entity> typeClass = type.getImplementingClass();

            Entity entity = typeClass.newInstance();
            for (Property p : collectedProperties) {
                addPropertyToObject(entity, p);
            }
            Entity entityEmpty = typeClass.newInstance();

            EntityChangedMessage message = new EntityChangedMessage();
            entityEmpty.setEntityPropertiesSet(entity, message);
            testPropertiesChanged(message, collectedProperties, entity, true);

            message = new EntityChangedMessage();
            entityEmpty.setEntityPropertiesSet(entityEmpty, message);
            testPropertiesChanged(message, collectedProperties, entityEmpty, false);

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
            LOGGER.error("Failed to access property.", ex);
            Assert.fail("Failed to access property: " + ex.getMessage());
        }
    }

    private void testPropertiesChanged(EntityChangedMessage message, Set<Property> collectedProperties, Entity entity, boolean shouldBeChanged) {
        Set<Property> changedFields = message.getFields();
        for (Property p : collectedProperties) {
            if (p instanceof NavigationProperty) {
                NavigationProperty nProp = (NavigationProperty) p;
                if (nProp.isSet) {
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

    private void addPropertyToObject(Object entity, Property property) throws NoSuchMethodException {
        try {
            addPropertyToObject(entity, property, propertyValues);
        } catch (NoSuchMethodException ex) {
            addPropertyToObject(entity, property, propertyValuesAlternative);
        }
    }

    private void addPropertyToObject(Object entity, Property property, Map<Property, Object> valuesToUse) throws NoSuchMethodException {
        Object value = valuesToUse.get(property);
        try {
            final String setterName = property.getSetterName();
            MethodUtils.invokeMethod(entity, setterName, value);
        } catch (NullPointerException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.error("Failed to set property " + property, ex);
            Assert.fail("Failed to set property " + property + ": " + ex.getMessage());
        }
    }

    private void isSetPropertyOnObject(Entity entity, Property property, boolean shouldBeSet) {
        try {
            if (property instanceof NavigationProperty) {
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
        Datastream entity = new Datastream();
        testIsSetPropertyDatastream(false, true, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyDatastream(true, true, entity);

        entity.setEntityPropertiesSet(false);
        testIsSetPropertyDatastream(false, false, entity);

        entity.setEntityPropertiesSet(true);
        testIsSetPropertyDatastream(true, true, entity);
    }

    private void testIsSetPropertyDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Datastream datastream) {
        testIsSetPropertyAbstractDatastream(shouldBeSet, shouldIdBeSet, datastream);
        Assert.assertEquals(shouldBeSet, datastream.isSetObservedProperty());
        Assert.assertEquals(shouldBeSet, datastream.isSetUnitOfMeasurement());
    }

    @Test
    public void testFeatureOfInterest() {
        FeatureOfInterest entity = new FeatureOfInterest();
        testIsSetPropertyFeatureOfInterest(false, true, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyFeatureOfInterest(true, true, entity);

        entity.setEntityPropertiesSet(false);
        testIsSetPropertyFeatureOfInterest(false, false, entity);

        entity.setEntityPropertiesSet(true);
        testIsSetPropertyFeatureOfInterest(true, true, entity);
    }

    private void testIsSetPropertyFeatureOfInterest(boolean shouldBeSet, boolean shouldIdBeSet, FeatureOfInterest featureOfInterest) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, featureOfInterest);
        Assert.assertEquals(shouldBeSet, featureOfInterest.isSetEncodingType());
        Assert.assertEquals(shouldBeSet, featureOfInterest.isSetFeature());
    }

    @Test
    public void testHistoricalLocation() {
        HistoricalLocation entity = new HistoricalLocation();
        testIsSetPropertyHistoricalLocation(false, true, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyHistoricalLocation(true, true, entity);

        entity.setEntityPropertiesSet(false);
        testIsSetPropertyHistoricalLocation(false, false, entity);

        entity.setEntityPropertiesSet(true);
        testIsSetPropertyHistoricalLocation(true, true, entity);
    }

    private void testIsSetPropertyHistoricalLocation(boolean shouldBeSet, boolean shouldIdBeSet, HistoricalLocation hl) {
        testIsSetPropertyAbstractEntity(shouldBeSet, shouldIdBeSet, hl);
        Assert.assertEquals(shouldBeSet, hl.isSetThing());
        Assert.assertEquals(shouldBeSet, hl.isSetTime());
    }

    @Test
    public void testLocation() {
        Location entity = new Location();
        testIsSetPropertyLocation(false, true, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyLocation(true, true, entity);

        entity.setEntityPropertiesSet(false);
        testIsSetPropertyLocation(false, false, entity);

        entity.setEntityPropertiesSet(true);
        testIsSetPropertyLocation(true, true, entity);
    }

    private void testIsSetPropertyLocation(boolean shouldBeSet, boolean shouldIdBeSet, Location location) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, location);
        Assert.assertEquals(shouldBeSet, location.isSetEncodingType());
        Assert.assertEquals(shouldBeSet, location.isSetLocation());
    }

    @Test
    public void testMultiDatastream() {
        MultiDatastream entity = new MultiDatastream();
        testIsSetPropertyMultiDatastream(false, true, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyMultiDatastream(true, true, entity);

        entity.setEntityPropertiesSet(false);
        testIsSetPropertyMultiDatastream(false, false, entity);

        entity.setEntityPropertiesSet(true);
        testIsSetPropertyMultiDatastream(true, true, entity);
    }

    private void testIsSetPropertyMultiDatastream(boolean shouldBeSet, boolean shouldIdBeSet, MultiDatastream mds) {
        testIsSetPropertyAbstractDatastream(shouldBeSet, shouldIdBeSet, mds);
        Assert.assertEquals(shouldBeSet, mds.isSetMultiObservationDataTypes());
        Assert.assertEquals(shouldBeSet, mds.isSetObservedProperties());
        Assert.assertEquals(shouldBeSet, mds.isSetUnitOfMeasurements());
    }

    private void testIsSetPropertyAbstractDatastream(boolean shouldBeSet, boolean shouldIdBeSet, AbstractDatastream mds) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, mds);
        Assert.assertEquals(shouldBeSet, mds.isSetObservationType());
        Assert.assertEquals(shouldBeSet, mds.isSetObservedArea());
        Assert.assertEquals(shouldBeSet, mds.isSetPhenomenonTime());
        Assert.assertEquals(shouldBeSet, mds.isSetResultTime());
        Assert.assertEquals(shouldBeSet, mds.isSetSensor());
        Assert.assertEquals(shouldBeSet, mds.isSetThing());
    }

    @Test
    public void testObservation() {
        Observation entity = new Observation();
        testIsSetPropertyObservation(false, true, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyObservation(true, true, entity);

        entity.setEntityPropertiesSet(false);
        testIsSetPropertyObservation(false, false, entity);

        entity.setEntityPropertiesSet(true);
        testIsSetPropertyObservation(true, true, entity);
    }

    private void testIsSetPropertyObservation(boolean shouldBeSet, boolean shouldIdBeSet, Observation o) {
        testIsSetPropertyAbstractEntity(shouldBeSet, shouldIdBeSet, o);
        Assert.assertEquals(shouldBeSet, o.isSetDatastream());
        Assert.assertEquals(shouldBeSet, o.isSetFeatureOfInterest());
        Assert.assertEquals(shouldBeSet, o.isSetMultiDatastream());
        Assert.assertEquals(shouldBeSet, o.isSetParameters());
        Assert.assertEquals(shouldBeSet, o.isSetPhenomenonTime());
        Assert.assertEquals(shouldBeSet, o.isSetResult());
        Assert.assertEquals(shouldBeSet, o.isSetResultQuality());
        Assert.assertEquals(shouldBeSet, o.isSetResultTime());
        Assert.assertEquals(shouldBeSet, o.isSetValidTime());
    }

    @Test
    public void testObservedProperty() {
        ObservedProperty entity = new ObservedProperty();
        testIsSetPropertyObservedProperty(false, true, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyObservedProperty(true, true, entity);

        entity.setEntityPropertiesSet(false);
        testIsSetPropertyObservedProperty(false, false, entity);

        entity.setEntityPropertiesSet(true);
        testIsSetPropertyObservedProperty(true, true, entity);
    }

    private void testIsSetPropertyObservedProperty(boolean shouldBeSet, boolean shouldIdBeSet, ObservedProperty op) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, op);
        Assert.assertEquals(shouldBeSet, op.isSetDefinition());
    }

    @Test
    public void testSensor() {
        Sensor entity = new Sensor();
        testIsSetPropertySensor(false, true, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertySensor(true, true, entity);

        entity.setEntityPropertiesSet(false);
        testIsSetPropertySensor(false, false, entity);

        entity.setEntityPropertiesSet(true);
        testIsSetPropertySensor(true, true, entity);
    }

    private void testIsSetPropertySensor(boolean shouldBeSet, boolean shouldIdBeSet, Sensor sensor) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, sensor);
        Assert.assertEquals(shouldBeSet, sensor.isSetEncodingType());
        Assert.assertEquals(shouldBeSet, sensor.isSetMetadata());
    }

    @Test
    public void testThing() {
        Thing entity = new Thing();
        testIsSetPropertyThing(false, true, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyThing(true, true, entity);

        entity.setEntityPropertiesSet(false);
        testIsSetPropertyThing(false, false, entity);

        entity.setEntityPropertiesSet(true);
        testIsSetPropertyThing(true, true, entity);
    }

    private void testIsSetPropertyThing(boolean shouldBeSet, boolean shouldIdBeSet, Thing thing) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, thing);
    }

    private void testIsSetPropertyNamedEntity(boolean shouldBeSet, boolean shouldIdBeSet, NamedEntity entity) {
        testIsSetPropertyAbstractEntity(shouldBeSet, shouldIdBeSet, entity);
        Assert.assertEquals(shouldBeSet, entity.isSetDescription());
        Assert.assertEquals(shouldBeSet, entity.isSetName());
        Assert.assertEquals(shouldBeSet, entity.isSetProperties());
    }

    private void testIsSetPropertyAbstractEntity(boolean shouldBeSet, boolean shouldIdBeSet, AbstractEntity entity) {
        Assert.assertEquals(shouldIdBeSet, entity.isSetId());
        Assert.assertEquals(shouldBeSet, entity.isSetSelfLink());
    }
}
