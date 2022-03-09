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
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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
    private static ModelRegistry modelRegistry;
    private static TestModel testModel;

    private Map<EntityType, Map<Property, Object>> propertyValues;

    @BeforeAll
    public static void beforeClass() {
        modelRegistry = new ModelRegistry();
        testModel = new TestModel();
        testModel.initModel(modelRegistry, Constants.VALUE_ID_TYPE_LONG);
        modelRegistry.initFinalise();
    }

    @BeforeEach
    public void setUp() {
        propertyValues = testModel.getTestPropertyValues(modelRegistry);

        for (EntityType et : modelRegistry.getEntityTypes()) {
            assertTrue(propertyValues.containsKey(et), "Missing values for " + et);
            final Map<Property, Object> propertValuesEt = propertyValues.get(et);
            for (EntityPropertyMain ep : et.getEntityProperties()) {
                assertTrue(propertValuesEt.containsKey(ep), "Missing value for " + ep);
            }
            for (NavigationPropertyMain np : et.getNavigationProperties()) {
                assertTrue(propertValuesEt.containsKey(np), "Missing value for " + np);
            }
        }
    }

    @Test
    void testEntityBuilders() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
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
            fail("Failed to access property: " + ex.getMessage());
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
                fail("Diff claims that Property: " + entity.getEntityType() + "/" + p + " did not change.");
            }
            if (!shouldBeChanged && changedFields.contains(p)) {
                fail("Diff claims that Property: " + entity.getEntityType() + "/" + p + " did change.");
            }
            isSetPropertyOnObject(entity, p, shouldBeChanged);
        }
    }

    private void addPropertyToObject(Entity entity, Property property) throws NoSuchMethodException {
        addPropertyToObject(entity, property, propertyValues.get(entity.getEntityType()));
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

}
