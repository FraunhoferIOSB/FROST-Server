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
package de.fraunhofer.iosb.ilt.frostserver.query;

import static de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant.TIMEZONE_UTC;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended.INTERNAL_ADMIN_PRINCIPAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.DefaultFunctions;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomLink;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.PropertyReference;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.ConstantList;
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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.In;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.LessEqual;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.NotEqual;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.context.PrincipalName;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.Any;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.math.Round;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.SubstringOf;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal.Overlaps;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigUtils;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.TestModel;
import net.time4j.PlainTimestamp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QueryParserTest {

    private static CoreSettings coreSettings;
    private static ServiceContext context;
    private static TestModel testModel;
    private static ResourcePath path;

    @BeforeAll
    static void beforeClass() {
        coreSettings = new CoreSettings();
        DefaultFunctions.registerDefaultFunctions(coreSettings.getFunctionRegistry());
        coreSettings.getQueryDefaults()
                .setAlwaysOrder(false)
                .setUseAbsoluteNavigationLinks(false);
        final ModelRegistry modelRegistry = coreSettings.getModelRegistry();
        context = new ServiceContext()
                .setModelRegistry(modelRegistry)
                .setFunctionRegistry(coreSettings.getFunctionRegistry())
                .setQueryDefaults(coreSettings.getQueryDefaults());
        testModel = new TestModel();
        testModel.initModel(modelRegistry, Constants.VALUE_ID_TYPE_LONG);
        modelRegistry.initFinalise();
        path = new ResourcePath(Version.INTERNAL, "/");
    }

    @Test
    void testTop() {
        Query expResult = new Query(context, path);
        assertFalse(expResult.getTop().isPresent());
        assertEquals(ConfigUtils.getDefaultValueInt(CoreSettings.class, CoreSettings.TAG_DEFAULT_TOP), expResult.getTopOrDefault());
        expResult.setTop(10);

        String query = "$top=10";
        Query result = QueryParser.parseQuery(query, context, path);
        assertEquals(expResult, result);
        assertTrue(result.getTop().isPresent());
        assertEquals(10, result.getTopOrDefault());
    }

    @Test
    void testSkip() {
        Query expResult = new Query(context, path);
        assertFalse(expResult.getSkip().isPresent());
        assertEquals(11, expResult.getSkip(11));
        expResult.setSkip(10);

        String query = "$skip=10";
        Query result = QueryParser.parseQuery(query, context, path);
        assertEquals(expResult, result);
        assertTrue(result.getSkip().isPresent());
        assertEquals(10, result.getSkip(11));
    }

    @Test
    void testCount() {
        Query expResult = new Query(context, path);
        assertFalse(expResult.getCount().isPresent());
        assertEquals(ConfigUtils.getDefaultValueBoolean(CoreSettings.class, CoreSettings.TAG_DEFAULT_COUNT), expResult.isCountOrDefault());

        expResult.setCount(true);
        String query = "$count=true";
        Query result = QueryParser.parseQuery(query, context, path);
        assertTrue(result.getCount().isPresent());
        assertTrue(result.isCountOrDefault());
        assertEquals(expResult, result);

        expResult.setCount(false);
        query = "$count=false";
        result = QueryParser.parseQuery(query, context, path);
        assertEquals(expResult, result);
        assertFalse(result.isCountOrDefault());
    }

    @Test
    void testFilterOnly1() {
        String query = "$filter=(value sub 5) gt 10";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new GreaterThan(
                        new Subtract(
                                new Path(testModel.epValue),
                                new IntegerConstant(5)),
                        new IntegerConstant(10)));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=14 div (value add 1) mod 3 mul 3 eq 3";
        expResult = new Query(context, path);
        expResult.setFilter(
                new Equal(
                        new Multiply(
                                new Modulo(
                                        new Divide(
                                                new IntegerConstant(14),
                                                new Add(
                                                        new Path(testModel.epValue),
                                                        new IntegerConstant(1))),
                                        new IntegerConstant(3)),
                                new IntegerConstant(3)),
                        new IntegerConstant(3)));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterLinked() {
        String query = "$filter=id eq 1";
        Query expResult = new Query(context, path);
        expResult.setFilter(new Equal(
                new Path(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0)),
                new IntegerConstant(1)));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=House/id eq 1";
        expResult = new Query(context, path);
        expResult.setFilter(new Equal(
                new Path(testModel.npRoomHouse, testModel.etHouse.getPrimaryKey().getKeyProperties().get(0)),
                new IntegerConstant(1)));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        // Theoretical path, does not actually exist
        query = "$filter=House/Rooms/name eq 1";
        expResult = new Query(context, path);
        expResult.setFilter(new Equal(
                new Path(testModel.npRoomHouse, testModel.npHouseRooms, testModel.epName),
                new IntegerConstant(1)));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterInvalidNavPath() {
        String query = "$filter=House/Room/id eq 1";
        Query result = QueryParser.parseQuery(query, context, path);
        assertThrows(IllegalArgumentException.class, () -> result.validate(testModel.etRoom));
    }

    @Test
    void testFilterInvalidCustomProperty() {
        String query = "$filter=House/custom eq 1";
        Query result = QueryParser.parseQuery(query, context, path);
        assertThrows(IllegalArgumentException.class, () -> result.validate(testModel.etRoom));
    }

    @Test
    void testFilterInvalid() {
        String query = "$filter=add and eq 1";
        assertThrows(IllegalArgumentException.class, () -> QueryParser.parseQuery(query, context, path));
    }

    @Test
    void testFilterString() {
        String query = "$filter=value gt '3'";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(testModel.epValue),
                        new StringConstant("3")));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=value eq '3'";
        expResult = new Query(context, path);
        expResult.setFilter(
                new Equal(
                        new Path(testModel.epValue),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=value ne '3'";
        expResult = new Query(context, path);
        expResult.setFilter(
                new NotEqual(
                        new Path(testModel.epValue),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=value eq 'it''s a quote'";
        expResult = new Query(context, path);
        expResult.setFilter(
                new Equal(
                        new Path(testModel.epValue),
                        new StringConstant("it's a quote")));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=value eq 'it''''s two quotes'";
        expResult = new Query(context, path);
        expResult.setFilter(
                new Equal(
                        new Path(testModel.epValue),
                        new StringConstant("it''''s two quotes")));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=name eq 'utf-8: 水位高度'";
        expResult = new Query(context, path);
        expResult.setFilter(
                new Equal(
                        new Path(testModel.epName),
                        new StringConstant("utf-8: 水位高度")));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterIn() {
        String query = "$filter=value in ('3', '4')";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new In(
                        new Path(testModel.epValue),
                        new ConstantList()
                                .addItem(new StringConstant("3"))
                                .addItem(new StringConstant("4"))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=value in (3, 4)";
        expResult = new Query(context, path);
        expResult.setFilter(
                new In(
                        new Path(testModel.epValue),
                        new ConstantList()
                                .addItem(new IntegerConstant(3))
                                .addItem(new IntegerConstant(4))));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter='tag' in properties/tags";
        expResult = new Query(context, path);
        expResult.setFilter(
                new In(
                        new StringConstant("tag"),
                        new Path(StandardProperties.EP_PROPERTIES,
                                new EntityPropertyCustom("tags"))));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterCustomEntityProperty() {
        String query = "$filter=properties/building.House/name eq 'Main'";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new Equal(
                        new Path(
                                StandardProperties.EP_PROPERTIES,
                                new EntityPropertyCustomLink("building.House", testModel.etHouse),
                                testModel.epName),
                        new StringConstant("Main")));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterProperty() {
        {
            String query = "$filter=properties/array[1] gt 3";
            Query expResult = new Query(context, path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    StandardProperties.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etRoom);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/test_name gt 3";
            Query expResult = new Query(context, path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    StandardProperties.EP_PROPERTIES,
                                    new EntityPropertyCustom("test_name")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etRoom);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array[1][2] gt 3";
            Query expResult = new Query(context, path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    StandardProperties.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]"),
                                    new EntityPropertyCustom("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etRoom);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array[1]/deeper[2] gt 3";
            Query expResult = new Query(context, path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    StandardProperties.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]"),
                                    new EntityPropertyCustom("deeper"),
                                    new EntityPropertyCustom("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etRoom);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array/1/deeper/2 gt 3";
            Query expResult = new Query(context, path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    StandardProperties.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]"),
                                    new EntityPropertyCustom("deeper"),
                                    new EntityPropertyCustom("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etRoom);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testFilterTime() {
        String query = "$filter=time gt 2015-10-14T23:30:00.104+02:00";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(testModel.epTime),
                        DateTimeConstant.parse("2015-10-14T23:30:00.104+02:00")));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=time gt 2015-10-14T23:30:00.104+02:00 add duration'P1D'";
        expResult = new Query(context, path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(testModel.epTime),
                        new Add(
                                DateTimeConstant.parse("2015-10-14T23:30:00.104+02:00"),
                                DurationConstant.parse("P1D"))));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=time gt 2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00";
        expResult = new Query(context, path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(testModel.epTime),
                        IntervalConstant.parse("2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00")));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=overlaps(time,2015-10-14T01:01:01.000+02:00/P1D)";
        expResult = new Query(context, path);
        expResult.setFilter(
                new Overlaps(
                        new Path(testModel.epTime),
                        IntervalConstant.parse("2015-10-14T01:01:01.000+02:00/P1D")));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$filter=overlaps(time,2015-10-14T01:01:01.000+02:00/P1Y2M3W4DT1H2M3S)";
        expResult = new Query(context, path);
        expResult.setFilter(
                new Overlaps(
                        new Path(testModel.epTime),
                        IntervalConstant.parse("2015-10-14T01:01:01.000+02:00/P1Y2M3W4DT1H2M3S")));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assert (result.equals(expResult));

        query = "$filter=overlaps(time,P1D/2015-10-14T01:01:01.000+02:00)";
        expResult = new Query(context, path);
        expResult.setFilter(
                new Overlaps(
                        new Path(testModel.epTime),
                        IntervalConstant.parse("P1D/2015-10-14T01:01:01.000+02:00")));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterFunction() {
        String query = "$filter=round(value add 0.1) eq 2";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new Equal(
                        new Round(
                                new Add(
                                        new Path(testModel.epValue),
                                        new DoubleConstant(0.1))),
                        new IntegerConstant(2)));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterFunctionBad() {
        String query = "$filter=round(value add 0.1)";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new Round(
                        new Add(
                                new Path(testModel.epValue),
                                new DoubleConstant(0.1))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterFunctionBoolean() {
        String query = "$filter=substringof('Things', 'SensorThings')";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new SubstringOf(
                        new StringConstant("Things"),
                        new StringConstant("SensorThings")));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterFunctionBooleanBad() {
        String query = "$filter=substringof('Things', 'SensorThings') eq true";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new Equal(
                        new SubstringOf(
                                new StringConstant("Things"),
                                new StringConstant("SensorThings")),
                        new BooleanConstant(true)));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterFunctionAdminOnly() {
        String query = "$filter=name eq principalName()";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new Equal(
                        new Path(testModel.epName),
                        new PrincipalName()));

        assertThrows(IllegalArgumentException.class, () -> {
            QueryParser.parseQuery(query, context, path);
        });

        Query result = QueryParser.parseQuery(query, context, path, INTERNAL_ADMIN_PRINCIPAL);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testOrderByAlias() {
        String query = "$orderby=id";
        Query expResult = new Query(context, path);
        expResult.getOrderBy().add(new OrderBy(new Path(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testOrderByEntityProperty() {
        String query = "$orderby=@iot.id";
        Query expResult = new Query(context, path);
        expResult.getOrderBy().add(new OrderBy(new Path(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testOrderByAliasAscDesc() {
        String query = "$orderby=@iot.id asc,@iot.id desc";
        Query expResult = new Query(context, path);
        expResult.getOrderBy().add(new OrderBy(new Path(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0))));
        expResult.getOrderBy().add(new OrderBy(new Path(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0)), OrderBy.OrderType.DESCENDING));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testOrderByMixedPath() {
        String query = "$orderby=House/@iot.id";
        Query expResult = new Query(context, path);
        expResult.getOrderBy().add(new OrderBy(new Path(testModel.npRoomHouse, testModel.etHouse.getPrimaryKey().getKeyProperties().get(0))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);

        query = "$orderby=properties/subprop/name";
        expResult = new Query(context, path);
        expResult.getOrderBy().add(
                new OrderBy(
                        new Path(
                                StandardProperties.EP_PROPERTIES,
                                new EntityPropertyCustom("subprop"),
                                new EntityPropertyCustom("name"))));
        result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testSelect() {
        Query expResult = new Query(context, path);
        expResult.getSelect().add(testModel.npHouseRooms);
        expResult.getSelect().add(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0));
        Query result = new Query(context, path);
        result.addSelect(testModel.npHouseRooms)
                .addSelect(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0));
        assertEquals(expResult, result);

        expResult.getSelect().clear();
        expResult.getSelect().add(testModel.npRoomHouse);
        expResult.getSelect().add(testModel.etRoom.getPrimaryKey().getKeyProperties().get(0));
        result.clearSelect();
        result.addSelect(testModel.npRoomHouse)
                .addSelect(testModel.etRoom.getPrimaryKey().getKeyProperties().get(0));
        assertEquals(expResult, result);
    }

    @Test
    void testSelectEntityProperty() {
        String query = "$select=id";
        Query expResult = new Query(context, path);
        expResult.getSelect().add(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testSelectDeepEntityProperty() {
        {
            String query = "$select=properties/my/type";
            Query expResult = new Query(context, path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(StandardProperties.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("type"));
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etHouse);
            assertEquals(expResult, result);
        }
        {
            String query = "$select=properties/my[5]/type";
            Query expResult = new Query(context, path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(StandardProperties.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etHouse);
            assertEquals(expResult, result);
        }
        {
            String query = "$select=properties/my/5/type";
            Query expResult = new Query(context, path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(StandardProperties.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etHouse);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testSelectDistinct() {
        {
            String query = "$select=distinct:id,name,properties/my/type";
            Query expResult = new Query(context, path);
            expResult
                    .addSelect(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0))
                    .addSelect(testModel.epName)
                    .addSelect(new EntityPropertyCustomSelect(StandardProperties.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("type"));
            expResult.setSelectDistinct(true);
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etHouse);
            assertEquals(expResult, result);
        }
        {
            String query = "$select=distinct:name,properties/my[5]/type";
            Query expResult = new Query(context, path);
            expResult
                    .addSelect(testModel.epName)
                    .addSelect(new EntityPropertyCustomSelect(StandardProperties.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            expResult.setSelectDistinct(true);
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etHouse);
            assertEquals(expResult, result);
        }
        {
            String query = "$select=distinct:properties/my/5/type";
            Query expResult = new Query(context, path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(StandardProperties.EP_PROPERTIES)
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            expResult.setSelectDistinct(true);
            Query result = QueryParser.parseQuery(query, context, path);
            result.validate(testModel.etHouse);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testSelectNavigationProperty() {
        String query = "$select=Rooms";
        Query expResult = new Query(context, path);
        expResult.getSelect().add(testModel.npHouseRooms);
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testSelectMultipleMixed() {
        String query = "$select=Rooms, id";
        Query expResult = new Query(context, path);
        expResult.addSelect(testModel.npHouseRooms)
                .addSelect(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandSingleNavigationProperty() {
        String query = "$expand=Rooms";
        Query expResult = new Query(context, path);
        expResult.getExpand().add(new Expand(testModel.npHouseRooms));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandSingleNavigationPropertyEmpty() {
        String query = "$expand=Rooms()";
        Query expResult = new Query(context, path);
        expResult.getExpand().add(new Expand(testModel.npHouseRooms));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandDeep() {
        String query = "$expand=Rooms/House";
        Query subQuery = new Query(context, path);
        subQuery.getExpand().add(new Expand(testModel.npRoomHouse));
        Query expResult = new Query(context, path);
        expResult.getExpand().add(new Expand(subQuery, testModel.npHouseRooms));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandCustom() {
        boolean old = coreSettings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class);
        coreSettings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, true);

        String query = "$expand=properties/sub/link.House";
        Query expResult = new Query(context, path)
                .addExpand(
                        new Expand(
                                new NavigationPropertyCustom(context.getModelRegistry(), StandardProperties.EP_PROPERTIES)
                                        .addToSubPath("sub")
                                        .addToSubPath("link.House")));
        Query result = QueryParser.parseQuery(query, context, path);
        assertEquals(expResult, result);

        coreSettings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, old);
        assertEquals(old, coreSettings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class));
    }

    @Test
    void testExpandCustom2() {
        boolean old = coreSettings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class);
        coreSettings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, true);

        {
            String query = "$expand=Houses,properties/link.House";
            Query expResult = new Query(context, path)
                    .addExpand(new Expand(testModel.npHouses))
                    .addExpand(
                            new Expand(
                                    new NavigationPropertyCustom(context.getModelRegistry(), StandardProperties.EP_PROPERTIES)
                                            .addToSubPath("link.House")));
            Query result = QueryParser.parseQuery(query, context, path);
            assertEquals(expResult, result);
        }
        {
            String query = "$expand=properties/link.House,Houses";
            Query expResult = new Query(context, path)
                    .addExpand(
                            new Expand(
                                    new NavigationPropertyCustom(context.getModelRegistry(), StandardProperties.EP_PROPERTIES)
                                            .addToSubPath("link.House")))
                    .addExpand(new Expand(testModel.npHouses));
            Query result = QueryParser.parseQuery(query, context, path);
            assertEquals(expResult, result);
        }

        coreSettings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, old);
        assertEquals(old, coreSettings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class));
    }

    @Test
    void testExpandDeepQuery() {
        String query = "$expand=Rooms/House($select=@iot.id)";
        Query subQuery = new Query(context, path);
        Query subSubQuery = new Query(context, path);
        subSubQuery.getSelect().add(testModel.etHouse.getPrimaryKey().getKeyProperties().get(0));
        subQuery.getExpand().add(new Expand(subSubQuery, testModel.npRoomHouse));
        Query expResult = new Query(context, path);
        expResult.getExpand().add(new Expand(subQuery, testModel.npHouseRooms));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandMultipleNavigationProperties() {
        String query = "$expand=Rooms,House";
        Query expResult = new Query(context, path);
        expResult.getExpand().add(new Expand(testModel.npHouseRooms));
        expResult.getExpand().add(new Expand(testModel.npRoomHouse));
        Query result = QueryParser.parseQuery(query, context, path);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandMultipleNavigationPropertiesDeep1() {
        String query = "$expand=Rooms/House,Rooms/Rooms";
        Query expResult = new Query(context, path)
                .addExpand(new Expand(testModel.npHouseRooms)
                        .setSubQuery(new Query(context, path)
                                .addExpand(new Expand(testModel.npRoomHouse))
                                .addExpand(new Expand(testModel.npHouseRooms))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandMultipleNavigationPropertiesDeep2() {
        String query = "$expand=Houses($expand=Rooms,House)";
        Query expResult = new Query(context, path)
                .addExpand(new Expand(testModel.npHouses)
                        .setSubQuery(new Query(context, path)
                                .addExpand(new Expand(testModel.npHouseRooms))
                                .addExpand(new Expand(testModel.npRoomHouse))));
        Query result = QueryParser.parseQuery(query, context, path);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandWithSubquery() {
        String query = "$expand=Rooms($filter=value eq 1;$expand=House;$select=@iot.id;$orderby=id;$skip=5;$top=10;$count=true),House&$top=10";
        Query expResult = new Query(context, path);
        Query subQuery = new Query(context, path);
        subQuery.setFilter(new Equal(new Path(testModel.epValue), new IntegerConstant(1)));
        subQuery.getExpand().add(new Expand(testModel.npRoomHouse));
        subQuery.getSelect().add(testModel.etRoom.getPrimaryKey().getKeyProperties().get(0));
        subQuery.getOrderBy().add(new OrderBy(new Path(testModel.etRoom.getPrimaryKey().getKeyProperties().get(0))));
        subQuery.setSkip(5);
        subQuery.setTop(10);
        subQuery.setCount(true);
        expResult.getExpand().add(new Expand(subQuery, testModel.npHouseRooms));
        expResult.getExpand().add(new Expand(testModel.npRoomHouse));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testComplex1() {
        String query = "$expand=Rooms($filter=value eq 1;$expand=House;$select=@iot.id),House&$top=10";
        Query expResult = new Query(context, path);
        Query subQuery1 = new Query(context, path);
        subQuery1.setFilter(new Equal(new Path(testModel.epValue), new IntegerConstant(1)));
        subQuery1.getExpand().add(new Expand(testModel.npRoomHouse));
        subQuery1.getSelect().add(testModel.etRoom.getPrimaryKey().getKeyProperties().get(0));
        expResult.getExpand().add(new Expand(subQuery1, testModel.npHouseRooms));
        expResult.getExpand().add(new Expand(testModel.npRoomHouse));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterComplex() {
        String query = "$filter=Rooms/House/id eq 'FOI_1' and House/Rooms/time ge 2010-06-01T00:00:00Z and House/Rooms/time le 2010-07-01T00:00:00Z";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new And(
                        new And(
                                new Equal(
                                        new Path(
                                                testModel.npHouseRooms,
                                                testModel.npRoomHouse,
                                                testModel.etHouse.getPrimaryKey().getKeyProperties().get(0)),
                                        new StringConstant("FOI_1")),
                                new GreaterEqual(
                                        new Path(testModel.npRoomHouse,
                                                testModel.npHouseRooms,
                                                testModel.epTime),
                                        new DateTimeConstant(PlainTimestamp.of(2010, 06, 01, 0, 0).inZonalView(TIMEZONE_UTC)))),
                        new LessEqual(
                                new Path(testModel.npRoomHouse,
                                        testModel.npHouseRooms,
                                        testModel.epTime),
                                new DateTimeConstant(PlainTimestamp.of(2010, 07, 01, 0, 0).inZonalView(TIMEZONE_UTC)))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etRoom);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterAny1() {
        String query = "$filter=Rooms/any(r : r/time le 2010-07-01T00:00:00Z)";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new Any(
                        new Path(testModel.npHouseRooms),
                        "r",
                        new LessEqual(
                                new Path(
                                        new PropertyReference("r", testModel.npHouseRooms),
                                        testModel.epTime),
                                new DateTimeConstant(PlainTimestamp.of(2010, 07, 01, 0, 0).inZonalView(TIMEZONE_UTC)))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterAny2() {
        String query = "$filter=Rooms/any(r : r/time le 2010-07-01T00:00:00Z) and Rooms/any(r : r/time ge 2010-07-01T00:00:00Z)";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new And(
                        new Any(
                                new Path(testModel.npHouseRooms),
                                "r",
                                new LessEqual(
                                        new Path(
                                                new PropertyReference("r", testModel.npHouseRooms),
                                                testModel.epTime),
                                        new DateTimeConstant(PlainTimestamp.of(2010, 07, 01, 0, 0).inZonalView(TIMEZONE_UTC)))),
                        new Any(
                                new Path(testModel.npHouseRooms),
                                "r",
                                new GreaterEqual(
                                        new Path(
                                                new PropertyReference("r", testModel.npHouseRooms),
                                                testModel.epTime),
                                        new DateTimeConstant(PlainTimestamp.of(2010, 07, 01, 0, 0).inZonalView(TIMEZONE_UTC))))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterAnyNested() {
        String query = "$filter=Rooms/any(r1 : r1/time le 2010-07-01T00:00:00Z and r1/House/Rooms/any(r2 : r2/time ge 2010-07-01T00:00:00Z))";
        Query expResult = new Query(context, path);
        expResult.setFilter(
                new Any(
                        new Path(testModel.npHouseRooms),
                        "r1",
                        new And(
                                new LessEqual(
                                        new Path(
                                                new PropertyReference("r1", testModel.npHouseRooms),
                                                testModel.epTime),
                                        new DateTimeConstant(PlainTimestamp.of(2010, 07, 01, 0, 0).inZonalView(TIMEZONE_UTC))),
                                new Any(
                                        new Path(
                                                new PropertyReference("r1", testModel.npHouseRooms),
                                                testModel.npRoomHouse,
                                                testModel.npHouseRooms),
                                        "r2",
                                        new GreaterEqual(
                                                new Path(
                                                        new PropertyReference("r2", testModel.npHouseRooms),
                                                        testModel.epTime),
                                                new DateTimeConstant(PlainTimestamp.of(2010, 07, 01, 0, 0).inZonalView(TIMEZONE_UTC)))))));
        Query result = QueryParser.parseQuery(query, context, path);
        result.validate(testModel.etHouse);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterAnyDupicateName() {
        String queryString = "$filter=Rooms/any(r : r/time le 2010-07-01T00:00:00Z and r/House/Rooms/any(r : r/time ge 2010-07-01T00:00:00Z))";
        final Query query = QueryParser.parseQuery(queryString, context, path);
        assertThrows(IllegalArgumentException.class, () -> {
            query.validate(testModel.etHouse);
        });
    }

    // TODO add tests for all functions
    @Test
    void testFormat() {
        String query = "$resultFormat=dataArray";
        Query expResult = new Query(context, path);
        expResult.setFormat("dataArray");
        Query result = QueryParser.parseQuery(query, context, path);
        assertEquals(expResult, result);
    }

    @Test
    void testDeleteRefWithId() {
        String query = "$id=../../Rooms(1)";
        Query result = QueryParser.parseQuery(query, context, path);
        Query expResult = new Query(context, path);
        assertNotEquals(expResult, result);
        expResult.setId("../../Rooms(1)");
        assertEquals(expResult, result);

    }
}
