/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import static de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant.TIMEZONE_UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
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
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
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
import net.time4j.PlainTimestamp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class QueryParserTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static PluginCoreModel pluginCoreModel;
    private static ResourcePath path;

    @BeforeAll
    public static void beforeClass() {
        coreSettings = new CoreSettings();
        modelRegistry = coreSettings.getModelRegistry();
        queryDefaults = coreSettings.getQueryDefaults()
                .setAlwaysOrder(false)
                .setUseAbsoluteNavigationLinks(false);
        pluginCoreModel = new PluginCoreModel();
        pluginCoreModel.init(coreSettings);
        coreSettings.getPluginManager().initPlugins(null);
        path = new ResourcePath("http://example.org/FROST-Server", Version.V_1_0, "/Datastreams");
    }

    @Test
    void testTop() {
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        assertFalse(expResult.getTop().isPresent());
        assertEquals(ConfigUtils.getDefaultValueInt(CoreSettings.class, CoreSettings.TAG_DEFAULT_TOP), expResult.getTopOrDefault());
        expResult.setTop(10);

        String query = "$top=10";
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etThing);
        assertEquals(expResult, result);
        assertTrue(result.getTop().isPresent());
        assertEquals(10, result.getTopOrDefault());
    }

    @Test
    void testSkip() {
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        assertFalse(expResult.getSkip().isPresent());
        assertEquals(11, expResult.getSkip(11));
        expResult.setSkip(10);

        String query = "$skip=10";
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etThing);
        assertEquals(expResult, result);
        assertTrue(result.getSkip().isPresent());
        assertEquals(10, result.getSkip(11));
    }

    @Test
    void testCount() {
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        assertFalse(expResult.getCount().isPresent());
        assertEquals(ConfigUtils.getDefaultValueBoolean(CoreSettings.class, CoreSettings.TAG_DEFAULT_COUNT), expResult.isCountOrDefault());

        expResult.setCount(true);
        String query = "$count=true";
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etThing);
        assertTrue(result.getCount().isPresent());
        assertTrue(result.isCountOrDefault());
        assertEquals(expResult, result);

        expResult.setCount(false);
        query = "$count=false";
        result = QueryParser.parseQuery(query, coreSettings, path);
        assertEquals(expResult, result);
        assertFalse(result.isCountOrDefault());
    }

    @Test
    void testFilterLargeNumber() {
        String query = "$filter=result eq 800000113797";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(pluginCoreModel.epResult),
                        new IntegerConstant(800000113797L)));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterOnly1() {
        String query = "$filter=(result sub 5) gt 10";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Subtract(
                                new Path(pluginCoreModel.epResult),
                                new IntegerConstant(5)),
                        new IntegerConstant(10)));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);

        query = "$filter=14 div (result add 1) mod 3 mul 3 eq 3";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Multiply(
                                new Modulo(
                                        new Divide(
                                                new IntegerConstant(14),
                                                new Add(
                                                        new Path(pluginCoreModel.epResult),
                                                        new IntegerConstant(1))),
                                        new IntegerConstant(3)),
                                new IntegerConstant(3)),
                        new IntegerConstant(3)));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterLinked() {
        String query = "$filter=Datastream/id eq 1";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(new Equal(
                new Path(pluginCoreModel.npDatastreamObservation, pluginCoreModel.etDatastream.getPrimaryKey().getKeyProperty(0)),
                new IntegerConstant(1)));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);

        // Theoretical path, does not actually exist
        query = "$filter=Thing/Locations/location eq 1";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(new Equal(
                new Path(pluginCoreModel.npThingDatasteam, pluginCoreModel.npLocationsThing, pluginCoreModel.epLocation),
                new IntegerConstant(1)));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterInvalidCustomProperty() {
        // Theoretical path, does not actually exist
        String query = "$filter=Thing/custom eq 1";
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        assertThrows(IllegalArgumentException.class, () -> result.validate(pluginCoreModel.etDatastream));
    }

    @Test
    void testFilterString() {
        String query = "$filter=result gt '3'";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(pluginCoreModel.epResult),
                        new StringConstant("3")));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);

        query = "$filter=result eq '3'";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(pluginCoreModel.epResult),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);

        query = "$filter=result ne '3'";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new NotEqual(
                        new Path(pluginCoreModel.epResult),
                        new StringConstant("3")));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);

        query = "$filter=result eq 'it''s a quote'";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(pluginCoreModel.epResult),
                        new StringConstant("it's a quote")));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);

        query = "$filter=result eq 'it''''s two quotes'";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(pluginCoreModel.epResult),
                        new StringConstant("it''''s two quotes")));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);

        query = "$filter=description eq 'utf-8: 水位高度'";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Path(pluginCoreModel.epDescription),
                        new StringConstant("utf-8: 水位高度")));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etThing);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterCustomEntityProperty() {
        {
            String query = "$filter=properties/building.Thing/name eq 'Main'";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    modelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustomLink("building.Thing", pluginCoreModel.etThing),
                                    pluginCoreModel.epName),
                            new StringConstant("Main")));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testFilterProperty() {
        {
            String query = "$filter=properties/array[1] gt 3";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    ModelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/test_name gt 3";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    ModelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("test_name")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array[1][2] gt 3";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    ModelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]"),
                                    new EntityPropertyCustom("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array[1]/deeper[2] gt 3";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    ModelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]"),
                                    new EntityPropertyCustom("deeper"),
                                    new EntityPropertyCustom("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=properties/array/1/deeper/2 gt 3";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new GreaterThan(
                            new Path(
                                    ModelRegistry.EP_PROPERTIES,
                                    new EntityPropertyCustom("array"),
                                    new EntityPropertyCustom("[1]"),
                                    new EntityPropertyCustom("deeper"),
                                    new EntityPropertyCustom("[2]")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=location/properties/priority eq 3";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    pluginCoreModel.epLocation,
                                    new EntityPropertyCustom("properties"),
                                    new EntityPropertyCustom("priority")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etLocation);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=location/properties/4frost eq 3";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    pluginCoreModel.epLocation,
                                    new EntityPropertyCustom("properties"),
                                    new EntityPropertyCustom("4frost")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etLocation);
            assertEquals(expResult, result);
        }
        {
            String query = "$filter=location/properties/4 eq 3";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    pluginCoreModel.epLocation,
                                    new EntityPropertyCustom("properties"),
                                    new EntityPropertyCustom("4")),
                            new IntegerConstant(3)));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etLocation);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testFilterUoM() {
        {
            String query = "$filter=unitOfMeasurement/name eq 'metre'";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.setFilter(
                    new Equal(
                            new Path(
                                    pluginCoreModel.getEpUnitOfMeasurement(),
                                    new EntityPropertyCustom("name")),
                            new StringConstant("metre")));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etDatastream);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testFilterTime() {
        String query = "$filter=time gt 2015-10-14T23:30:00.104+02:00";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(pluginCoreModel.epTime),
                        DateTimeConstant.parse("2015-10-14T23:30:00.104+02:00")));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etHistoricalLocation);
        assertEquals(expResult, result);

        query = "$filter=time gt 2015-10-14T23:30:00.104+02:00 add duration'P1D'";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(pluginCoreModel.epTime),
                        new Add(
                                DateTimeConstant.parse("2015-10-14T23:30:00.104+02:00"),
                                DurationConstant.parse("P1D"))));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etHistoricalLocation);
        assertEquals(expResult, result);

        query = "$filter=time gt 2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new GreaterThan(
                        new Path(pluginCoreModel.epTime),
                        IntervalConstant.parse("2015-10-14T01:01:01.000+02:00/2015-10-14T23:30:00.104+02:00")));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etHistoricalLocation);
        assertEquals(expResult, result);

        query = "$filter=overlaps(phenomenonTime,2015-10-14T01:01:01.000+02:00/P1D)";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Overlaps(
                        new Path(pluginCoreModel.epPhenomenonTime),
                        IntervalConstant.parse("2015-10-14T01:01:01.000+02:00/P1D")));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);

        query = "$filter=overlaps(phenomenonTime,2015-10-14T01:01:01.000+02:00/P1Y2M3W4DT1H2M3S)";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Overlaps(
                        new Path(pluginCoreModel.epPhenomenonTime),
                        IntervalConstant.parse("2015-10-14T01:01:01.000+02:00/P1Y2M3W4DT1H2M3S")));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assert (result.equals(expResult));

        query = "$filter=overlaps(phenomenonTime,P1D/2015-10-14T01:01:01.000+02:00)";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Overlaps(
                        new Path(pluginCoreModel.epPhenomenonTime),
                        IntervalConstant.parse("P1D/2015-10-14T01:01:01.000+02:00")));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterFunction() {
        String query = "$filter=round(result add 0.1) eq 2";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new Equal(
                        new Round(
                                new Add(
                                        new Path(pluginCoreModel.epResult),
                                        new DoubleConstant(0.1))),
                        new IntegerConstant(2)));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);
    }

    @Test
    void testOrderByAlias() {
        String query = "$orderby=id";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getOrderBy().add(new OrderBy(new Path(pluginCoreModel.etObservation.getPrimaryKey().getKeyProperty(0))));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);
    }

    @Test
    void testOrderByEntityProperty() {
        String query = "$orderby=@iot.id";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getOrderBy().add(new OrderBy(new Path(pluginCoreModel.etObservation.getPrimaryKey().getKeyProperty(0))));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);
    }

    @Test
    void testOrderByAliasAscDesc() {
        String query = "$orderby=@iot.id asc,@iot.id desc";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getOrderBy().add(new OrderBy(new Path(pluginCoreModel.etObservation.getPrimaryKey().getKeyProperty(0))));
        expResult.getOrderBy().add(new OrderBy(new Path(pluginCoreModel.etObservation.getPrimaryKey().getKeyProperty(0)), OrderBy.OrderType.DESCENDING));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);
    }

    @Test
    void testOrderByMixedPath() {
        String query = "$orderby=Datastream/@iot.id";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getOrderBy().add(new OrderBy(new Path(pluginCoreModel.npDatastreamObservation, pluginCoreModel.etDatastream.getPrimaryKey().getKeyProperty(0))));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);

        query = "$orderby=properties/subprop/name";
        expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getOrderBy().add(
                new OrderBy(
                        new Path(
                                modelRegistry.EP_PROPERTIES,
                                new EntityPropertyCustom("subprop"),
                                new EntityPropertyCustom("name"))));
        result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etThing);
        assertEquals(expResult, result);
    }

    @Test
    void testSelect() {
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getSelect().add(pluginCoreModel.npObservationsDatastream);
        expResult.getSelect().addAll(pluginCoreModel.etObservation.getPrimaryKey().getKeyProperties());
        Query result = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        result.addSelect(pluginCoreModel.npObservationsDatastream)
                .addSelect(pluginCoreModel.etObservation.getPrimaryKey().getKeyProperties());
        assertEquals(expResult, result);

        expResult.getSelect().clear();
        expResult.getSelect().add(pluginCoreModel.npThingDatasteam);
        expResult.getSelect().add(pluginCoreModel.etThing.getPrimaryKey().getKeyProperty(0));
        result.clearSelect();
        result.addSelect(pluginCoreModel.npThingDatasteam)
                .addSelect(pluginCoreModel.etThing.getPrimaryKey().getKeyProperties());
        assertEquals(expResult, result);
    }

    @Test
    void testSelectEntityProperty() {
        String query = "$select=id";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getSelect().add(pluginCoreModel.etObservation.getPrimaryKey().getKeyProperty(0));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);
    }

    @Test
    void testSelectDeepEntityProperty() {
        {
            String query = "$select=properties/my/type";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(ModelRegistry.EP_PROPERTIES.getName())
                            .addToSubPath("my")
                            .addToSubPath("type"));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
        {
            String query = "$select=properties/my[5]/type";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(ModelRegistry.EP_PROPERTIES.getName())
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
        {
            String query = "$select=properties/my/5/type";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(ModelRegistry.EP_PROPERTIES.getName())
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testSelectDistinct() {
        {
            String query = "$select=distinct:id,name,properties/my/type";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult
                    .addSelect(pluginCoreModel.etThing.getPrimaryKey().getKeyProperties())
                    .addSelect(pluginCoreModel.epName)
                    .addSelect(new EntityPropertyCustomSelect(ModelRegistry.EP_PROPERTIES.getName())
                            .addToSubPath("my")
                            .addToSubPath("type"));
            expResult.setSelectDistinct(true);
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
        {
            String query = "$select=distinct:name,properties/my[5]/type";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult
                    .addSelect(pluginCoreModel.epName)
                    .addSelect(new EntityPropertyCustomSelect(ModelRegistry.EP_PROPERTIES.getName())
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            expResult.setSelectDistinct(true);
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
        {
            String query = "$select=distinct:properties/my/5/type";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
            expResult.getSelect().add(
                    new EntityPropertyCustomSelect(ModelRegistry.EP_PROPERTIES.getName())
                            .addToSubPath("my")
                            .addToSubPath("5")
                            .addToSubPath("type"));
            expResult.setSelectDistinct(true);
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etThing);
            assertEquals(expResult, result);
        }
    }

    @Test
    void testSelectNavigationProperty() {
        String query = "$select=Observations";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getSelect().add(pluginCoreModel.npObservationsDatastream);
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);
    }

    @Test
    void testSelectMultipleMixed() {
        String query = "$select=Observations, id";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.addSelect(pluginCoreModel.npObservationsDatastream)
                .addSelect(pluginCoreModel.etDatastream.getPrimaryKey().getKeyProperties());
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandSingleNavigationProperty() {
        String query = "$expand=Observations";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getExpand().add(new Expand(modelRegistry, pluginCoreModel.npObservationsDatastream));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandDeep() {
        String query = "$expand=Observations/FeatureOfInterest";
        Query subQuery = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        subQuery.getExpand().add(new Expand(modelRegistry, pluginCoreModel.npFeatureOfInterestObservation));
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getExpand().add(new Expand(modelRegistry, subQuery, pluginCoreModel.npObservationsDatastream));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandCustom() {
        boolean old = coreSettings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class);
        coreSettings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, true);

        String query = "$expand=properties/sub/link.Thing";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path)
                .addExpand(
                        new Expand(
                                modelRegistry,
                                new NavigationPropertyCustom(modelRegistry, ModelRegistry.EP_PROPERTIES)
                                        .addToSubPath("sub")
                                        .addToSubPath("link.Thing")));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);

        coreSettings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, old);
        assertEquals(old, coreSettings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class));
    }

    @Test
    void testExpandCustom2() {
        boolean old = coreSettings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class);
        coreSettings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, true);

        {
            String query = "$expand=Things,properties/link.Thing";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path)
                    .addExpand(new Expand(modelRegistry, pluginCoreModel.npThingsLocation))
                    .addExpand(
                            new Expand(
                                    modelRegistry,
                                    new NavigationPropertyCustom(modelRegistry, ModelRegistry.EP_PROPERTIES)
                                            .addToSubPath("link.Thing")));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etLocation);
            assertEquals(expResult, result);
        }
        {
            String query = "$expand=properties/link.Thing,Things";
            Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path)
                    .addExpand(
                            new Expand(
                                    modelRegistry,
                                    new NavigationPropertyCustom(modelRegistry, ModelRegistry.EP_PROPERTIES)
                                            .addToSubPath("link.Thing")))
                    .addExpand(new Expand(modelRegistry, pluginCoreModel.npThingsLocation));
            Query result = QueryParser.parseQuery(query, coreSettings, path);
            result.validate(pluginCoreModel.etLocation);
            assertEquals(expResult, result);
        }

        coreSettings.getExtensionSettings().set(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, old);
        assertEquals(old, coreSettings.getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class));
    }

    @Test
    void testExpandDeepQuery() {
        String query = "$expand=Observations/FeatureOfInterest($select=@iot.id)";
        Query subQuery = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        Query subSubQuery = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        subSubQuery.getSelect().add(pluginCoreModel.etFeatureOfInterest.getPrimaryKey().getKeyProperty(0));
        subQuery.getExpand().add(new Expand(modelRegistry, subSubQuery, pluginCoreModel.npFeatureOfInterestObservation));
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getExpand().add(new Expand(modelRegistry, subQuery, pluginCoreModel.npObservationsDatastream));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandMultipleNavigationProperties() {
        String query = "$expand=Observations,ObservedProperty";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.getExpand().add(new Expand(modelRegistry, pluginCoreModel.npObservationsDatastream));
        expResult.getExpand().add(new Expand(modelRegistry, pluginCoreModel.npObservedPropertyDatastream));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandMultipleNavigationPropertiesDeep1() {
        String query = "$expand=Datastreams/Observations,Datastreams/ObservedProperty";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path)
                .addExpand(new Expand(modelRegistry, pluginCoreModel.npDatastreamsThing)
                        .setSubQuery(new Query(modelRegistry, coreSettings.getQueryDefaults(), path)
                                .addExpand(new Expand(modelRegistry, pluginCoreModel.npObservationsDatastream))
                                .addExpand(new Expand(modelRegistry, pluginCoreModel.npObservedPropertyDatastream))));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etThing);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandMultipleNavigationPropertiesDeep2() {
        String query = "$expand=Datastreams($expand=Observations,ObservedProperty)";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path)
                .addExpand(new Expand(modelRegistry, pluginCoreModel.npDatastreamsThing)
                        .setSubQuery(new Query(modelRegistry, coreSettings.getQueryDefaults(), path)
                                .addExpand(new Expand(modelRegistry, pluginCoreModel.npObservationsDatastream))
                                .addExpand(new Expand(modelRegistry, pluginCoreModel.npObservedPropertyDatastream))));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etThing);
        assertEquals(expResult, result);
    }

    @Test
    void testExpandWithSubquery() {
        String query = "$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id;$orderby=id;$skip=5;$top=10;$count=true),ObservedProperty&$top=10";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        Query subQuery = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        subQuery.setFilter(new Equal(new Path(pluginCoreModel.epResult), new IntegerConstant(1)));
        subQuery.getExpand().add(new Expand(modelRegistry, pluginCoreModel.npFeatureOfInterestObservation));
        subQuery.getSelect().add(pluginCoreModel.etObservation.getPrimaryKey().getKeyProperty(0));
        subQuery.getOrderBy().add(new OrderBy(new Path(pluginCoreModel.etObservation.getPrimaryKey().getKeyProperty(0))));
        subQuery.setSkip(5);
        subQuery.setTop(10);
        subQuery.setCount(true);
        expResult.getExpand().add(new Expand(modelRegistry, subQuery, pluginCoreModel.npObservationsDatastream));
        expResult.getExpand().add(new Expand(modelRegistry, pluginCoreModel.npObservedPropertyDatastream));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);
    }

    @Test
    void testComplex1() {
        String query = "$expand=Observations($filter=result eq 1;$expand=FeatureOfInterest;$select=@iot.id),ObservedProperty&$top=10";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        Query subQuery1 = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        subQuery1.setFilter(new Equal(new Path(pluginCoreModel.epResult), new IntegerConstant(1)));
        subQuery1.getExpand().add(new Expand(modelRegistry, pluginCoreModel.npFeatureOfInterestObservation));
        subQuery1.getSelect().add(pluginCoreModel.etFeatureOfInterest.getPrimaryKey().getKeyProperty(0));
        expResult.getExpand().add(new Expand(modelRegistry, subQuery1, pluginCoreModel.npObservationsDatastream));
        expResult.getExpand().add(new Expand(modelRegistry, pluginCoreModel.npObservedPropertyDatastream));
        expResult.setTop(10);
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etDatastream);
        assertEquals(expResult, result);
    }

    @Test
    void testFilterComplex() {
        String query = "$filter=Datastreams/Observations/FeatureOfInterest/id eq 'FOI_1' and Datastreams/Observations/resultTime ge 2010-06-01T00:00:00Z and Datastreams/Observations/resultTime le 2010-07-01T00:00:00Z";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFilter(
                new And(
                        new And(
                                new Equal(
                                        new Path(pluginCoreModel.npDatastreamsThing,
                                                pluginCoreModel.npObservationsDatastream,
                                                pluginCoreModel.npFeatureOfInterestObservation,
                                                pluginCoreModel.etFeatureOfInterest.getPrimaryKey().getKeyProperty(0)),
                                        new StringConstant("FOI_1")),
                                new GreaterEqual(
                                        new Path(pluginCoreModel.npDatastreamsThing,
                                                pluginCoreModel.npObservationsDatastream,
                                                pluginCoreModel.epResultTime),
                                        new DateTimeConstant(PlainTimestamp.of(2010, 06, 01, 0, 0).inZonalView(TIMEZONE_UTC)))),
                        new LessEqual(
                                new Path(pluginCoreModel.npDatastreamsThing,
                                        pluginCoreModel.npObservationsDatastream,
                                        pluginCoreModel.epResultTime),
                                new DateTimeConstant(PlainTimestamp.of(2010, 07, 01, 0, 0).inZonalView(TIMEZONE_UTC)))));
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etThing);
        assertEquals(expResult, result);
    }

    // TODO add tests for all functions
    @Test
    void testFormat() {
        String query = "$resultFormat=dataArray";
        Query expResult = new Query(modelRegistry, coreSettings.getQueryDefaults(), path);
        expResult.setFormat("dataArray");
        Query result = QueryParser.parseQuery(query, coreSettings, path);
        result.validate(pluginCoreModel.etObservation);
        assertEquals(expResult, result);
    }
}
