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
package de.fraunhofer.iosb.ilt.frostserver.query;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementArrayIndex;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementCustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.TestModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class PathParserTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static TestModel testModel;

    @BeforeAll
    public static void beforeClass() {
        coreSettings = new CoreSettings();
        modelRegistry = coreSettings.getModelRegistry();
        queryDefaults = coreSettings.getQueryDefaults()
                .setUseAbsoluteNavigationLinks(false);
        testModel = new TestModel();
        testModel.initModel(modelRegistry, Constants.VALUE_ID_TYPE_LONG);
        modelRegistry.initFinalise();
    }

    @Test
    void testPathsetRooms() {
        String path = "/Rooms";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_ROOM);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);

        assertEquals(expResult, result);
    }

    @Test
    void testPathsetRoomsRef() {
        String path = "/Rooms/$ref";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_ROOM);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);
        expResult.setRef(true);

        assertEquals(expResult, result);
    }

    @Test
    void testPathsetHouses() {
        String path = "/Houses";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_HOUSE);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);

        assertEquals(expResult, result);
    }

    @Test
    void testIdentifiers() {
        testHouse(0);
        testHouse(1);
        testHouse(-1);
        testHouse(Long.MAX_VALUE);
        testHouse(Long.MIN_VALUE);
    }

    @Test
    void testPathEntityProperty() {
        for (EntityType entityType : modelRegistry.getEntityTypes()) {
            for (Property property : entityType.getPropertySet()) {
                String basePath = "/" + entityType.plural + "(1)/";
                String path = basePath + property.getName();
                ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

                ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
                PathElementEntitySet espe = new PathElementEntitySet(entityType);
                expResult.addPathElement(espe, false, false);
                PathElementEntity epe = new PathElementEntity(new IdLong(1), entityType, espe);
                expResult.addPathElement(epe, true, true);

                if (property instanceof EntityPropertyMain) {
                    EntityPropertyMain entityProperty = (EntityPropertyMain) property;
                    PathElementProperty ppe = new PathElementProperty(entityProperty, epe);
                    expResult.addPathElement(ppe, false, false);

                    assertEquals(expResult, result, "Failed on " + entityType + " - " + property);
                }
                if (property instanceof NavigationPropertyEntitySet) {
                    NavigationPropertyEntitySet nps = (NavigationPropertyEntitySet) property;
                    PathElementEntitySet espe2 = new PathElementEntitySet(nps, epe);
                    expResult.addPathElement(espe2, false, false);

                    assertEquals(expResult, result, "Failed on " + entityType + " - " + property);
                }
            }
        }
    }

    @Test
    void testPathEntityHousePropertyValue() {
        String path = "/Houses(1)/properties/$value";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_HOUSE);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(1), testModel.ET_HOUSE, espe);
        expResult.addPathElement(epe, true, true);
        PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
        expResult.addPathElement(ppe, false, false);
        expResult.setValue(true);

        assertEquals(expResult, result);
    }

    @Test
    void testPathEntityHouseSubProperty() {
        {
            String path = "/Houses(1)/properties/property1";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_HOUSE);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), testModel.ET_HOUSE, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            assertEquals(expResult, result);
        }
        {
            String path = "/Houses(1)/properties/name_two";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_HOUSE);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), testModel.ET_HOUSE, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("name_two", ppe);
            expResult.addPathElement(cppe, false, false);
            assertEquals(expResult, result);
        }
        {
            String path = "/Houses(1)/properties/property1[2]";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_HOUSE);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), testModel.ET_HOUSE, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
            expResult.addPathElement(cpai, false, false);
            assertEquals(expResult, result);
        }
        {
            String path = "/Houses(1)/properties/property1[2][3]";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_HOUSE);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), testModel.ET_HOUSE, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
            expResult.addPathElement(cpai, false, false);
            cpai = new PathElementArrayIndex(3, cpai);
            expResult.addPathElement(cpai, false, false);
            assertEquals(expResult, result);
        }
        {
            String path = "/Houses(1)/properties/property1[2]/deep[3]";
            ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
            ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
            PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_HOUSE);
            expResult.addPathElement(espe, false, false);
            PathElementEntity epe = new PathElementEntity(new IdLong(1), testModel.ET_HOUSE, espe);
            expResult.addPathElement(epe, true, true);
            PathElementProperty ppe = new PathElementProperty(ModelRegistry.EP_PROPERTIES, epe);
            expResult.addPathElement(ppe, false, false);
            PathElementCustomProperty cppe = new PathElementCustomProperty("property1", ppe);
            expResult.addPathElement(cppe, false, false);
            PathElementArrayIndex cpai = new PathElementArrayIndex(2, cppe);
            expResult.addPathElement(cpai, false, false);
            cppe = new PathElementCustomProperty("deep", cpai);
            expResult.addPathElement(cppe, false, false);
            cpai = new PathElementArrayIndex(3, cppe);
            expResult.addPathElement(cpai, false, false);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testPathIllegal1() {
        assertThrows(IllegalArgumentException.class, () -> {
            String path = "/Rooms(1)/Houses";
            PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        });
    }

    @Test
    void testPathIllegal2() {
        assertThrows(IllegalArgumentException.class, () -> {
            String path = "/Room";
            PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            String path = "/Room(1)";
            PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        });
    }

    @Test
    void testPathIllegal3() {
        assertThrows(IllegalArgumentException.class, () -> {
            String path = "/Rooms/House";
            PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);
        });
    }

    private void testHouse(long id) {
        String path = "/Houses(" + id + ")";
        ResourcePath result = PathParser.parsePath(modelRegistry, "", Version.V_1_1, path);

        ResourcePath expResult = new ResourcePath("", Version.V_1_1, path);
        PathElementEntitySet espe = new PathElementEntitySet(testModel.ET_HOUSE);
        expResult.addPathElement(espe, false, false);
        PathElementEntity epe = new PathElementEntity(new IdLong(id), testModel.ET_HOUSE, espe);
        expResult.addPathElement(epe, true, true);

        assertEquals(expResult, result);
    }

}
