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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.ParserUtils;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
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
    private static PluginCoreModel pluginCoreModel;

    private static CoreSettings coreSettingsString;
    private static QueryDefaults queryDefaultsString;
    private static ModelRegistry modelRegistryString;
    private static PluginCoreModel pluginCoreModelString;

    @BeforeAll
    public static void beforeClass() {
        coreSettings = new CoreSettings();
        modelRegistry = coreSettings.getModelRegistry();
        queryDefaults = coreSettings.getQueryDefaults();
        queryDefaults.setUseAbsoluteNavigationLinks(false);
        pluginCoreModel = new PluginCoreModel();
        pluginCoreModel.init(coreSettings);
        coreSettings.getPluginManager().initPlugins(null);

        coreSettingsString = new CoreSettings();
        coreSettingsString.getPluginSettings().set("coreModel.idType", Constants.VALUE_ID_TYPE_STRING);
        modelRegistryString = coreSettingsString.getModelRegistry();
        queryDefaultsString = coreSettingsString.getQueryDefaults();
        queryDefaultsString.setUseAbsoluteNavigationLinks(false);
        pluginCoreModelString = new PluginCoreModel();
        pluginCoreModelString.init(coreSettingsString);
        coreSettingsString.getPluginManager().initPlugins(null);
    }

    @Test
    void testNextLinkTop() {
        testNextLink(
                coreSettings,
                "/Things?$top=2",
                "/Things?$top=2&$skip=2");
        testNextLink(
                coreSettings,
                "/Things(5)/Datastreams?$top=2",
                "/Things(5)/Datastreams?$top=2&$skip=2");
        testNextLink(
                coreSettingsString,
                "/Things('a String Id')/Datastreams?$top=2",
                "/Things('a String Id')/Datastreams?$top=2&$skip=2");
    }

    @Test
    void testNextLinkSkip() {
        testNextLink(
                coreSettings,
                "/Things?$skip=2&$top=2",
                "/Things?$skip=4&$top=2");
    }

    @Test
    void testNextLinkCountTrue() {
        testNextLink(
                coreSettings,
                "/Things?$count=true&$skip=2&$top=2",
                "/Things?$count=true&$skip=4&$top=2");
    }

    @Test
    void testNextLinkCountFalse() {
        testNextLink(
                coreSettings,
                "/Things?$count=false&$skip=2&$top=2",
                "/Things?$count=false&$top=2&$skip=4");
    }

    @Test
    void testNextLinkOrderByAliasAscDesc() {
        testNextLink(
                coreSettings,
                "/Things?$orderby=@iot.id asc,@iot.id desc&$top=2",
                "/Things?$orderby=@iot.id asc,@iot.id desc&$top=2&$skip=2");
    }

    @Test
    void testNextLinkSelectMultipleMixed() {
        testNextLink(
                coreSettings,
                "/Datastreams?$select=Observations, @iot.id&$top=2",
                "/Datastreams?$select=Observations, @iot.id&$top=2&$skip=2");
    }

    @Test
    void testNextLinkSelectDistinct() {
        testNextLink(
                coreSettings,
                "/Things?$select=distinct:properties/type&$top=2",
                "/Things?$select=distinct:properties/type&$top=2&$skip=2");
    }

    @Test
    void testNextLinkExpand() {
        testNextLink(
                coreSettings,
                "/Things?$expand=Locations&$top=2",
                "/Things?$expand=Locations&$top=2&$skip=2");
    }

    @Test
    void testNextLinkExpandMultipleNavigationPropertes() {
        testNextLink(
                coreSettings,
                "/Datastreams?$expand=Observations($count=true;$top=3),ObservedProperty&$top=2",
                "/Datastreams?$expand=Observations($top=3;$count=true),ObservedProperty&$top=2&$skip=2");
    }

    @Test
    void testNextLink() {
        String[] bases = {
            "/Observations?$filter=validTime gt 2016-01-02T01:01:01.123Z",
            "/Observations?$filter=validTime gt 2016-01-02T01:01:01,123Z",
            "/Observations?$filter=length(result) le 2",
            "/Things?$filter=name eq 'it''s a quote'",
            "/Things?$filter=name eq 'it''''s two quotes'",
            "/Things?$filter=Datastreams/Observations/FeatureOfInterest/id eq 'FOI_1' and Datastreams/Observations/resultTime ge 2010-06-01T00:00:00Z and date(Datastreams/Observations/resultTime) le date(2010-07-01T00:00:00Z)",
            "/Datastreams?$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id;$orderby=id;$skip=5;$top=10;$count=true),ObservedProperty",
            "/Locations?$orderby=geo.distance(location,geography'POINT(8.0 52.0)')",
            "/Locations?$filter=geo.intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')",
            "/Locations?$filter=st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
            "/Locations?$filter=st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', location)",
            "/Locations?$filter=st_disjoint(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
            "/Locations?$filter=st_equals(location, geography'POINT(8 53)')",
            "/Locations?$filter=st_intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')",
            "/Locations?$filter=st_overlaps(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
            "/Locations?$filter=st_relate(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location, 'T********')",
            "/Locations?$filter=st_touches(geography'POLYGON((8 53, 7.5 54.5, 8.5 54.5, 8 53))', location)",
            "/Locations?$filter=st_within(geography'POINT(7.5 52.75)', location)",
            "/Observations?$filter=validTime gt 2016-01-02T01:01:01.000Z/2016-01-03T23:59:59.999Z sub duration'P1D'"
        };
        for (String base : bases) {
            testNextLink(
                    coreSettings,
                    base + "&$top=2",
                    base + "&$top=2&$skip=2");
        }
    }

    // TODO: Add all filters
    @Test
    void testNextLinkFilter() {
        testNextLink(
                coreSettings,
                "/Things?$filter=id eq 1");
        testNextLink(
                coreSettings,
                "/Things?$filter=id eq 'one'&$top=2");
        testNextLink(
                coreSettings,
                "/Things?$filter=properties/prop1 eq 1&$top=2");
        testNextLink(
                coreSettings,
                "/Things?$filter=properties/prop1&$top=2");
        testNextLink(
                coreSettings,
                "/Datastreams?$filter=unitOfMeasurement/name eq 'metre'&$top=2");
    }

    @Test
    void testgetRelativePath() {
        {
            String gotten = UrlHelper.getRelativePath("/a/b/c/e", "/a/b/c/d");
            String expected = "e";
            assertEquals(expected, gotten);
        }
        {
            String gotten = UrlHelper.getRelativePath("/SensorThingsService/v0.0/Datastreams(1)/Sensor", "/SensorThingsService/v0.0/Datastreams");
            String expected = "Datastreams(1)/Sensor";
            assertEquals(expected, gotten);
        }
        {
            String gotten = UrlHelper.getRelativePath("/SensorThingsService/v0.0/Datastreams('a String id')/Sensor", "/SensorThingsService/v0.0/Datastreams");
            String expected = "Datastreams('a String id')/Sensor";
            assertEquals(expected, gotten);
        }
    }

    private static void testNextLink(CoreSettings settings, String url) {
        String baseUrl;
        String expectedNextUrl;
        if (url.contains("?")) {
            baseUrl = url + "&$top=2";
            expectedNextUrl = url + "&$skip=2&$top=2";
        } else {
            baseUrl = url + "?$top=2";
            expectedNextUrl = url + "?$skip=2&$top=2";
        }
        testNextLink(
                settings,
                baseUrl,
                expectedNextUrl);
    }

    private static void testNextLink(CoreSettings settings, String baseUrl, String expectedNextUrl) {

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

        String nextLink = UrlHelper.generateNextLink(queryBase.getPath(), queryBase);
        nextLink = StringHelper.urlDecode(nextLink).substring(SERVICE_ROOT_URL_V11.length());
        Query next = null;
        try {
            next = ParserUtils.parsePathAndQuery(SERVICE_ROOT_URL, Version.V_1_1, nextLink, settings);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed for base url {}", baseUrl);
            LOGGER.error("Expected nextLink   {}", expectedNextUrl);
            LOGGER.error("Got nextLink        {}", nextLink);
            Assertions.fail("Failed to parse generated next link: " + nextLink, e);
        }

        assertEquals(queryExpected, next);
    }

    private static void probeQuery(Query query) {
        for (Expand expand : query.getExpand()) {
            probeQuery(expand.getSubQuery());
        }
    }
}
