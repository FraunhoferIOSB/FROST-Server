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
package de.fraunhofer.iosb.ilt.frostserver.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.DefaultFunctions;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UrlHelperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlHelperTest.class.getName());

    private static final String SERVICE_ROOT_URL = "http://example.org/FROST-Server";

    private static CoreSettings coreSettings;
    private static ServiceContext context;
    private static TestModel testModel;

    @BeforeAll
    static void beforeClass() {
        coreSettings = new CoreSettings();
        DefaultFunctions.registerDefaultFunctions(coreSettings.getFunctionRegistry());
        coreSettings.getQueryDefaults()
                .setAlwaysOrder(true)
                .setUseAbsoluteNavigationLinks(false)
                .setServiceRootUrl(SERVICE_ROOT_URL);
        context = new ServiceContext()
                .setModelRegistry(coreSettings.getModelRegistry())
                .setFunctionRegistry(coreSettings.getFunctionRegistry())
                .setQueryDefaults(coreSettings.getQueryDefaults())
                .setPrefixGen(() -> "");
        testModel = new TestModel();
        testModel.initModel(context.getModelRegistry(), Constants.VALUE_ID_TYPE_LONG);
        context.getModelRegistry().initFinalise();
    }

    @Test
    void testPathsetThings() {
        String path = "/Rooms";
        ResourcePath result = PathParser.parsePath(context, Version.INTERNAL, path);

        ResourcePath expResult = new ResourcePath(Version.INTERNAL, path);
        PathElementEntitySet espe = new PathElementEntitySet(testModel.etRoom);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);

        assertEquals(expResult, result);
    }

    @Test
    void testPathThing() {
        assertThrows(IllegalArgumentException.class, () -> {
            String path = "/Room";
            PathParser.parsePath(context, Version.INTERNAL, path);
        });
    }

    @Test
    void testPathsetThingsRef() {
        String path = "/Houses/$ref";
        ResourcePath result = PathParser.parsePath(context, Version.INTERNAL, path);

        ResourcePath expResult = new ResourcePath(Version.INTERNAL, path);
        PathElementEntitySet espe = new PathElementEntitySet(testModel.etHouse);
        expResult.addPathElement(espe, true, false);
        expResult.setMainElement(espe);
        expResult.setRef(true);

        assertEquals(expResult, result);
    }

    @Test
    void testSelfLink() {
        ResourcePath path = new ResourcePath(Version.INTERNAL, "Houses");
        path.addPathElement(new PathElementEntitySet(testModel.etHouse), true, false);

        Query query = new Query(context, path);
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        house1.setQuery(query);

        String selfLink = house1.getSelfLink();
        assertEquals("Houses(1)", selfLink);
    }

    @Test
    void testNextLink() {
        ResourcePath path = new ResourcePath(Version.INTERNAL, "Houses");
        path.addPathElement(new PathElementEntitySet(testModel.etHouse), true, false);

        Query query = new Query(context, path);
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 2", 2.0);

        String nextLink = UrlHelper.generateNextLink(path, query, 10, house1, house2);
        assertEquals("Houses?$skip=10", nextLink);
    }

    @Test
    void testNextLinkDiffOrderIdAuto() {
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 2", 2.0);
        testNextLink(
                coreSettings, house1, house2,
                "Houses?$top=2",
                "Houses?$top=2&$skip=2&$skipFilter=(id gt 1)");
        testNextLink(
                coreSettings, house1, house2,
                "Houses(5)/Rooms?$top=2",
                "Houses(5)/Rooms?$top=2&$skip=2&$skipFilter=(id gt 1)");
    }

    @Test
    void testNextLinkDiffOrderId() {
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 2", 2.0);
        testNextLink(
                coreSettings, house1, house2,
                "Houses?$orderby=id&$top=2",
                "Houses?$orderby=id&$top=2&$skip=2&$skipFilter=(id gt 1)");
        testNextLink(
                coreSettings, house1, house2,
                "Houses?$orderby=id desc&$top=2",
                "Houses?$orderby=id desc&$top=2&$skip=2&$skipFilter=(id lt 1)");
    }

    @Test
    void testNextLinkDiffOrderName() {
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 2", 2.0);
        testNextLink(
                coreSettings, house1, house2,
                "Houses?$orderby=name&$top=2",
                "Houses?$orderby=name&$top=2&$skip=2&$skipFilter=(name gt 'House 1')");
        testNextLink(
                coreSettings, house1, house2,
                "Houses?$orderby=name desc&$top=2",
                "Houses?$orderby=name desc&$top=2&$skip=2&$skipFilter=(name lt 'House 1')");
    }

    @Test
    void testNextLinkSameOrderName() {
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 1", 2.0);
        testNextLink(
                coreSettings, house1, house2,
                "Houses?$orderby=name&$top=2",
                "Houses?$orderby=name&$top=2&$skip=2&$skipFilter=(name gt 'House 1' or (name eq 'House 1' and id gt 1))");
        testNextLink(
                coreSettings, house1, house2,
                "Houses?$orderby=name desc&$top=2",
                "Houses?$orderby=name desc&$top=2&$skip=2&$skipFilter=(name lt 'House 1' or (name eq 'House 1' and id gt 1))");
    }

    @Test
    void testNextLinkSameOrderNameValue() {
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 1", 1.0);
        testNextLink(
                coreSettings, house1, house2,
                "Houses?$orderby=name,value&$top=2",
                "Houses?$orderby=name,value&$top=2&$skip=2&$skipFilter=(name gt 'House 1' or (name eq 'House 1' and (value gt 1.0 or (value eq 1.0 and id gt 1))))");
        testNextLink(
                coreSettings, house1, house2,
                "Houses?$orderby=name desc,value&$top=2",
                "Houses?$orderby=name desc,value&$top=2&$skip=2&$skipFilter=(name lt 'House 1' or (name eq 'House 1' and (value gt 1.0 or (value eq 1.0 and id gt 1))))");
    }

    @Test
    void testNormaliseQuery() {
        String pathAndQuery1 = "Houses?$select=name,value&$expand=Rooms($select=name,time;$orderby=time)";
        String pathAndQuery2 = "Houses?$expand=Rooms($select=time,name;$orderby=time)&$select=value,name";

        String normal1 = normalise(pathAndQuery1);
        String normal2 = normalise(pathAndQuery2);
        assertEquals(normal1, normal2);
    }

    private String normalise(String pathAndQuery) {
        Query query = PathParser.parsePathAndQuery(Version.INTERNAL, pathAndQuery, context);
        query.normalise();
        return query.getPath().toString() + "?" + query.toString(false);
    }

    private static void testNextLink(CoreSettings settings, Entity last, Entity next, String baseUrl, String expectedNextUrl) {
        Query queryBase = null;
        Query queryExpected = null;
        try {
            queryBase = PathParser.parsePathAndQuery(Version.INTERNAL, baseUrl, context);
        } catch (IllegalArgumentException e) {
            Assertions.fail("Failed to parse base url: " + baseUrl, e);
        }
        try {
            queryExpected = PathParser.parsePathAndQuery(Version.INTERNAL, expectedNextUrl, context);
        } catch (IllegalArgumentException e) {
            Assertions.fail("Failed to parse expexted url: " + expectedNextUrl, e);
        }

        probeQuery(queryBase);

        String nextLink = UrlHelper.generateNextLink(queryBase.getPath(), queryBase, queryBase.getTopOrDefault(), last, next);
        nextLink = StringHelper.urlDecode(nextLink);
        Query nextQuery = null;
        try {
            nextQuery = PathParser.parsePathAndQuery(Version.INTERNAL, nextLink, context);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed for base url {}", baseUrl);
            LOGGER.error("Expected nextLink   {}", expectedNextUrl);
            LOGGER.error("Got nextLink        {}", nextLink);
            Assertions.fail("Failed to parse generated next link: " + nextLink, e);
        }

        assertEquals(queryExpected, nextQuery);
    }

    private static void probeQuery(Query query) {
        for (Expand expand : query.getExpand()) {
            probeQuery(expand.getSubQuery());
        }
    }
}
