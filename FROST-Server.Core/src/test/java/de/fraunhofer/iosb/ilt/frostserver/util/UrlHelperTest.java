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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
class UrlHelperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlHelperTest.class.getName());

    public UrlHelperTest() {
    }
    private static final String SERVICE_ROOT_URL = "http://example.org/FROST-Server";
    private static final String SERVICE_ROOT_URL_V11 = SERVICE_ROOT_URL + '/' + Version.V_1_1.urlPart;

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static TestModel testModel;
    private static ResourcePath path;

    @BeforeAll
    public static void beforeClass() {
        coreSettings = new CoreSettings();
        modelRegistry = coreSettings.getModelRegistry();
        testModel = new TestModel();
        testModel.initModel(modelRegistry, Constants.VALUE_ID_TYPE_LONG);
        modelRegistry.initFinalise();
        queryDefaults = coreSettings.getQueryDefaults()
                .setAlwaysOrder(true)
                .setUseAbsoluteNavigationLinks(false);
        path = new ResourcePath("http://example.org/FROST-Server", Version.V_1_0, "/");
    }

    @Test
    void testNextLinkDiffOrderIdAuto() {
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 2", 2.0);
        testNextLink(
                coreSettings, house1, house2,
                "/Houses?$top=2",
                "/Houses?$top=2&$skip=2&$skipFilter=(id gt 1)");
        testNextLink(
                coreSettings, house1, house2,
                "/Houses(5)/Rooms?$top=2",
                "/Houses(5)/Rooms?$top=2&$skip=2&$skipFilter=(id gt 1)");
    }

    @Test
    void testNextLinkDiffOrderId() {
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 2", 2.0);
        testNextLink(
                coreSettings, house1, house2,
                "/Houses?$orderby=id&$top=2",
                "/Houses?$orderby=id&$top=2&$skip=2&$skipFilter=(id gt 1)");
        testNextLink(
                coreSettings, house1, house2,
                "/Houses?$orderby=id desc&$top=2",
                "/Houses?$orderby=id desc&$top=2&$skip=2&$skipFilter=(id lt 1)");
    }

    @Test
    void testNextLinkDiffOrderName() {
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 2", 2.0);
        testNextLink(
                coreSettings, house1, house2,
                "/Houses?$orderby=name&$top=2",
                "/Houses?$orderby=name&$top=2&$skip=2&$skipFilter=(name gt 'House 1')");
        testNextLink(
                coreSettings, house1, house2,
                "/Houses?$orderby=name desc&$top=2",
                "/Houses?$orderby=name desc&$top=2&$skip=2&$skipFilter=(name lt 'House 1')");
    }

    @Test
    void testNextLinkSameOrderName() {
        Entity house1 = testModel.createHouse(1, "House 1", 1.0);
        Entity house2 = testModel.createHouse(2, "House 1", 2.0);
        testNextLink(
                coreSettings, house1, house2,
                "/Houses?$orderby=name&$top=2",
                "/Houses?$orderby=name&$top=2&$skip=2&$skipFilter=(name gt 'House 1' or (name eq 'House 1' and id gt 1))");
        testNextLink(
                coreSettings, house1, house2,
                "/Houses?$orderby=name desc&$top=2",
                "/Houses?$orderby=name desc&$top=2&$skip=2&$skipFilter=(name lt 'House 1' or (name eq 'House 1' and id gt 1))");
    }

    private static void testNextLink(CoreSettings settings, Entity last, Entity next, String baseUrl, String expectedNextUrl) {

        Query queryBase = null;
        Query queryExpected = null;
        try {
            queryBase = ParserUtils.parsePathAndQuery(SERVICE_ROOT_URL, Version.V_1_1, baseUrl, settings);
        } catch (IllegalArgumentException e) {
            Assertions.fail("Failed to parse base url: " + baseUrl, e);
        }
        try {
            queryExpected = ParserUtils.parsePathAndQuery(SERVICE_ROOT_URL, Version.V_1_1, expectedNextUrl, settings);
        } catch (IllegalArgumentException e) {
            Assertions.fail("Failed to parse expexted url: " + expectedNextUrl, e);
        }

        probeQuery(queryBase);

        String nextLink = UrlHelper.generateNextLink(queryBase.getPath(), queryBase, queryBase.getTopOrDefault(), last, next);
        nextLink = StringHelper.urlDecode(nextLink).substring(SERVICE_ROOT_URL_V11.length());
        Query nextQuery = null;
        try {
            nextQuery = ParserUtils.parsePathAndQuery(SERVICE_ROOT_URL, Version.V_1_1, nextLink, settings);
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
