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
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private static ModelRegistry modelRegistry;
    private static TestModel testModel;

    private Map<EntityType, Map<Property, Object>> propertyValues;

    @BeforeClass
    public static void beforeClass() {
        modelRegistry = new ModelRegistry();
        testModel = new TestModel();
        testModel.initModel(modelRegistry);
        modelRegistry.initFinalise();
    }

    @Before
    public void setUp() {
        propertyValues = testModel.getTestPropertyValues(modelRegistry);
    }

    @Test
    public void testEntityBuilders() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (EntityType type : modelRegistry.getEntityTypes()) {
            testEntityType(type, type.getPropertySet());
        }
    }

    private void testEntityType(EntityType type, Set<Property> collectedProperties) {
        String pName;
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
        addPropertyToObject(entity, property, propertyValues.get(entity.getEntityType()));
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
            Object value = propertyValues.get(entity.getEntityType()).get(property);
            Object setValue = property.getFrom(entity);

            if (!Objects.equals(value, setValue)) {
                Assert.fail("Getter did not return set value for property " + property + " on entity type " + entity.getEntityType());
            }
        } catch (SecurityException | IllegalArgumentException ex) {
            LOGGER.error("Failed to set property", ex);
            Assert.fail("Failed to set property: " + ex.getMessage());
        }
    }

}
