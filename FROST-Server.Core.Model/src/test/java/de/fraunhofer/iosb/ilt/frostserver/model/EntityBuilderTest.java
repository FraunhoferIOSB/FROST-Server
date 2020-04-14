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
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
        propertyValues.put(NavigationProperty.ACTUATOR, new Actuator(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.DATASTREAM, new Datastream(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.FEATUREOFINTEREST, new FeatureOfInterest(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.LOCATION, new Location(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.MULTIDATASTREAM, new MultiDatastream(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.OBSERVEDPROPERTY, new ObservedProperty(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.SENSOR, new Sensor(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.TASK, new Task(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.TASKINGCAPABILITY, new TaskingCapability(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.THING, new Thing(new IdLong(nextId++)));

        EntitySetImpl<Actuator> actuators = new EntitySetImpl<>(EntityType.ACTUATOR);
        actuators.add(new Actuator(new IdLong(nextId++)));
        actuators.add(new Actuator(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.ACTUATORS, actuators);

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

        EntitySetImpl<Task> tasks = new EntitySetImpl<>(EntityType.TASK);
        tasks.add(new Task(new IdLong(nextId++)));
        tasks.add(new Task(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.TASKS, tasks);

        EntitySetImpl<TaskingCapability> taskingCapabilities = new EntitySetImpl<>(EntityType.TASKINGCAPABILITY);
        taskingCapabilities.add(new TaskingCapability(new IdLong(nextId++)));
        taskingCapabilities.add(new TaskingCapability(new IdLong(nextId++)));
        propertyValues.put(NavigationProperty.TASKINGCAPABILITIES, taskingCapabilities);

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
        }
    }

    private void testEntityType(EntityType type, Set<Property> collectedProperties) {
        String pName = "";
        try {
            Class<? extends Entity> typeClass = type.getImplementingClass();

            Entity entity = typeClass.getDeclaredConstructor().newInstance();
            Entity entity2 = typeClass.getDeclaredConstructor().newInstance();
            for (Property p : collectedProperties) {
                pName = p.toString();
                addPropertyToObject(entity, p);
                Assert.assertNotEquals("Property " + pName + " should influence equals.", entity, entity2);

                addPropertyToObject(entity2, p);
                Assert.assertEquals("Entities should be the same after adding " + pName + " to both.", entity, entity2);

                getPropertyFromObject(entity, p);
            }
        } catch (IllegalAccessException | NoSuchMethodException ex) {
            LOGGER.error("Failed to access property.", ex);
            Assert.fail("Failed to access property " + pName + " on entity of type " + type);
        } catch (InstantiationException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.error("Failed create entity.", ex);
            Assert.fail("Failed create entity: " + ex.getMessage());
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
            if (!(property instanceof NavigationProperty) && !entity.isSetProperty(property)) {
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
