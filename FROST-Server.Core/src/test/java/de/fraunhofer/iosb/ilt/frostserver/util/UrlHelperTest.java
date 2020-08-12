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

import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManagerLong;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManagerString;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class UrlHelperTest {

    private static CoreSettings settings;
    private static final String SERVICE_ROOT_URL = "http://example.org/FROST-Server";
    private static final String SERVICE_ROOT_URL_V11 = SERVICE_ROOT_URL + '/' + Version.V_1_1.urlPart;

    @BeforeClass
    public static void initClass() {
        settings = new CoreSettings();
    }

    @Test
    public void testNextLinkTop() {
        testNextLink(
                settings,
                "/Things?$top=2",
                "/Things?$top=2&$skip=2");
        testNextLink(
                settings,
                "/Things(5)/Datastreams?$top=2",
                "/Things(5)/Datastreams?$top=2&$skip=2");
        testNextLink(
                settings,
                new IdManagerString(),
                "/Things('a String Id')/Datastreams?$top=2",
                "/Things('a String Id')/Datastreams?$top=2&$skip=2");
    }

    @Test
    public void testNextLinkSkip() {
        testNextLink(
                settings,
                "/Things?$skip=2&$top=2",
                "/Things?$skip=4&$top=2");
    }

    @Test
    public void testNextLinkCountTrue() {
        testNextLink(
                settings,
                "/Things?$count=true&$skip=2&$top=2",
                "/Things?$count=true&$skip=4&$top=2");
    }

    @Test
    public void testNextLinkCountFalse() {
        testNextLink(
                settings,
                "/Things?$count=false&$skip=2&$top=2",
                "/Things?$count=false&$top=2&$skip=4");
    }

    @Test
    public void testNextLinkOrderByAliasAscDesc() {
        testNextLink(
                settings,
                "/Things?$orderby=@iot.id asc,@iot.id desc&$top=2",
                "/Things?$orderby=@iot.id asc,@iot.id desc&$top=2&$skip=2");
    }

    @Test
    public void testNextLinkSelectMultipleMixed() {
        testNextLink(
                settings,
                "/Things?$select=Observations, @iot.id&$top=2",
                "/Things?$select=Observations, @iot.id&$top=2&$skip=2");
    }

    @Test
    public void testNextLinkExpandMultipleNavigationPropertes() {
        testNextLink(
                settings,
                "/Things?$expand=Observations($count=true;$top=3),ObservedProperty&$top=2",
                "/Things?$expand=Observations($top=3;$count=true),ObservedProperty&$top=2&$skip=2");
    }

    @Test
    public void testNextLink() {
        String[] bases = {
            "$filter=length(result) le 2",
            "$filter=name eq 'it''s a quote'",
            "$filter=name eq 'it''''s two quotes'",
            "$filter=Datastreams/Observations/FeatureOfInterest/id eq 'FOI_1' and Datastreams/Observations/resultTime ge 2010-06-01T00:00:00Z and date(Datastreams/Observations/resultTime) le date(2010-07-01T00:00:00Z)",
            "$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id;$orderby=id;$skip=5;$top=10;$count=true),ObservedProperty",
            "$orderby=geo.distance(location,geography'POINT(8.0 52.0)')",
            "$filter=geo.intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')",
            "$filter=st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
            "$filter=st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', location)",
            "$filter=st_disjoint(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
            "$filter=st_equals(location, geography'POINT(8 53)')",
            "$filter=st_intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')",
            "$filter=st_overlaps(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
            "$filter=st_relate(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location, 'T********')",
            "$filter=st_touches(geography'POLYGON((8 53, 7.5 54.5, 8.5 54.5, 8 53))', location)",
            "$filter=st_within(geography'POINT(7.5 52.75)', location)",
            "$filter=validTime gt 2016-01-02T01:01:01.000Z/2016-01-03T23:59:59.999Z sub duration'P1D'"
        };
        for (String base : bases) {
            testNextLink(
                    settings,
                    "/Things?" + base + "&$top=2",
                    "/Things?" + base + "&$top=2&$skip=2");
        }
    }

    // TODO: Add all filters
    @Test
    public void testNextLinkFilter() {
        testNextLink(
                settings,
                "/Things?$filter=id eq 1");
        testNextLink(
                settings,
                new IdManagerString(),
                "/Things?$filter=id eq 'one'&$top=2");
        testNextLink(
                settings,
                "/Things?$filter=properties/prop1 eq 1&$top=2");
        testNextLink(
                settings,
                "/Things?$filter=properties/prop1&$top=2");
        testNextLink(
                settings,
                "/Datastreams?$filter=unitOfMeasurement/name eq 'metre'&$top=2");
    }

    @Test
    public void testgetRelativePath() {
        {
            String gotten = UrlHelper.getRelativePath("/a/b/c/e", "/a/b/c/d");
            String expected = "e";
            Assert.assertEquals(expected, gotten);
        }
        {
            String gotten = UrlHelper.getRelativePath("/SensorThingsService/v0.0/Datastreams(1)/Sensor", "/SensorThingsService/v0.0/Datastreams");
            String expected = "Datastreams(1)/Sensor";
            Assert.assertEquals(expected, gotten);
        }
        {
            String gotten = UrlHelper.getRelativePath("/SensorThingsService/v0.0/Datastreams('a String id')/Sensor", "/SensorThingsService/v0.0/Datastreams");
            String expected = "Datastreams('a String id')/Sensor";
            Assert.assertEquals(expected, gotten);
        }
    }

    private static void testNextLink(CoreSettings settings, String baseUrl, String expectedNextUrl) {
        testNextLink(settings, new IdManagerLong(), baseUrl, expectedNextUrl);
    }

    private static void testNextLink(CoreSettings settings, String url) {
        testNextLink(settings, new IdManagerLong(), url);
    }

    private static void testNextLink(CoreSettings settings, IdManager idManager, String url) {
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
                idManager,
                baseUrl,
                expectedNextUrl);
    }

    private static void testNextLink(CoreSettings settings, IdManager idManager, String baseUrl, String expectedNextUrl) {
        Query queryBase = ParserHelper.parsePathAndQuery(idManager, SERVICE_ROOT_URL, Version.V_1_1, baseUrl, settings);
        Query queryExpected = ParserHelper.parsePathAndQuery(idManager, SERVICE_ROOT_URL, Version.V_1_1, expectedNextUrl, settings);

        String nextLink = UrlHelper.generateNextLink(queryBase.getPath(), queryBase);
        nextLink = StringHelper.urlDecode(nextLink).substring(SERVICE_ROOT_URL_V11.length());
        Query next = ParserHelper.parsePathAndQuery(idManager, SERVICE_ROOT_URL, Version.V_1_1, nextLink, settings);

        Assert.assertEquals(queryExpected, next);
    }

}
