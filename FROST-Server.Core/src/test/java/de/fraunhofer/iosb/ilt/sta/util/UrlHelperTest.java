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
package de.fraunhofer.iosb.ilt.sta.util;

import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManagerString;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManagerlong;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class UrlHelperTest {

    public UrlHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void testNextLink(String baseUrl, String expectedNextUrl) {
        testNextLink(new IdManagerlong(), baseUrl, expectedNextUrl);
    }

    private void testNextLink(String url) {
        testNextLink(new IdManagerlong(), url);
    }

    private void testNextLink(IdManager idManager, String url) {
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
                idManager,
                baseUrl,
                expectedNextUrl);
    }

    private void testNextLink(IdManager idManager, String baseUrl, String expectedNextUrl) {
        ParserHelper.PathQuery queryBase = ParserHelper.parsePathAndQuery(idManager, "", baseUrl);
        ParserHelper.PathQuery queryExpected = ParserHelper.parsePathAndQuery(idManager, "", expectedNextUrl);

        String nextLink = UrlHelper.generateNextLink(queryBase.path, queryBase.query);
        nextLink = UrlHelper.urlDecode(nextLink);
        ParserHelper.PathQuery next = ParserHelper.parsePathAndQuery(idManager, "", nextLink);

        assert (next.equals(queryExpected));
    }

    @Test
    public void testEscapeForStringConstant() {
        Assert.assertEquals("abcdefg", UrlHelper.escapeForStringConstant("abcdefg"));
        Assert.assertEquals("''", UrlHelper.escapeForStringConstant("'"));
        Assert.assertEquals("''''", UrlHelper.escapeForStringConstant("''"));
    }

    @Test
    public void testUrlEncode() {
        Assert.assertEquals("http%3A//example.org/Things%5Bxyz%27xyz%5D", UrlHelper.urlEncode("http://example.org/Things[xyz'xyz]", true));
        Assert.assertEquals("http%3A%2F%2Fexample.org%2FThings%5Bxyz%27xyz%5D", UrlHelper.urlEncode("http://example.org/Things[xyz'xyz]", false));
    }

    @Test
    public void testNextLink_Top_Success() {
        testNextLink(
                "/Things?$top=2",
                "/Things?$top=2&$skip=2");
        testNextLink(
                "/Things(5)/Datastreams?$top=2",
                "/Things(5)/Datastreams?$top=2&$skip=2");
        testNextLink(
                new IdManagerString(),
                "/Things('a String Id')/Datastreams?$top=2",
                "/Things('a String Id')/Datastreams?$top=2&$skip=2");
    }

    @Test
    public void testNextLink_Skip_Success() {
        testNextLink(
                "/Things?$skip=2&$top=2",
                "/Things?$skip=4&$top=2");
    }

    @Test
    public void testNextLink_CountTrue_Success() {
        testNextLink(
                "/Things?$count=true&$skip=2&$top=2",
                "/Things?$count=true&$skip=4&$top=2");
    }

    @Test
    public void testNextLink_CountFalse_Success() {
        testNextLink(
                "/Things?$count=false&$skip=2&$top=2",
                "/Things?$count=false&$top=2&$skip=4");
    }

    @Test
    public void testNextLink_OrderByAliasAscDesc_Success() {
        testNextLink(
                "/Things?$orderby=@iot.id asc,@iot.id desc&$top=2",
                "/Things?$orderby=@iot.id asc,@iot.id desc&$top=2&$skip=2");
    }

    @Test
    public void testNextLink_SelectMultipleMixed_Success() {
        testNextLink(
                "/Things?$select=Observations, @iot.id&$top=2",
                "/Things?$select=Observations, @iot.id&$top=2&$skip=2");
    }

    @Test
    public void testNextLink_ExpandMultipleNavigationPropertes_Success() {
        testNextLink(
                "/Things?$expand=Observations($count=true;$top=3),ObservedProperty&$top=2",
                "/Things?$expand=Observations($top=3;$count=true),ObservedProperty&$top=2&$skip=2");
    }

    @Test
    public void testNextLink_Success() {
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
                    "/Things?" + base + "&$top=2",
                    "/Things?" + base + "&$top=2&$skip=2");
        }
    }

    // TODO: Add all filters
    @Test
    public void testNextLink_Filter() {
        testNextLink(
                "/Things?$filter=id eq 1");
        testNextLink(
                new IdManagerString(),
                "/Things?$filter=id eq 'one'&$top=2");
        testNextLink(
                "/Things?$filter=properties/prop1 eq 1&$top=2");
        testNextLink(
                "/Things?$filter=properties/prop1&$top=2");
        testNextLink(
                "/Datastreams?$filter=unitOfMeasurement/name eq 'metre'&$top=2");
    }

    @Test
    public void testgetRelativePath() {
        {
            String gotten = UrlHelper.getRelativePath("/a/b/c/e", "/a/b/c/d");
            String expected = "e";
            assert (gotten.equals(expected));
        }
        {
            String gotten = UrlHelper.getRelativePath("/SensorThingsService/v0.0/Datastreams(1)/Sensor", "/SensorThingsService/v0.0/Datastreams");
            String expected = "Datastreams(1)/Sensor";
            assert (gotten.equals(expected));
        }
        {
            String gotten = UrlHelper.getRelativePath("/SensorThingsService/v0.0/Datastreams('a String id')/Sensor", "/SensorThingsService/v0.0/Datastreams");
            String expected = "Datastreams('a String id')/Sensor";
            assert (gotten.equals(expected));
        }
    }

}
