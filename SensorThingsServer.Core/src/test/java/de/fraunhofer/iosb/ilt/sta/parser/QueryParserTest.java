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
package de.fraunhofer.iosb.ilt.sta.parser;

import de.fraunhofer.iosb.ilt.sta.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.sta.path.CustomProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.query.Expand;
import de.fraunhofer.iosb.ilt.sta.query.OrderBy;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.query.expression.Path;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.IntervalConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Add;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Divide;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Modulo;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Multiply;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Subtract;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.Equal;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.GreaterEqual;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.GreaterThan;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.LessEqual;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.NotEqual;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.math.Round;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class QueryParserTest {

    public QueryParserTest() {
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

    @Test
    public void testParseQuery_Top_Success() {
        String query = "$top=10";
        Query expResult = new Query();
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_Skip_Success() {
        String query = "$skip=10";
        Query expResult = new Query();
        expResult.setSkip(10);
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_Count_Success() {
        String query = "$count=true";
        Query expResult = new Query();
        expResult.setCount(true);
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_FilterOnly1_Success() {
        String query = "$filter=(result sub 5) gt 10";
        Query expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Subtract(
                                new Path(EntityProperty.Result),
                                new IntegerConstant(5)),
                        new IntegerConstant(10)));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));

        query = "$filter=14 div (result add 1) mod 3 mul 3 eq 3";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Multiply(
                                new Modulo(
                                        new Divide(
                                                new IntegerConstant(14),
                                                new Add(
                                                        new Path(EntityProperty.Result),
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
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_FilterString_Success() {
        String query = "$filter=result gt '3'";
        Query expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Path(EntityProperty.Result),
                        new StringConstant("3")));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));

        query = "$filter=result eq '3'";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Path(EntityProperty.Result),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));

        query = "$filter=result ne '3'";
        expResult = new Query();
        expResult.setFilter(
                new NotEqual(
                        new Path(EntityProperty.Result),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));

        query = "$filter=result eq 'it''s a quote'";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Path(EntityProperty.Result),
                        new StringConstant("it's a quote")));
        result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));

        query = "$filter=result eq 'it''''s two quotes'";
        expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Path(EntityProperty.Result),
                        new StringConstant("it''''s two quotes")));
        result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_FilterProperty() {
        {
            String query = "$filter=properties/array[1] gt 3";
            Query expResult = new Query();
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    EntityProperty.Properties,
                                    new CustomProperty("array"),
                                    new CustomProperty("[1]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query);
            assert (result.equals(expResult));
        }
        {
            String query = "$filter=properties/test_name gt 3";
            Query expResult = new Query();
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    EntityProperty.Properties,
                                    new CustomProperty("test_name")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query);
            assert (result.equals(expResult));
        }
        {
            String query = "$filter=properties/array[1][2] gt 3";
            Query expResult = new Query();
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    EntityProperty.Properties,
                                    new CustomProperty("array"),
                                    new CustomProperty("[1]"),
                                    new CustomProperty("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query);
            assert (result.equals(expResult));
        }
        {
            String query = "$filter=properties/array[1]/deeper[2] gt 3";
            Query expResult = new Query();
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    EntityProperty.Properties,
                                    new CustomProperty("array"),
                                    new CustomProperty("[1]"),
                                    new CustomProperty("deeper"),
                                    new CustomProperty("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query);
            assert (result.equals(expResult));
        }
    }

    @Test
    public void testParseQuery_FilterTime_Success() {
        String query = "$filter=time gt 2015-10-14T23:30:00.104+02:00";
        Query expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Path(EntityProperty.Time),
                        new DateTimeConstant("2015-10-14T23:30:00.104+02:00")));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));

        query = "$filter=time gt 2015-10-14T23:30:00.104+02:00 add duration'P1D'";
        expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Path(EntityProperty.Time),
                        new Add(
                                new DateTimeConstant("2015-10-14T23:30:00.104+02:00"),
                                new DurationConstant("P1D")
                        )
                ));
        result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));

        query = "$filter=time gt 2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00";
        expResult = new Query();
        expResult.setFilter(
                new GreaterThan(
                        new Path(EntityProperty.Time),
                        new IntervalConstant("2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00")));
        result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_FilterFunction_Success() {
        String query = "$filter=round(result add 0.1) eq 2";
        Query expResult = new Query();
        expResult.setFilter(
                new Equal(
                        new Round(
                                new Add(
                                        new Path(EntityProperty.Result),
                                        new DoubleConstant(0.1)
                                )
                        ),
                        new IntegerConstant(2)
                ));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_OrderByEntityProperty_Success() {
        String query = "$orderby=ID";
        Query expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.Id)));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_OrderByAlias_Success() {
        String query = "$orderby=@iot.id";
        Query expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.Id)));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_OrderByAliasAscDesc_Success() {
        String query = "$orderby=@iot.id asc,@iot.id desc";
        Query expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.Id)));
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.Id), OrderBy.OrderType.Descending));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_OrderByMixedPath_Success() {
        String query = "$orderby=Datastream/@iot.id";
        Query expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(NavigationProperty.Datastream, EntityProperty.Id)));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));

        query = "$orderby=properties/subprop/name";
        expResult = new Query();
        expResult.getOrderBy().add(new OrderBy(new Path(EntityProperty.Properties, new CustomProperty("subprop"), new CustomProperty("name"))));
        result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_SelectEntityProperty_Success() {
        String query = "$select=id";
        Query expResult = new Query();
        expResult.getSelect().add(EntityProperty.Id);
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_SelectNavigationProperty_Success() {
        String query = "$select=Observations";
        Query expResult = new Query();
        expResult.getSelect().add(NavigationProperty.Observations);
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_SelectMultipleMixed_Success() {
        String query = "$select=Observations, id";
        Query expResult = new Query();
        expResult.getSelect().add(NavigationProperty.Observations);
        expResult.getSelect().add(EntityProperty.Id);
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_ExpandSingleNavigationProperty_Success() {
        String query = "$expand=Observations";
        Query expResult = new Query();
        expResult.getExpand().add(new Expand(NavigationProperty.Observations));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_ExpandMultipleNavigationPropertes_Success() {
        String query = "$expand=Observations,ObservedProperty";
        Query expResult = new Query();
        expResult.getExpand().add(new Expand(NavigationProperty.Observations));
        expResult.getExpand().add(new Expand(NavigationProperty.ObservedProperty));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_ExpandWithSubquery_Success() {
        String query = "$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id;$orderby=id;$skip=5;$top=10;$count=true),ObservedProperty&$top=10";
        Query expResult = new Query();
        Query subQuery = new Query();
        subQuery.setFilter(new Equal(new Path(EntityProperty.Result), new IntegerConstant(1)));
        subQuery.getExpand().add(new Expand(NavigationProperty.FeatureOfInterest));
        subQuery.getSelect().add(EntityProperty.Id);
        subQuery.getOrderBy().add(new OrderBy(new Path(EntityProperty.Id)));
        subQuery.setSkip(5);
        subQuery.setTop(10);
        subQuery.setCount(true);
        expResult.getExpand().add(new Expand(subQuery, NavigationProperty.Observations));
        expResult.getExpand().add(new Expand(NavigationProperty.ObservedProperty));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_Complex1_Success() {
        String query = "$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id),ObservedProperty&$top=10";
        Query expResult = new Query();
        Query subQuery1 = new Query();
        subQuery1.setFilter(new Equal(new Path(EntityProperty.Result), new IntegerConstant(1)));
        subQuery1.getExpand().add(new Expand(NavigationProperty.FeatureOfInterest));
        subQuery1.getSelect().add(EntityProperty.Id);
        expResult.getExpand().add(new Expand(subQuery1, NavigationProperty.Observations));
        expResult.getExpand().add(new Expand(NavigationProperty.ObservedProperty));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    @Test
    public void testParseQuery_FilterComplex_Success() {
        String query = "$filter=Datastreams/Observations/FeatureOfInterest/id eq 'FOI_1' and Datastreams/Observations/resultTime ge 2010-06-01T00:00:00Z and Datastreams/Observations/resultTime le 2010-07-01T00:00:00Z";
        Query expResult = new Query();
        expResult.setFilter(
                new And(
                        new Equal(
                                new Path(NavigationProperty.Datastreams,
                                        NavigationProperty.Observations,
                                        NavigationProperty.FeatureOfInterest,
                                        EntityProperty.Id),
                                new StringConstant("FOI_1")),
                        new And(
                                new GreaterEqual(
                                        new Path(NavigationProperty.Datastreams,
                                                NavigationProperty.Observations,
                                                EntityProperty.ResultTime),
                                        new DateTimeConstant(new DateTime(2010, 06, 01, 0, 0, DateTimeZone.UTC))),
                                new LessEqual(
                                        new Path(NavigationProperty.Datastreams,
                                                NavigationProperty.Observations,
                                                EntityProperty.ResultTime),
                                        new DateTimeConstant(new DateTime(2010, 07, 01, 0, 0, DateTimeZone.UTC))))));
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }

    // TODO add tests for all functions
    @Test
    public void testParseQuery_Format() {
        String query = "$resultFormat=dataArray";
        Query expResult = new Query();
        expResult.setFormat("dataArray");
        Query result = QueryParser.parseQuery(query);
        assert (result.equals(expResult));
    }
}
