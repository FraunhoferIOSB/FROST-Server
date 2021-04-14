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
package de.fraunhofer.iosb.ilt.frostserver.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
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
            coreSettings.getPluginManager().initPlugins(null);
        }
    }

    @Before
    public void setUp() {
        propertyValues.put(pluginCoreModel.epCreationTime, TimeInstant.now());
        propertyValues.put(pluginCoreModel.epDefinition, "MyDefinition");
        propertyValues.put(pluginCoreModel.epDescription, "My description");
        propertyValues.put(ModelRegistry.EP_ENCODINGTYPE, "My EncodingType");
        propertyValues.put(pluginCoreModel.epFeature, new Point(8, 42));
        propertyValues.put(ModelRegistry.EP_ID, new IdLong(1));
        propertyValues.put(pluginCoreModel.epLocation, new Point(9, 43));
        propertyValues.put(pluginCoreModel.epMetadata, "my meta data");
        propertyValues.put(pluginCoreModel.epName, "myName");
        propertyValues.put(pluginCoreModel.epObservationType, "my Type");
        propertyValues.put(pluginCoreModel.epObservedArea, new Polygon(new LngLatAlt(0, 0), new LngLatAlt(1, 0), new LngLatAlt(1, 1)));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", 2);
        propertyValues.put(pluginCoreModel.epParameters, parameters);
        propertyValues.put(pluginCoreModel.epPhenomenonTime, TimeInstant.now());
        propertyValuesAlternative.put(pluginCoreModel.epPhenomenonTime, TimeInterval.parse("2014-03-02T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(ModelRegistry.EP_PROPERTIES, parameters);
        propertyValues.put(pluginCoreModel.epResult, 42);
        propertyValues.put(pluginCoreModel.epResultQuality, "myQuality");
        propertyValues.put(pluginCoreModel.epResultTime, TimeInstant.now());
        propertyValuesAlternative.put(pluginCoreModel.epResultTime, TimeInterval.parse("2014-03-01T13:00:00Z/2014-05-11T15:30:00Z"));
        propertyValues.put(ModelRegistry.EP_SELFLINK, "http://my.self/link");
        propertyValues.put(pluginCoreModel.epTime, TimeInstant.now());
        UnitOfMeasurement unit1 = new UnitOfMeasurement("unitName", "unitSymbol", "unitDefinition");
        UnitOfMeasurement unit2 = new UnitOfMeasurement("unitName2", "unitSymbol2", "unitDefinition2");
        propertyValues.put(pluginCoreModel.epUnitOfMeasurement, unit1);
        propertyValues.put(pluginCoreModel.epValidTime, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"));

        int nextId = 100;
        propertyValues.put(pluginCoreModel.npDatastream, new DefaultEntity(pluginCoreModel.etDatastream, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npFeatureOfInterest, new DefaultEntity(pluginCoreModel.etFeatureOfInterest, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npHistoricalLocation, new DefaultEntity(pluginCoreModel.etHistoricalLocation, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npLocation, new DefaultEntity(pluginCoreModel.etLocation, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npObservation, new DefaultEntity(pluginCoreModel.etObservation, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npObservedProperty, new DefaultEntity(pluginCoreModel.etObservedProperty, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npSensor, new DefaultEntity(pluginCoreModel.etSensor, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npThing, new DefaultEntity(pluginCoreModel.etThing, new IdLong(nextId++)));

        EntitySetImpl datastreams = new EntitySetImpl(pluginCoreModel.etDatastream);
        datastreams.add(new DefaultEntity(pluginCoreModel.etDatastream, new IdLong(nextId++)));
        datastreams.add(new DefaultEntity(pluginCoreModel.etDatastream, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npDatastreams, datastreams);

        EntitySetImpl features = new EntitySetImpl(pluginCoreModel.etFeatureOfInterest);
        features.add(new DefaultEntity(pluginCoreModel.etFeatureOfInterest, new IdLong(nextId++)));
        features.add(new DefaultEntity(pluginCoreModel.etFeatureOfInterest, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npFeaturesOfInterest, features);

        EntitySetImpl histLocations = new EntitySetImpl(pluginCoreModel.etHistoricalLocation);
        histLocations.add(new DefaultEntity(pluginCoreModel.etHistoricalLocation, new IdLong(nextId++)));
        histLocations.add(new DefaultEntity(pluginCoreModel.etHistoricalLocation, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npHistoricalLocations, histLocations);

        EntitySetImpl locations = new EntitySetImpl(pluginCoreModel.etLocation);
        locations.add(new DefaultEntity(pluginCoreModel.etLocation, new IdLong(nextId++)));
        locations.add(new DefaultEntity(pluginCoreModel.etLocation, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npLocations, locations);

        EntitySetImpl observations = new EntitySetImpl(pluginCoreModel.etObservation);
        observations.add(new DefaultEntity(pluginCoreModel.etObservation, new IdLong(nextId++)));
        observations.add(new DefaultEntity(pluginCoreModel.etObservation, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npObservations, observations);

        EntitySetImpl obsProperties = new EntitySetImpl(pluginCoreModel.etObservedProperty);
        obsProperties.add(new DefaultEntity(pluginCoreModel.etObservedProperty, new IdLong(nextId++)));
        obsProperties.add(new DefaultEntity(pluginCoreModel.etObservedProperty, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npObservedProperties, obsProperties);

        EntitySetImpl sensors = new EntitySetImpl(pluginCoreModel.etSensor);
        sensors.add(new DefaultEntity(pluginCoreModel.etSensor, new IdLong(nextId++)));
        sensors.add(new DefaultEntity(pluginCoreModel.etSensor, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npSensors, sensors);

        EntitySetImpl things = new EntitySetImpl(pluginCoreModel.etThing);
        things.add(new DefaultEntity(pluginCoreModel.etThing, new IdLong(nextId++)));
        things.add(new DefaultEntity(pluginCoreModel.etThing, new IdLong(nextId++)));
        propertyValues.put(pluginCoreModel.npThings, things);

        for (EntityType entityType : modelRegistry.getEntityTypes()) {
            for (EntityPropertyMain ep : entityType.getEntityProperties()) {
                Assert.assertTrue("Missing value for " + ep, propertyValues.containsKey(ep));
            }
        }

        for (EntityType entityType : modelRegistry.getEntityTypes()) {
            for (NavigationPropertyMain np : entityType.getNavigationEntities()) {
                Assert.assertTrue("Missing value for " + np, propertyValues.containsKey(np));
            }
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
        Entity entity = new DefaultEntity(pluginCoreModel.etDatastream);
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
        Assert.assertEquals(shouldBeSet, datastream.isSetProperty(pluginCoreModel.npObservedProperty));
        Assert.assertEquals(shouldBeSet, datastream.isSetProperty(pluginCoreModel.epUnitOfMeasurement));
    }

    @Test
    public void testFeatureOfInterest() {
        Entity entity = new DefaultEntity(pluginCoreModel.etFeatureOfInterest);
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
        Assert.assertEquals(shouldBeSet, featureOfInterest.isSetProperty(pluginCoreModel.epFeature));
    }

    @Test
    public void testHistoricalLocation() {
        Entity entity = new DefaultEntity(pluginCoreModel.etHistoricalLocation);
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
        Assert.assertEquals(shouldBeSet, hl.isSetProperty(pluginCoreModel.npThing));
        Assert.assertEquals(shouldBeSet, hl.isSetProperty(pluginCoreModel.epTime));
    }

    @Test
    public void testLocation() {
        Entity entity = new DefaultEntity(pluginCoreModel.etLocation);
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
        Assert.assertEquals(shouldBeSet, location.isSetProperty(pluginCoreModel.epLocation));
    }

    private void testIsSetPropertyAbstractDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Entity mds) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, mds);
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.epObservationType));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.epObservedArea));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.epPhenomenonTime));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.epResultTime));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.npSensor));
        Assert.assertEquals(shouldBeSet, mds.isSetProperty(pluginCoreModel.npThing));
    }

    @Test
    public void testObservation() {
        Entity entity = new DefaultEntity(pluginCoreModel.etObservation);
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
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.npDatastream));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.npFeatureOfInterest));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.epParameters));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.epPhenomenonTime));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.epResult));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.epResultQuality));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.epResultTime));
        Assert.assertEquals(shouldBeSet, o.isSetProperty(pluginCoreModel.epValidTime));
    }

    @Test
    public void testObservedProperty() {
        Entity entity = new DefaultEntity(pluginCoreModel.etObservedProperty);
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
        Assert.assertEquals(shouldBeSet, op.isSetProperty(pluginCoreModel.epDefinition));
    }

    @Test
    public void testSensor() {
        Entity entity = new DefaultEntity(pluginCoreModel.etSensor);
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
        Assert.assertEquals(shouldBeSet, sensor.isSetProperty(pluginCoreModel.epMetadata));
    }

    @Test
    public void testThing() {
        Entity entity = new DefaultEntity(pluginCoreModel.etThing);
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
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(pluginCoreModel.epDescription));
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(pluginCoreModel.epName));
        Assert.assertEquals(shouldBeSet, entity.isSetProperty(ModelRegistry.EP_PROPERTIES));
    }

    private void testIsSetPropertyAbstractEntity(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        Assert.assertEquals("Failed isSet for ID", shouldIdBeSet, entity.isSetProperty(ModelRegistry.EP_ID));
        Assert.assertEquals("Failed isSet for SelfLink", shouldBeSet, entity.isSetProperty(ModelRegistry.EP_SELFLINK));
    }
}
