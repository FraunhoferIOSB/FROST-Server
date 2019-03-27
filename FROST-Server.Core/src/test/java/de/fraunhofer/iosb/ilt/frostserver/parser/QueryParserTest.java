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
package de.fraunhofer.iosb.ilt.frostserver.parser;

import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.CustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntervalConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.arithmetic.Add;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.arithmetic.Divide;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.arithmetic.Modulo;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.arithmetic.Multiply;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.arithmetic.Subtract;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.Equal;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.GreaterEqual;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.GreaterThan;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.LessEqual;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.NotEqual;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.math.Round;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal.Overlaps;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigUtils;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.Arrays;
import java.util.HashSet;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class QueryParserTest {

    @Test
    public void testParseQuery_Top() {
        Query expResult = new Query();
        Assert.assertFalse(expResult.getTop().isPresent());
        Assert.assertEquals(ConfigUtils.getDefaultValueInt(CoreSettings.class, CoreSettings.TAG_DEFAULT_TOP), expResult.getTopOrDefault());
        expResult.setTop(10);

        String query = "$top=10";
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
        Assert.assertTrue(result.getTop().isPresent());
        Assert.assertEquals(10, result.getTopOrDefault());
    }

    @Test
    public void testParseQuery_Skip() {
        Query expResult = new Query();
        Assert.assertFalse(expResult.getSkip().isPresent());
        Assert.assertEquals(11, expResult.getSkip(11));
        expResult.setSkip(10);

        String query = "$skip=10";
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
        Assert.assertTrue(result.getSkip().isPresent());
        Assert.assertEquals(10, result.getSkip(11));
    }

    @Test
    public void testParseQuery_Count() {
        Query expResult = new Query();
        Assert.assertFalse(expResult.getCount().isPresent());
        Assert.assertEquals(ConfigUtils.getDefaultValueBoolean(CoreSettings.class, CoreSettings.TAG_DEFAULT_COUNT), expResult.isCountOrDefault());

        expResult.setCount(true);
        String query = "$count=true";
        Query result = QueryParser.parseQuery(query);
        Assert.assertTrue(result.getCount().isPresent());
        Assert.assertTrue(result.isCountOrDefault());
        Assert.assertEquals(expResult, result);

        expResult.setCount(false);
        query = "$count=false";
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
        Assert.assertFalse(result.isCountOrDefault());
    }

    @Test
    public void testParseQuery_FilterOnly1() {
        String query = "$filter=(result sub 5) gt 10";
        Query expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Subtract(
                                new Path(EntityProperty.RESULT),
                                new IntegerConstant(5)),
                        new IntegerConstant(10)));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=14 div (result add 1) mod 3 mul 3 eq 3";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Multiply(
                                new Modulo(
                                        new Divide(
                                                new IntegerConstant(14),
                                                new Add(
                                                        new Path(EntityProperty.RESULT),
                                                        new IntegerConstant(1)
                                                )
                                        ),
                                        new IntegerConstant(3)
                                ),
                                new IntegerConstant(3)
                        ),
                        new IntegerConstant(3)
                )
        );
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_FilterLinked() {
        String query = "$filter=Datastream/id eq 1";
        Query expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Path(NavigationProperty.DATASTREAM, EntityProperty.ID),
                        new IntegerConstant(1)));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        // Theoretical path, does not actually exist
        query = "$filter=Thing/Location/location eq 1";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Path(NavigationProperty.THING, NavigationProperty.LOCATION, EntityProperty.LOCATION),
                        new IntegerConstant(1)));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseQuery_FilterInvalidCustomProperty() {
        // Theoretical path, does not actually exist
        String query = "$filter=Thing/custom eq 1";
        QueryParser.parseQuery(query);
    }

    @Test
    public void testParseQuery_FilterString() {
        String query = "$filter=result gt '3'";
        Query expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Path(EntityProperty.RESULT),
                        new StringConstant("3")));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=result eq '3'";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Path(EntityProperty.RESULT),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=result ne '3'";
        expResult = new Query();
        expResult.setFilter(
                new NotEqual(
                        new Path(EntityProperty.RESULT),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=result eq 'it''s a quote'";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Path(EntityProperty.RESULT),
                        new StringConstant("it's a quote")));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=result eq 'it''''s two quotes'";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Path(EntityProperty.RESULT),
                        new StringConstant("it''''s two quotes")));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=description eq 'utf-8: 水位高度'";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Path(EntityProperty.DESCRIPTION),
                        new StringConstant("utf-8: 水位高度")));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_FilterProperty() {
        {
            String query = "$filter=properties/array[1] gt 3";
            Query expResult = new Query();
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    EntityProperty.PROPERTIES,
                                    new CustomProperty("array"),
                                    new CustomProperty("[1]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/test_name gt 3";
            Query expResult = new Query();
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    EntityProperty.PROPERTIES,
                                    new CustomProperty("test_name")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array[1][2] gt 3";
            Query expResult = new Query();
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    EntityProperty.PROPERTIES,
                                    new CustomProperty("array"),
                                    new CustomProperty("[1]"),
                                    new CustomProperty("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array[1]/deeper[2] gt 3";
            Query expResult = new Query();
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    EntityProperty.PROPERTIES,
                                    new CustomProperty("array"),
                                    new CustomProperty("[1]"),
                                    new CustomProperty("deeper"),
                                    new CustomProperty("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=location/properties/priority eq 3";
            Query expResult = new Query();
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    EntityProperty.LOCATION,
                                    new CustomProperty("properties"),
                                    new CustomProperty("priority")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query);
            Assert.assertEquals(expResult, result);
        }
    }

    @Test
    public void testParseQuery_FilterUoM() {
        {
            String query = "$filter=unitOfMeasurement/name eq 'metre'";
            Query expResult = new Query();
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    EntityProperty.UNITOFMEASUREMENT,
                                    new CustomProperty("name")),
                            new StringConstant("metre")));
            Query result = QueryParser.parseQuery(query);
            Assert.assertEquals(expResult, result);
        }
    }

    @Test
    public void testParseQuery_FilterTime() {
        String query = "$filter=time gt 2015-10-14T23:30:00.104+02:00";
        Query expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Path(EntityProperty.TIME),
                        new DateTimeConstant("2015-10-14T23:30:00.104+02:00")));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=time gt 2015-10-14T23:30:00.104+02:00 add duration'P1D'";
        expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Path(EntityProperty.TIME),
                        new Add(
                                new DateTimeConstant("2015-10-14T23:30:00.104+02:00"),
                                new DurationConstant("P1D")
                        )
                ));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=time gt 2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00";
        expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Path(EntityProperty.TIME),
                        new IntervalConstant("2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00")));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=overlaps(phenomenonTime,2015-10-14T01:01:01.000+02:00/P1D)";
        expResult = new Query();
        expResult.setFilter(
                new Overlaps(
                        new Path(EntityProperty.PHENOMENONTIME),
                        new IntervalConstant("2015-10-14T01:01:01.000+02:00/P1D")));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$filter=overlaps(phenomenonTime,P1D/2015-10-14T01:01:01.000+02:00)";
        expResult = new Query();
        expResult.setFilter(
                new Overlaps(
                        new Path(EntityProperty.PHENOMENONTIME),
                        new IntervalConstant("P1D/2015-10-14T01:01:01.000+02:00")));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_FilterFunction() {
        String query = "$filter=round(result add 0.1) eq 2";
        Query expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Round(
                                new Add(
                                        new Path(EntityProperty.RESULT),
                                        new DoubleConstant(0.1)
                                )
                        ),
                        new IntegerConstant(2)
                ));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_OrderByEntityProperty() {
        String query = "$orderby=ID";
        Query expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.ID)));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_OrderByAlias() {
        String query = "$orderby=@iot.id";
        Query expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.ID)));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_OrderByAliasAscDesc() {
        String query = "$orderby=@iot.id asc,@iot.id desc";
        Query expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.ID)));
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.ID), OrderBy.OrderType.DESCENDING));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_OrderByMixedPath() {
        String query = "$orderby=Datastream/@iot.id";
        Query expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(NavigationProperty.DATASTREAM, EntityProperty.ID)));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);

        query = "$orderby=properties/subprop/name";
        expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.PROPERTIES, new CustomProperty("subprop"), new CustomProperty("name"))));
        result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_Select() {
        Query expResult = new Query();
        expResult.getSelect().add(NavigationProperty.OBSERVATIONS);
        expResult.getSelect().add(EntityProperty.ID);
        Query result = new Query();
        result.setSelect(new HashSet<>(Arrays.asList(NavigationProperty.OBSERVATIONS, EntityProperty.ID)));
        Assert.assertEquals(expResult, result);

        expResult.getSelect().clear();
        expResult.getSelect().add(NavigationProperty.THING);
        expResult.getSelect().add(EntityProperty.ID);
        result.setSelect(Arrays.asList(NavigationProperty.THING, EntityProperty.ID));
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_SelectEntityProperty() {
        String query = "$select=id";
        Query expResult = new Query();
        expResult.getSelect().add(EntityProperty.ID);
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_SelectNavigationProperty() {
        String query = "$select=Observations";
        Query expResult = new Query();
        expResult.getSelect().add(NavigationProperty.OBSERVATIONS);
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_SelectMultipleMixed() {
        String query = "$select=Observations, id";
        Query expResult = new Query();
        expResult.setSelect(Arrays.asList(NavigationProperty.OBSERVATIONS, EntityProperty.ID));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_ExpandSingleNavigationProperty() {
        String query = "$expand=Observations";
        Query expResult = new Query();
        expResult.getExpand().add(new Expand(NavigationProperty.OBSERVATIONS));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_ExpandMultipleNavigationPropertes() {
        String query = "$expand=Observations,ObservedProperty";
        Query expResult = new Query();
        expResult.getExpand().add(new Expand(NavigationProperty.OBSERVATIONS));
        expResult.getExpand().add(new Expand(NavigationProperty.OBSERVEDPROPERTY));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_ExpandWithSubquery() {
        String query = "$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id;$orderby=id;$skip=5;$top=10;$count=true),ObservedProperty&$top=10";
        Query expResult = new Query();
        Query subQuery = new Query();
        subQuery.setFilter(new Equal(new Path(EntityProperty.RESULT), new IntegerConstant(1)));
        subQuery.getExpand().add(new Expand(NavigationProperty.FEATUREOFINTEREST));
        subQuery.getSelect().add(EntityProperty.ID);
        subQuery.getOrderBy().add(new OrderBy(new Path(EntityProperty.ID)));
        subQuery.setSkip(5);
        subQuery.setTop(10);
        subQuery.setCount(true);
        expResult.getExpand().add(new Expand(subQuery, NavigationProperty.OBSERVATIONS));
        expResult.getExpand().add(new Expand(NavigationProperty.OBSERVEDPROPERTY));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_Complex1() {
        String query = "$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id),ObservedProperty&$top=10";
        Query expResult = new Query();
        Query subQuery1 = new Query();
        subQuery1.setFilter(new Equal(new Path(EntityProperty.RESULT), new IntegerConstant(1)));
        subQuery1.getExpand().add(new Expand(NavigationProperty.FEATUREOFINTEREST));
        subQuery1.getSelect().add(EntityProperty.ID);
        expResult.getExpand().add(new Expand(subQuery1, NavigationProperty.OBSERVATIONS));
        expResult.getExpand().add(new Expand(NavigationProperty.OBSERVEDPROPERTY));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testParseQuery_FilterComplex() {
        String query = "$filter=Datastreams/Observations/FeatureOfInterest/id eq 'FOI_1' and Datastreams/Observations/resultTime ge 2010-06-01T00:00:00Z and Datastreams/Observations/resultTime le 2010-07-01T00:00:00Z";
        Query expResult = new Query();
        expResult.setFilter(
                new And(
                        new Equal(
                                new Path(NavigationProperty.DATASTREAMS,
                                        NavigationProperty.OBSERVATIONS,
                                        NavigationProperty.FEATUREOFINTEREST,
                                        EntityProperty.ID),
                                new StringConstant("FOI_1")),
                        new And(
                                new GreaterEqual(
                                        new Path(NavigationProperty.DATASTREAMS,
                                                NavigationProperty.OBSERVATIONS,
                                                EntityProperty.RESULTTIME),
                                        new DateTimeConstant(new DateTime(2010, 06, 01, 0, 0, DateTimeZone.UTC))),
                                new LessEqual(
                                        new Path(NavigationProperty.DATASTREAMS,
                                                NavigationProperty.OBSERVATIONS,
                                                EntityProperty.RESULTTIME),
                                        new DateTimeConstant(new DateTime(2010, 07, 01, 0, 0, DateTimeZone.UTC))))));
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }

    // TODO add tests for all functions
    @Test
    public void testParseQuery_Format() {
        String query = "$resultFormat=dataArray";
        Query expResult = new Query();
        expResult.setFormat("dataArray");
        Query result = QueryParser.parseQuery(query);
        Assert.assertEquals(expResult, result);
    }
}
