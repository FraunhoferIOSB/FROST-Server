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
package de.fraunhofer.iosb.ilt.frostserver.mcp.resource;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.VALUE_ID_TYPE_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.mcp.TestModelMcp;
import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/**
 * Unit tests for ModelResourceGenerator on the fake model registry: the
 * overview lists only non-admin entity sets, per-type documents carry the
 * key, properties (with the free-form annotation on untyped properties) and
 * navigation properties, and admin-only sets stay invisible.
 */
class ModelResourceGeneratorTest {

    private static ModelResourceGenerator generator;
    private static TestModelMcp testModel;

    @BeforeAll
    static void beforeAll() {
        final CoreSettings coreSettings = new CoreSettings();
        testModel = new TestModelMcp();
        testModel.initModel(coreSettings.getModelRegistry(), VALUE_ID_TYPE_LONG);
        coreSettings.getModelRegistry().initFinalise();
        generator = new ModelResourceGenerator(coreSettings.getModelRegistry());
    }

    @Test
    void testGenerateModel() {
        final JsonNode model = StaJsonHelper.parseOrNull(generator.generateModel());
        assertNotNull(model);
        assertEquals(2, model.get("entities").size(), "Only non-admin entity sets in the overview.");
        assertFalse(generator.generateModel().contains("Safes"), "The admin-only type must not appear anywhere.");
        final String json = generator.generateModel();
        assertTrue(json.contains("House"));
        assertTrue(json.contains("Room"));
    }

    @Test
    void testGenerateEntityTypeDetails() {
        final JsonNode house = StaJsonHelper.parseOrNull(generator.generateEntityType("Houses"));
        assertNotNull(house);
        assertEquals("House", house.get("name").asText());
        assertEquals("Houses", house.get("entitySet").asText());
        assertEquals("@iot.id", house.get("key").asText());
        assertNotNull(house.get("keyType"));

        boolean hasName = false;
        boolean hasUntypedAnnotation = false;
        boolean hasFreeFormProperties = false;
        boolean hasRoomsNavigation = false;
        for (final JsonNode property : house.get("properties")) {
            if ("name".equals(property.get("name").asText())) {
                hasName = true;
                assertEquals(true, property.get("required").asBoolean());
            }
            if ("metadata".equals(property.get("name").asText())) {
                hasUntypedAnnotation = property.has("jsonSchema");
            }
            if ("properties".equals(property.get("name").asText())) {
                hasFreeFormProperties = !property.has("jsonSchema");
            }
        }
        for (final JsonNode navigation : house.get("navigationProperties")) {
            if ("Rooms".equals(navigation.get("name").asText())) {
                hasRoomsNavigation = true;
                assertEquals("Room", navigation.get("target").asText());
                assertEquals("Rooms", navigation.get("targetSet").asText());
                assertEquals(true, navigation.get("isEntitySet").asBoolean());
            }
        }
        assertTrue(hasName, "name property listed.");
        assertTrue(hasUntypedAnnotation, "Edm.Untyped properties carry the jsonSchema annotation.");
        assertTrue(hasFreeFormProperties, "The STA 'properties' free-form object is listed without the untyped annotation.");
        assertTrue(hasRoomsNavigation, "Rooms navigation listed.");
    }

    @Test
    void testGenerateEntityTypeUnknownAndAdmin() {
        assertNull(generator.generateEntityType("Nonesuch"));
        assertNull(generator.generateEntityType("Safes"), "Admin-only entity sets resolve to null for non-admins.");
    }

    @Test
    void testFrostUriConstants() {
        assertEquals("frost://index", FrostResourceHandler.URI_INDEX);
        assertEquals("frost://model", FrostResourceHandler.URI_MODEL);
        assertEquals("frost://model/{entitySet}", FrostResourceHandler.URI_TEMPLATE_MODEL_SET);
        assertEquals("frost://sta/{path}", FrostResourceHandler.URI_TEMPLATE_STA);
        assertEquals("frost://sta/", FrostResourceHandler.URI_PREFIX_STA);
    }

}
