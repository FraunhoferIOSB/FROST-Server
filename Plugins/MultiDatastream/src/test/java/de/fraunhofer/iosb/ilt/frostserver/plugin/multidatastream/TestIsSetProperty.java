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
package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.MdsModelSettings.TAG_ENABLE_MDS_MODEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private static EntityType etMultiDatastream;
    private static EntityPropertyMain epMultiObservationDataTypes;
    private static EntityPropertyMain epUnitOfMeasurements;
    private static NavigationPropertyEntity npMultiDatastreamObservation;
    private static NavigationPropertyEntity npThingMds;
    private static NavigationPropertyEntity npSensorMds;
    private static NavigationPropertyEntitySet npObservedPropertiesMds;
    private static NavigationPropertyEntitySet npMultiDatastreamsSensor;
    private static NavigationPropertyEntitySet npMultiDatastreamsObsProp;
    private static NavigationPropertyEntitySet npMultiDatastreamsThing;

    private final Map<Property, Object> propertyValues = new HashMap<>();

    @BeforeAll
    public static void beforeClass() {
        if (queryDefaults == null) {
            coreSettings = new CoreSettings();
            coreSettings.getSettings().getProperties().put("plugins." + TAG_ENABLE_MDS_MODEL, "true");
            modelRegistry = coreSettings.getModelRegistry();
            queryDefaults = coreSettings.getQueryDefaults();
            queryDefaults.setUseAbsoluteNavigationLinks(false);
            pluginCoreModel = new PluginCoreModel();
            pluginCoreModel.init(coreSettings);
            pluginMultiDatastream = new PluginMultiDatastream();
            pluginMultiDatastream.init(coreSettings);
            coreSettings.getPluginManager().initPlugins(null);
            etMultiDatastream = modelRegistry.getEntityTypeForName("MultiDatastream");
            epMultiObservationDataTypes = etMultiDatastream.getEntityProperty("multiObservationDataTypes");
            epUnitOfMeasurements = etMultiDatastream.getEntityProperty("unitOfMeasurements");

            npThingMds = (NavigationPropertyEntity) etMultiDatastream.getNavigationProperty("Thing");
            npSensorMds = (NavigationPropertyEntity) etMultiDatastream.getNavigationProperty("Sensor");
            npObservedPropertiesMds = (NavigationPropertyEntitySet) etMultiDatastream.getNavigationProperty("ObservedProperties");

            npMultiDatastreamObservation = (NavigationPropertyEntity) pluginCoreModel.etObservation.getNavigationProperty("MultiDatastream");
            npMultiDatastreamsThing = (NavigationPropertyEntitySet) pluginCoreModel.etThing.getNavigationProperty("MultiDatastreams");
            npMultiDatastreamsSensor = (NavigationPropertyEntitySet) pluginCoreModel.etSensor.getNavigationProperty("MultiDatastreams");
            npMultiDatastreamsObsProp = (NavigationPropertyEntitySet) pluginCoreModel.etObservedProperty.getNavigationProperty("MultiDatastreams");
        }
    }

    @BeforeEach
    public void setUp() {
        TestHelper.generateDefaultValues(propertyValues, pluginCoreModel, modelRegistry);
    }

    @Test
    void testEntityBuilders() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (EntityType type : modelRegistry.getEntityTypes()) {
            Set<Property> properties = type.getPropertySet().stream().filter(t -> !t.isReadOnly()).collect(Collectors.toSet());
            testEntityType(type, properties);
            testEntityCompare(type, properties);
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
            if (p == ModelRegistry.EP_SELFLINK) {
                continue;
            }
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
            fail("Failed to access property: " + ex.getMessage());
        }
    }

    private void testPropertiesChanged(EntityChangedMessage message, Set<Property> collectedProperties, Entity entity, boolean shouldBeChanged) {
        Set<Property> changedFields = message.getFields();
        for (Property p : collectedProperties) {
            if (p == ModelRegistry.EP_SELFLINK) {
                continue;
            }
            if (p instanceof NavigationPropertyMain) {
                NavigationPropertyMain nProp = (NavigationPropertyMain) p;
                if (nProp.isEntitySet()) {
                    continue;
                }
            }
            if (shouldBeChanged && !changedFields.contains(p)) {
                fail("Diff claims that Property: " + entity.getEntityType() + "/" + p + " did not change.");
            }
            if (!shouldBeChanged && changedFields.contains(p)) {
                fail("Diff claims that Property: " + entity.getEntityType() + "/" + p + " did change.");
            }
            isSetPropertyOnObject(entity, p, shouldBeChanged);
        }
    }

    private void addPropertyToObject(Entity entity, Property property) throws NoSuchMethodException {
        addPropertyToObject(entity, property, propertyValues);
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
                fail("Property " + property + " returned false for isSet on entity type " + entity.getEntityType());
            }
        } catch (SecurityException | IllegalArgumentException ex) {
            LOGGER.error("Failed to set property", ex);
            fail("Failed to set property: " + ex.getMessage());
        }
    }

    @Test
    void testDatastream() {
        Entity entity = new DefaultEntity(pluginCoreModel.etDatastream);
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
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.npObservedPropertyDatastream);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.getEpUnitOfMeasurement());
    }

    @Test
    void testFeatureOfInterest() {
        Entity entity = new DefaultEntity(pluginCoreModel.etFeatureOfInterest);
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
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epFeature);
    }

    @Test
    void testHistoricalLocation() {
        Entity entity = new DefaultEntity(pluginCoreModel.etHistoricalLocation);
        testIsSetPropertyHistoricalLocation(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyHistoricalLocation(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyHistoricalLocation(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyHistoricalLocation(true, true, entity);
    }

    private void testIsSetPropertyHistoricalLocation(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyAbstractEntity(shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.npThingHistLoc);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epTime);
    }

    @Test
    void testLocation() {
        Entity entity = new DefaultEntity(pluginCoreModel.etLocation);
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
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epLocation);
    }

    @Test
    void testMultiDatastream() {
        Entity entity = new DefaultEntity(etMultiDatastream);
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
        testIsSetProperty(shouldBeSet, entity, epMultiObservationDataTypes);
        testIsSetProperty(shouldBeSet, entity, epUnitOfMeasurements);
    }

    private void testIsSetPropertyAbstractDatastream(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyNamedEntity(shouldBeSet, shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epObservationType);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.npSensorDatastream);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.npThingDatasteam);
    }

    @Test
    void testObservation() {
        Entity entity = new DefaultEntity(pluginCoreModel.etObservation);
        testIsSetPropertyObservation(false, false, entity);

        entity.setEntityPropertiesSet();
        testIsSetPropertyObservation(true, true, entity);

        entity.setEntityPropertiesSet(false, false);
        testIsSetPropertyObservation(false, false, entity);

        entity.setEntityPropertiesSet(true, false);
        testIsSetPropertyObservation(true, true, entity);
    }

    private void testIsSetPropertyObservation(boolean shouldBeSet, boolean shouldIdBeSet, Entity entity) {
        testIsSetPropertyAbstractEntity(shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.npDatastreamObservation);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.npFeatureOfInterestObservation);
        testIsSetProperty(shouldBeSet, entity, npMultiDatastreamObservation);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epParameters);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epPhenomenonTime);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epResult);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epResultQuality);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epResultTime);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epValidTime);
    }

    @Test
    void testObservedProperty() {
        Entity entity = new DefaultEntity(pluginCoreModel.etObservedProperty);
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
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epDefinition);
    }

    @Test
    void testSensor() {
        Entity entity = new DefaultEntity(pluginCoreModel.etSensor);
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
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epMetadata);
    }

    @Test
    void testThing() {
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
        testIsSetPropertyAbstractEntity(shouldIdBeSet, entity);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epDescription);
        testIsSetProperty(shouldBeSet, entity, pluginCoreModel.epName);
        testIsSetProperty(shouldBeSet, entity, ModelRegistry.EP_PROPERTIES);
    }

    private void testIsSetPropertyAbstractEntity(boolean shouldIdBeSet, Entity entity) {
        testIsSetProperty(shouldIdBeSet, entity, entity.getEntityType().getPrimaryKey().getKeyProperty(0));
        testIsSetProperty(true, entity, ModelRegistry.EP_SELFLINK);
    }

    private void testIsSetProperty(boolean shouldBeSet, Entity entity, Property property) {
        assertEquals(shouldBeSet, entity.isSetProperty(property), property + " incorrect status on " + entity.getEntityType() + ": " + shouldBeSet);
    }
}
