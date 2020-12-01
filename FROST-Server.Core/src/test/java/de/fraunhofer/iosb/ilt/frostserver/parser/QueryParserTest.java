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

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomLink;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyCustom;
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class QueryParserTest {

    private static CoreSettings settings;
    private static ResourcePath path;
    private static ModelRegistry modelRegistry;
    private static JsonReader entityParser;

    @BeforeClass
    public static void beforeClass() {
        settings = new CoreSettings();
        modelRegistry = settings.getModelRegistry();
        modelRegistry.initDefaultTypes();
        modelRegistry.initFinalise();
        modelRegistry.setIdClass(IdLong.class);
        entityParser = new JsonReader(modelRegistry);
        path = new ResourcePath("http://example.org/FROST-Server", Version.V_1_0, "/");
    }

    @Test
    public void testTop() {
        Query expResult = new Query(settings.getQueryDefaults(), path);
        Assert.assertFalse(expResult.getTop().isPresent());
        Assert.assertEquals(ConfigUtils.getDefaultValueInt(CoreSettings.class, CoreSettings.TAG_DEFAULT_TOP), expResult.getTopOrDefault());
        expResult.setTop(10);

        String query = "$top=10";
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
        Assert.assertTrue(result.getTop().isPresent());
        Assert.assertEquals(10, result.getTopOrDefault());
    }

    @Test
    public void testSkip() {
        Query expResult = new Query(settings.getQueryDefaults(), path);
        Assert.assertFalse(expResult.getSkip().isPresent());
        Assert.assertEquals(11, expResult.getSkip(11));
        expResult.setSkip(10);

        String query = "$skip=10";
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
        Assert.assertTrue(result.getSkip().isPresent());
        Assert.assertEquals(10, result.getSkip(11));
    }

    @Test
    public void testCount() {
        Query expResult = new Query(settings.getQueryDefaults(), path);
        Assert.assertFalse(expResult.getCount().isPresent());
        Assert.assertEquals(ConfigUtils.getDefaultValueBoolean(CoreSettings.class, CoreSettings.TAG_DEFAULT_COUNT), expResult.isCountOrDefault());

        expResult.setCount(true);
        String query = "$count=true";
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertTrue(result.getCount().isPresent());
        Assert.assertTrue(result.isCountOrDefault());
        Assert.assertEquals(expResult, result);

        expResult.setCount(false);
        query = "$count=false";
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
        Assert.assertFalse(result.isCountOrDefault());
    }

    @Test
    public void testFilterLargeNumber() {
        String query = "$filter=result eq 800000113797";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(modelRegistry.EP_RESULT),
                        new IntegerConstant(800000113797L)));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testFilterOnly1() {
        String query = "$filter=(result sub 5) gt 10";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Subtract(
                                new Path(modelRegistry.EP_RESULT),
                                new IntegerConstant(5)),
                        new IntegerConstant(10)));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=14 div (result add 1) mod 3 mul 3 eq 3";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Multiply(
                                new Modulo(
                                        new Divide(
                                                new IntegerConstant(14),
                                                new Add(
                                                        new Path(modelRegistry.EP_RESULT),
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
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testFilterLinked() {
        String query = "$filter=Datastream/id eq 1";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(new Equal(
                new Path(modelRegistry.NP_DATASTREAM, ModelRegistry.EP_ID),
                new IntegerConstant(1)));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        // Theoretical path, does not actually exist
        query = "$filter=Thing/Location/location eq 1";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(new Equal(
                new Path(modelRegistry.NP_THING, modelRegistry.NP_LOCATION, modelRegistry.EP_LOCATION),
                new IntegerConstant(1)));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilterInvalidCustomProperty() {
        // Theoretical path, does not actually exist
        String query = "$filter=Thing/custom eq 1";
        QueryParser.parseQuery(query, settings, path);
    }

    @Test
    public void testFilterString() {
        String query = "$filter=result gt '3'";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(modelRegistry.EP_RESULT),
                        new StringConstant("3")));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=result eq '3'";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(modelRegistry.EP_RESULT),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=result ne '3'";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new NotEqual(
                        new Path(modelRegistry.EP_RESULT),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=result eq 'it''s a quote'";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(modelRegistry.EP_RESULT),
                        new StringConstant("it's a quote")));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=result eq 'it''''s two quotes'";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(modelRegistry.EP_RESULT),
                        new StringConstant("it''''s two quotes")));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=description eq 'utf-8: 水位高度'";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(modelRegistry.EP_DESCRIPTION),
                        new StringConstant("utf-8: 水位高度")));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testFilterCustomEntityProperty() {
        {
            String query = "$filter=properties/building.Thing/name eq 'Main'";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    modelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustomLink("building.Thing", modelRegistry.THING),
                                    modelRegistry.EP_NAME),
                            new StringConstant("Main")
                    ));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
    }

    @Test
    public void testFilterProperty() {
        {
            String query = "$filter=properties/array[1] gt 3";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    modelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/test_name gt 3";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    modelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("test_name")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array[1][2] gt 3";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    modelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]"),
                                    new EntityPropertyCustom("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array[1]/deeper[2] gt 3";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    modelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]"),
                                    new EntityPropertyCustom("deeper"),
                                    new EntityPropertyCustom("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array/1/deeper/2 gt 3";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    modelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]"),
                                    new EntityPropertyCustom("deeper"),
                                    new EntityPropertyCustom("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=location/properties/priority eq 3";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    modelRegistry.EP_LOCATION,
                                    new EntityPropertyCustom("properties"),
                                    new EntityPropertyCustom("priority")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=location/properties/4frost eq 3";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    modelRegistry.EP_LOCATION,
                                    new EntityPropertyCustom("properties"),
                                    new EntityPropertyCustom("4frost")),
                            new IntegerConstant(3)
                    ));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$filter=location/properties/4 eq 3";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    modelRegistry.EP_LOCATION,
                                    new EntityPropertyCustom("properties"),
                                    new EntityPropertyCustom("4")),
                            new IntegerConstant(3)
                    ));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
    }

    @Test
    public void testFilterUoM() {
        {
            String query = "$filter=unitOfMeasurement/name eq 'metre'";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    modelRegistry.EP_UNITOFMEASUREMENT,
                                    new EntityPropertyCustom("name")),
                            new StringConstant("metre")));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
    }

    @Test
    public void testFilterTime() {
        String query = "$filter=time gt 2015-10-14T23:30:00.104+02:00";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(modelRegistry.EP_TIME),
                        new DateTimeConstant("2015-10-14T23:30:00.104+02:00")));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=time gt 2015-10-14T23:30:00.104+02:00 add duration'P1D'";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(modelRegistry.EP_TIME),
                        new Add(
                                new DateTimeConstant("2015-10-14T23:30:00.104+02:00"),
                                new DurationConstant("P1D")
                        )
                ));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=time gt 2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(modelRegistry.EP_TIME),
                        new IntervalConstant("2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00")));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=overlaps(phenomenonTime,2015-10-14T01:01:01.000+02:00/P1D)";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Overlaps(
                        new Path(modelRegistry.EP_PHENOMENONTIME),
                        new IntervalConstant("2015-10-14T01:01:01.000+02:00/P1D")));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$filter=overlaps(phenomenonTime,2015-10-14T01:01:01.000+02:00/P1Y2M3W4DT1H2M3S)";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Overlaps(
                        new Path(modelRegistry.EP_PHENOMENONTIME),
                        new IntervalConstant("2015-10-14T01:01:01.000+02:00/P1Y2M3W4DT1H2M3S")));
        result = QueryParser.parseQuery(query, settings, path);
        assert (result.equals(expResult));

        query = "$filter=overlaps(phenomenonTime,P1D/2015-10-14T01:01:01.000+02:00)";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Overlaps(
                        new Path(modelRegistry.EP_PHENOMENONTIME),
                        new IntervalConstant("P1D/2015-10-14T01:01:01.000+02:00")));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction() {
        String query = "$filter=round(result add 0.1) eq 2";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Round(
                                new Add(
                                        new Path(modelRegistry.EP_RESULT),
                                        new DoubleConstant(0.1)
                                )
                        ),
                        new IntegerConstant(2)
                ));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testOrderByAlias() {
        String query = "$orderby=id";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getOrderBy().add(new OrderBy(new Path(ModelRegistry.EP_ID)));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testOrderByEntityProperty() {
        String query = "$orderby=@iot.id";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getOrderBy().add(new OrderBy(new Path(ModelRegistry.EP_ID)));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testOrderByAliasAscDesc() {
        String query = "$orderby=@iot.id asc,@iot.id desc";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getOrderBy().add(new OrderBy(new Path(ModelRegistry.EP_ID)));
        expResult.getOrderBy().add(new OrderBy(new Path(ModelRegistry.EP_ID), OrderBy.OrderType.DESCENDING));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testOrderByMixedPath() {
        String query = "$orderby=Datastream/@iot.id";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getOrderBy().add(new OrderBy(new Path(modelRegistry.NP_DATASTREAM, ModelRegistry.EP_ID)));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        query = "$orderby=properties/subprop/name";
        expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getOrderBy().add(
                new OrderBy(
                        new Path(
                                modelRegistry.EP_PROPERTIES,
                                new EntityPropertyCustom("subprop"),
                                new EntityPropertyCustom("name")
                        )));
        result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testSelect() {
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getSelect().add(modelRegistry.NP_OBSERVATIONS);
        expResult.getSelect().add(ModelRegistry.EP_ID);
        Query result = new Query(settings.getQueryDefaults(), path);
        result.addSelect(modelRegistry.NP_OBSERVATIONS)
                .addSelect(ModelRegistry.EP_ID);
        Assert.assertEquals(expResult, result);

        expResult.getSelect().clear();
        expResult.getSelect().add(modelRegistry.NP_THING);
        expResult.getSelect().add(ModelRegistry.EP_ID);
        result.clearSelect();
        result.addSelect(modelRegistry.NP_THING)
                .addSelect(ModelRegistry.EP_ID);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testSelectEntityProperty() {
        String query = "$select=id";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getSelect().add(ModelRegistry.EP_ID);
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testSelectDeepEntityProperty() {
        {
            String query = "$select=properties/my/type";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(modelRegistry.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("type"));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$select=properties/my[5]/type";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(modelRegistry.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$select=properties/my/5/type";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(modelRegistry.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
    }

    @Test
    public void testSelectDistinct() {
        {
            String query = "$select=distinct:id,name,properties/my/type";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult
                    .addSelect(ModelRegistry.EP_ID)
                    .addSelect(modelRegistry.EP_NAME)
                    .addSelect(new EntityPropertyCustomSelect(modelRegistry.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("type"));
            expResult.setSelectDistinct(true);
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$select=distinct:name,properties/my[5]/type";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult
                    .addSelect(modelRegistry.EP_NAME)
                    .addSelect(new EntityPropertyCustomSelect(modelRegistry.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            expResult.setSelectDistinct(true);
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$select=distinct:properties/my/5/type";
            Query expResult = new Query(settings.getQueryDefaults(), path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(modelRegistry.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            expResult.setSelectDistinct(true);
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
    }

    @Test
    public void testSelectNavigationProperty() {
        String query = "$select=Observations";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getSelect().add(modelRegistry.NP_OBSERVATIONS);
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testSelectMultipleMixed() {
        String query = "$select=Observations, id";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.addSelect(modelRegistry.NP_OBSERVATIONS)
                .addSelect(ModelRegistry.EP_ID);
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testExpandSingleNavigationProperty() {
        String query = "$expand=Observations";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getExpand().add(new Expand(modelRegistry.NP_OBSERVATIONS));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testExpandDeep() {
        String query = "$expand=Observations/FeatureOfInterest";
        Query subQuery = new Query(settings.getQueryDefaults(), path);
        subQuery.getExpand().add(new Expand(modelRegistry.NP_FEATUREOFINTEREST));
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getExpand().add(new Expand(subQuery, modelRegistry.NP_OBSERVATIONS));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testExpandCustom() {
        boolean old = settings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class);
        settings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, true);

        String query = "$expand=properties/sub/link.Thing";
        Query expResult = new Query(settings.getQueryDefaults(), path)
                .addExpand(
                        new Expand(
                                new NavigationPropertyCustom(modelRegistry, modelRegistry.EP_PROPERTIES)
                                        .addToSubPath("sub")
                                        .addToSubPath("link.Thing")
                        )
                );
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);

        settings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, old);
        Assert.assertEquals(old, settings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class));
    }

    @Test
    public void testExpandCustom2() {
        boolean old = settings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class);
        settings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, true);

        {
            String query = "$expand=Things,properties/link.Thing";
            Query expResult = new Query(settings.getQueryDefaults(), path)
                    .addExpand(new Expand(modelRegistry.NP_THINGS))
                    .addExpand(
                            new Expand(
                                    new NavigationPropertyCustom(modelRegistry, modelRegistry.EP_PROPERTIES)
                                            .addToSubPath("link.Thing")
                            )
                    );
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }
        {
            String query = "$expand=properties/link.Thing,Things";
            Query expResult = new Query(settings.getQueryDefaults(), path)
                    .addExpand(
                            new Expand(
                                    new NavigationPropertyCustom(modelRegistry, modelRegistry.EP_PROPERTIES)
                                            .addToSubPath("link.Thing")
                            )
                    )
                    .addExpand(new Expand(modelRegistry.NP_THINGS));
            Query result = QueryParser.parseQuery(query, settings, path);
            Assert.assertEquals(expResult, result);
        }

        settings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, old);
        Assert.assertEquals(old, settings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class));
    }

    @Test
    public void testExpandDeepQuery() {
        String query = "$expand=Observations/FeatureOfInterest($select=@iot.id)";
        Query subQuery = new Query(settings.getQueryDefaults(), path);
        Query subSubQuery = new Query(settings.getQueryDefaults(), path);
        subSubQuery.getSelect().add(ModelRegistry.EP_ID);
        subQuery.getExpand().add(new Expand(subSubQuery, modelRegistry.NP_FEATUREOFINTEREST));
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getExpand().add(new Expand(subQuery, modelRegistry.NP_OBSERVATIONS));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testExpandMultipleNavigationProperties() {
        String query = "$expand=Observations,ObservedProperty";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.getExpand().add(new Expand(modelRegistry.NP_OBSERVATIONS));
        expResult.getExpand().add(new Expand(modelRegistry.NP_OBSERVEDPROPERTY));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testExpandMultipleNavigationPropertiesDeep1() {
        String query = "$expand=Datastreams/Observations,Datastreams/ObservedProperty";
        Query expResult = new Query(settings.getQueryDefaults(), path)
                .addExpand(new Expand(modelRegistry.NP_DATASTREAMS)
                        .setSubQuery(new Query(settings.getQueryDefaults(), path)
                                .addExpand(new Expand(modelRegistry.NP_OBSERVATIONS))
                                .addExpand(new Expand(modelRegistry.NP_OBSERVEDPROPERTY))));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testExpandMultipleNavigationPropertiesDeep2() {
        String query = "$expand=Datastreams($expand=Observations,ObservedProperty)";
        Query expResult = new Query(settings.getQueryDefaults(), path)
                .addExpand(new Expand(modelRegistry.NP_DATASTREAMS)
                        .setSubQuery(new Query(settings.getQueryDefaults(), path)
                                .addExpand(new Expand(modelRegistry.NP_OBSERVATIONS))
                                .addExpand(new Expand(modelRegistry.NP_OBSERVEDPROPERTY))
                        ));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testExpandWithSubquery() {
        String query = "$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id;$orderby=id;$skip=5;$top=10;$count=true),ObservedProperty&$top=10";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        Query subQuery = new Query(settings.getQueryDefaults(), path);
        subQuery.setFilter(new Equal(new Path(modelRegistry.EP_RESULT), new IntegerConstant(1)));
        subQuery.getExpand().add(new Expand(modelRegistry.NP_FEATUREOFINTEREST));
        subQuery.getSelect().add(ModelRegistry.EP_ID);
        subQuery.getOrderBy().add(new OrderBy(new Path(ModelRegistry.EP_ID)));
        subQuery.setSkip(5);
        subQuery.setTop(10);
        subQuery.setCount(true);
        expResult.getExpand().add(new Expand(subQuery, modelRegistry.NP_OBSERVATIONS));
        expResult.getExpand().add(new Expand(modelRegistry.NP_OBSERVEDPROPERTY));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testComplex1() {
        String query = "$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id),ObservedProperty&$top=10";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        Query subQuery1 = new Query(settings.getQueryDefaults(), path);
        subQuery1.setFilter(new Equal(new Path(modelRegistry.EP_RESULT), new IntegerConstant(1)));
        subQuery1.getExpand().add(new Expand(modelRegistry.NP_FEATUREOFINTEREST));
        subQuery1.getSelect().add(ModelRegistry.EP_ID);
        expResult.getExpand().add(new Expand(subQuery1, modelRegistry.NP_OBSERVATIONS));
        expResult.getExpand().add(new Expand(modelRegistry.NP_OBSERVEDPROPERTY));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    @Test
    public void testFilterComplex() {
        String query = "$filter=Datastreams/Observations/FeatureOfInterest/id eq 'FOI_1' and Datastreams/Observations/resultTime ge 2010-06-01T00:00:00Z and Datastreams/Observations/resultTime le 2010-07-01T00:00:00Z";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFilter(new And(
                new Equal(
                        new Path(modelRegistry.NP_DATASTREAMS,
                                modelRegistry.NP_OBSERVATIONS,
                                modelRegistry.NP_FEATUREOFINTEREST,
                                ModelRegistry.EP_ID),
                        new StringConstant("FOI_1")),
                new And(
                        new GreaterEqual(
                                new Path(modelRegistry.NP_DATASTREAMS,
                                        modelRegistry.NP_OBSERVATIONS,
                                        modelRegistry.EP_RESULTTIME),
                                new DateTimeConstant(new DateTime(2010, 06, 01, 0, 0, DateTimeZone.UTC))),
                        new LessEqual(
                                new Path(modelRegistry.NP_DATASTREAMS,
                                        modelRegistry.NP_OBSERVATIONS,
                                        modelRegistry.EP_RESULTTIME),
                                new DateTimeConstant(new DateTime(2010, 07, 01, 0, 0, DateTimeZone.UTC))))));
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }

    // TODO add tests for all functions
    @Test
    public void testFormat() {
        String query = "$resultFormat=dataArray";
        Query expResult = new Query(settings.getQueryDefaults(), path);
        expResult.setFormat("dataArray");
        Query result = QueryParser.parseQuery(query, settings, path);
        Assert.assertEquals(expResult, result);
    }
}
