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

import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.DatastreamBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.FeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.HistoricalLocationBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.LocationBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.MultiDatastreamBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.ObservationBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.ObservedPropertyBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.SensorBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.ThingBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.reflect.MethodUtils;
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
public class EntityBuilderTest {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBuilderTest.class);
    private final Map<Property, Object> propertyValues = new HashMap<>();
    private final Map<Property, Object> propertyValuesAlternative = new HashMap<>();

    @Before
    public void setUp() {
        propertyValues.put(EntityProperty.CREATIONTIME, TimeInstant.now());
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
        propertyValues.put(EntityProperty.TASKINGPARAMETERS, parameters);
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
        propertyValues.put(NavigationPropertyMain.ACTUATOR, new Actuator(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.DATASTREAM, new Datastream(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.FEATUREOFINTEREST, new FeatureOfInterest(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.LOCATION, new Location(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.MULTIDATASTREAM, new MultiDatastream(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.OBSERVEDPROPERTY, new ObservedProperty(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.SENSOR, new Sensor(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.TASK, new Task(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.TASKINGCAPABILITY, new TaskingCapability(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.THING, new Thing(new IdLong(nextId++)));

        EntitySetImpl<Actuator> actuators = new EntitySetImpl<>(EntityType.ACTUATOR);
        actuators.add(new Actuator(new IdLong(nextId++)));
        actuators.add(new Actuator(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.ACTUATORS, actuators);

        EntitySetImpl<Datastream> datastreams = new EntitySetImpl<>(EntityType.DATASTREAM);
        datastreams.add(new Datastream(new IdLong(nextId++)));
        datastreams.add(new Datastream(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.DATASTREAMS, datastreams);

        EntitySetImpl<HistoricalLocation> histLocations = new EntitySetImpl<>(EntityType.HISTORICALLOCATION);
        histLocations.add(new HistoricalLocation(new IdLong(nextId++)));
        histLocations.add(new HistoricalLocation(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.HISTORICALLOCATIONS, histLocations);

        EntitySetImpl<Location> locations = new EntitySetImpl<>(EntityType.LOCATION);
        locations.add(new Location(new IdLong(nextId++)));
        locations.add(new Location(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.LOCATIONS, locations);

        EntitySetImpl<MultiDatastream> multiDatastreams = new EntitySetImpl<>(EntityType.MULTIDATASTREAM);
        multiDatastreams.add(new MultiDatastream(new IdLong(nextId++)));
        multiDatastreams.add(new MultiDatastream(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.MULTIDATASTREAMS, multiDatastreams);

        EntitySetImpl<Observation> observations = new EntitySetImpl<>(EntityType.OBSERVATION);
        observations.add(new Observation(new IdLong(nextId++)));
        observations.add(new Observation(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.OBSERVATIONS, observations);

        EntitySetImpl<ObservedProperty> obsProperties = new EntitySetImpl<>(EntityType.OBSERVEDPROPERTY);
        obsProperties.add(new ObservedProperty(new IdLong(nextId++)));
        obsProperties.add(new ObservedProperty(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.OBSERVEDPROPERTIES, obsProperties);

        EntitySetImpl<Task> tasks = new EntitySetImpl<>(EntityType.TASK);
        tasks.add(new Task(new IdLong(nextId++)));
        tasks.add(new Task(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.TASKS, tasks);

        EntitySetImpl<TaskingCapability> taskingCapabilities = new EntitySetImpl<>(EntityType.TASKINGCAPABILITY);
        taskingCapabilities.add(new TaskingCapability(new IdLong(nextId++)));
        taskingCapabilities.add(new TaskingCapability(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.TASKINGCAPABILITIES, taskingCapabilities);

        EntitySetImpl<Thing> things = new EntitySetImpl<>(EntityType.THING);
        things.add(new Thing(new IdLong(nextId++)));
        things.add(new Thing(new IdLong(nextId++)));
        propertyValues.put(NavigationPropertyMain.THINGS, things);

        for (NavigationPropertyMain np : NavigationPropertyMain.values()) {
            Assert.assertTrue("Missing value for " + np, propertyValues.containsKey(np));
        }

    }

    @Test
    public void testEntityBuilders() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (EntityType type : EntityType.values()) {
            testEntityType(type, type.getPropertySet());
        }
    }

    private void testEntityType(EntityType type, Set<Property> collectedProperties) {
        String pName = "";
        try {
            Class<? extends Entity> typeClass = type.getImplementingClass();
            String builderName = "de.fraunhofer.iosb.ilt.frostserver.model.builder." + typeClass.getSimpleName() + "Builder";
            Class<?> builderClass = getClass().getClassLoader().loadClass(builderName);

            Entity entity = typeClass.newInstance();
            Object builder = builderClass.newInstance();
            for (Property p : collectedProperties) {
                pName = p.toString();
                addPropertyToObject(entity, p);
                Assert.assertNotEquals("Property " + pName + " should influence equals.", entity, buildBuilder(builder));

                addPropertyToObject(builder, p);
                Entity buildEntity = (Entity) buildBuilder(builder);
                Assert.assertEquals("Entities should be the same after adding " + pName + " to both.", entity, buildEntity);

                getPropertyFromObject(entity, p);
                getPropertyFromObject(buildEntity, p);
            }
        } catch (IllegalAccessException | NoSuchMethodException ex) {
            LOGGER.error("Failed to access property.", ex);
            Assert.fail("Failed to access property " + pName + " on entity of type " + type);
        } catch (ClassNotFoundException | InstantiationException ex) {
            LOGGER.error("Failed create builder.", ex);
            Assert.fail("Failed create builder: " + ex.getMessage());
        }
    }

    private Object buildBuilder(Object builder) {
        try {
            return builder.getClass().getMethod("build").invoke(builder);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Assert.fail("Failed to build: " + ex.getMessage());
            LOGGER.error("Failed to Build", ex);
        }
        return null;
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

    private void getPropertyFromObject(Entity entity, Property property) {
        try {
            if (!(property instanceof NavigationPropertyMain) && !entity.isSetProperty(property)) {
                Assert.fail("Property " + property + " returned false for isSet on entity type " + entity.getEntityType());
            }
            Object value = propertyValues.get(property);
            Object value2 = propertyValuesAlternative.get(property);
            Method getter = entity.getClass().getMethod(property.getGetterName());
            Object setValue = getter.invoke(entity);

            if (!(Objects.equals(value, setValue) || Objects.equals(value2, setValue))) {
                Assert.fail("Getter did not return set value for property " + property + " on entity type " + entity.getEntityType());
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.error("Failed to set property", ex);
            Assert.fail("Failed to set property: " + ex.getMessage());
        }
    }

    @Test
    public void testDatastream() {
        Datastream expected = new Datastream();
        Datastream builder = new DatastreamBuilder().build();
        Assert.assertEquals(expected, builder);

        IdLong myId = new IdLong(1);
        expected = new Datastream(myId);
        builder = new DatastreamBuilder().setId(myId).build();
        Assert.assertEquals(expected, builder);

        expected = new Datastream(false, myId);
        builder = new DatastreamBuilder().setId(myId).setUnitOfMeasurement(new UnitOfMeasurement()).build();
        Assert.assertEquals(expected, builder);
    }

    @Test
    public void testFeatureOfInterest() {
        FeatureOfInterest expected = new FeatureOfInterest();
        FeatureOfInterest builder = new FeatureOfInterestBuilder().build();
        Assert.assertEquals(expected, builder);

        IdLong myId = new IdLong(1);
        expected = new FeatureOfInterest(myId);
        builder = new FeatureOfInterestBuilder().setId(myId).build();
        Assert.assertEquals(expected, builder);
    }

    @Test
    public void testHistoricalLocation() {
        HistoricalLocation expected = new HistoricalLocation();
        HistoricalLocation builder = new HistoricalLocationBuilder().build();
        Assert.assertEquals(expected, builder);

        IdLong myId = new IdLong(1);
        expected = new HistoricalLocation(myId);
        builder = new HistoricalLocationBuilder().setId(myId).build();
        Assert.assertEquals(expected, builder);
    }

    @Test
    public void testLocation() {
        Location expected = new Location();
        Location builder = new LocationBuilder().build();
        Assert.assertEquals(expected, builder);

        IdLong myId = new IdLong(1);
        expected = new Location(myId);
        builder = new LocationBuilder().setId(myId).build();
        Assert.assertEquals(expected, builder);
    }

    @Test
    public void testMultiDatastream() {
        MultiDatastream expected = new MultiDatastream();
        MultiDatastream builder = new MultiDatastreamBuilder().build();
        Assert.assertEquals(expected, builder);

        IdLong myId = new IdLong(1);
        expected = new MultiDatastream(myId);
        builder = new MultiDatastreamBuilder().setId(myId).build();
        Assert.assertEquals(expected, builder);
    }

    @Test
    public void testObservation() {
        Observation expected = new Observation();
        Observation builder = new ObservationBuilder().build();
        Assert.assertEquals(expected, builder);

        IdLong myId = new IdLong(1);
        expected = new Observation(myId);
        builder = new ObservationBuilder().setId(myId).build();
        Assert.assertEquals(expected, builder);
    }

    @Test
    public void testObservedProperty() {
        ObservedProperty expected = new ObservedProperty();
        ObservedProperty builder = new ObservedPropertyBuilder().build();
        Assert.assertEquals(expected, builder);

        IdLong myId = new IdLong(1);
        expected = new ObservedProperty(myId);
        builder = new ObservedPropertyBuilder().setId(myId).build();
        Assert.assertEquals(expected, builder);
    }

    @Test
    public void testSensor() {
        Sensor expected = new Sensor();
        Sensor builder = new SensorBuilder().build();
        Assert.assertEquals(expected, builder);

        IdLong myId = new IdLong(1);
        expected = new Sensor(myId);
        builder = new SensorBuilder().setId(myId).build();
        Assert.assertEquals(expected, builder);
    }

    @Test
    public void testThing() {
        Thing expected = new Thing();
        Thing builder = new ThingBuilder().build();
        Assert.assertEquals(expected, builder);

        IdLong myId = new IdLong(1);
        expected = new Thing(myId);
        builder = new ThingBuilder().setId(myId).build();
        Assert.assertEquals(expected, builder);
    }
}
