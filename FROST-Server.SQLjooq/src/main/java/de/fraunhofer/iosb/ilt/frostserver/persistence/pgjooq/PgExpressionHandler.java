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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.ArrayConstandFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.FieldListWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.FieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory.JsonFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.NullWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.SimpleFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaDateTimeWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaDurationWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.TimeFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.WrapperHelper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomLink;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.ConstantList;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.GeoJsonConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntervalConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.LineStringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.NullConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PointConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PolygonConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.TimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.LessThan;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.NotEqual;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.context.ContextEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.context.PrincipalName;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.Date;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.Day;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.FractionalSeconds;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.Hour;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.MaxDateTime;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.MinDateTime;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.Minute;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.Month;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.Now;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.Second;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.Time;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.TotalOffsetMinutes;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date.Year;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.Not;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.Or;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.math.Ceiling;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.math.Floor;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.math.Round;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.GeoDistance;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.GeoIntersects;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.GeoLength;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.STContains;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.STCrosses;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.STDisjoint;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.STEquals;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.STIntersects;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.STOverlaps;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.STRelate;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.STTouches;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.spatialrelation.STWithin;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.Concat;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.EndsWith;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.IndexOf;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.Length;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.StartsWith;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.Substring;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.SubstringOf;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.ToLower;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.ToUpper;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string.Trim;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal.After;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal.Before;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal.During;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal.Finishes;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal.Meets;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal.Overlaps;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal.Starts;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.PlainTime;
import net.time4j.ZonalDateTime;
import net.time4j.range.MomentInterval;
import org.geojson.GeoJsonObject;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.jooq.Condition;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hylke van der Schaaf
 */
public class PgExpressionHandler implements ExpressionVisitor<FieldWrapper> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PgExpressionHandler.class);
    private static final String ST_GEOM_FROM_EWKT = "ST_GeomFromEWKT(?)";

    private final QueryBuilder queryBuilder;
    /**
     * The table reference for the main table of the request.
     */
    private final TableRef tableRef;

    private int maxCustomLinkDepth = -1;

    public PgExpressionHandler(CoreSettings settings, QueryBuilder queryBuilder, TableRef tableRef) {
        this.queryBuilder = queryBuilder;
        this.tableRef = tableRef;
        final Settings experimentalSettings = settings.getExtensionSettings();
        if (experimentalSettings.getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class)) {
            maxCustomLinkDepth = experimentalSettings.getInt(CoreSettings.TAG_CUSTOM_LINKS_RECURSE_DEPTH, CoreSettings.class);
        }
    }

    public Condition addFilterToWhere(Expression filter, Condition sqlWhere) {
        FieldWrapper filterField = filter.accept(this);
        if (filterField.isCondition()) {
            return sqlWhere.and(filterField.getCondition());

        }
        if (filterField instanceof FieldListWrapper listExpression) {
            for (Field expression : listExpression.getExpressions().values()) {
                if (Boolean.class.isAssignableFrom(expression.getType())) {
                    Field<Boolean> predicate = expression;
                    return sqlWhere.and(predicate);
                }
            }
        }
        LOGGER.error("Filter is not a predicate but a {}.", filterField.getClass().getName());
        throw new IllegalArgumentException("Filter is not a predicate but a " + filterField.getClass().getName());
    }

    public void addOrderbyToQuery(OrderBy orderBy, Utils.SortSelectFields orderFields) {
        FieldWrapper resultExpression = orderBy.getExpression().accept(this);
        if (resultExpression instanceof StaTimeIntervalWrapper timeInterval) {
            addToQuery(orderBy, timeInterval.getStart(), orderFields);
            addToQuery(orderBy, timeInterval.getEnd(), orderFields);
            return;
        }
        if (resultExpression instanceof StaDurationWrapper duration) {
            addToQuery(orderBy, duration.getDuration(), orderFields);
            return;
        }
        if (resultExpression instanceof StaDateTimeWrapper dateTime) {
            addToQuery(orderBy, dateTime.getDateTime(), orderFields);
            return;
        }
        if (resultExpression instanceof FieldListWrapper fieldListWrapper) {
            for (Field sqlExpression : fieldListWrapper.getExpressionsForOrder().values()) {
                addToQuery(orderBy, sqlExpression, orderFields);
            }
            return;
        }
        Field field = resultExpression.getDefaultField();
        addToQuery(orderBy, field, orderFields);
    }

    public void addToQuery(OrderBy orderBy, Field field, Utils.SortSelectFields orderFields) {
        orderFields.add(field, orderBy.getType());
    }

    @Override
    public FieldWrapper visit(Path path) {
        PathState state = new PathState();
        state.pathTableRef = tableRef;
        state.elements = path.getElements();
        for (state.curIndex = 0; state.curIndex < state.elements.size() && !state.finished; state.curIndex++) {
            Property element = state.elements.get(state.curIndex);
            if (element instanceof EntityPropertyCustom) {
                handleCustomProperty(state, path);

            } else if (element instanceof EntityPropertyCustomLink) {
                handleCustomProperty(state, path);

            } else if (element instanceof EntityPropertyMain entityPropertyMain) {
                handleEntityProperty(state, path, entityPropertyMain);

            } else if (element instanceof NavigationPropertyMain navigationPropertyMain) {
                handleNavigationProperty(state, path, navigationPropertyMain);
            }
        }
        if (state.finalExpression == null) {
            throw new IllegalArgumentException("Path does not end in an EntityProperty: " + path);
        }
        if (state.finalExpression instanceof Field field) {
            if (Moment.class.isAssignableFrom(field.getType())) {
                Field<Moment> dateTimePath = (Field<Moment>) state.finalExpression;
                state.finalExpression = new StaDateTimeWrapper(dateTimePath);
            }
        }
        return state.finalExpression;
    }

    private void handleCustomProperty(PathState state, Path path) {
        if (state.finalExpression == null) {
            throw new IllegalArgumentException("CustomProperty must follow an EntityProperty: " + path);
        }
        // generate finalExpression::jsonb#>>'{x,y,z}'
        JsonFieldWrapper jsonFactory;
        if (state.finalExpression instanceof JsonFieldWrapper jsonFieldWrapper) {
            jsonFactory = jsonFieldWrapper;
        } else {
            jsonFactory = new JsonFieldWrapper(state.finalExpression);
        }
        for (; state.curIndex < state.elements.size(); state.curIndex++) {
            final Property property = state.elements.get(state.curIndex);
            String name = property.getName();
            if (property instanceof EntityPropertyCustomLink epcl) {
                int maxDepth = state.curIndex + maxCustomLinkDepth;
                if (state.curIndex <= maxDepth) {
                    handleCustomLink(epcl, jsonFactory, name, state);
                    return;
                } else {
                    jsonFactory.addToPath(name);
                }
            } else {
                jsonFactory.addToPath(name);
            }
        }
        state.finalExpression = jsonFactory.materialise();
        state.finished = true;
    }

    private void handleCustomLink(final EntityPropertyCustomLink epcl, JsonFieldWrapper jsonFactory, String name, PathState state) {
        JsonFieldFactory.JsonFieldWrapper sourceIdFieldWrapper = jsonFactory.addToPath(name + AT_IOT_ID).materialise();
        Field<Number> sourceIdField = sourceIdFieldWrapper.getFieldAsType(Number.class, true);
        state.pathTableRef = queryBuilder.queryEntityType(epcl, state.pathTableRef, sourceIdField);
        state.finalExpression = null;
    }

    private void handleEntityProperty(PathState state, Path path, EntityPropertyMain element) {
        if (state.finalExpression != null) {
            throw new IllegalArgumentException("EntityProperty can not follow an other EntityProperty: " + path);
        }
        Map<String, Field> pathExpressions = state.pathTableRef.getTable()
                .getPropertyFieldRegistry()
                .getAllFieldsForProperty(element, new LinkedHashMap<>());
        if (pathExpressions.size() == 1) {
            state.finalExpression = WrapperHelper.wrapField(pathExpressions.values().iterator().next());
        } else {
            state.finalExpression = getSubExpression(state, pathExpressions);
        }
    }

    private void handleNavigationProperty(PathState state, Path path, NavigationPropertyMain np) {
        if (state.finalExpression != null) {
            throw new IllegalArgumentException("NavigationProperty can not follow an EntityProperty: " + path);
        }
        state.pathTableRef = queryBuilder.queryEntityType(np, state.pathTableRef);
    }

    private FieldWrapper getSubExpression(PathState state, Map<String, Field> pathExpressions) {
        int nextIdx = state.curIndex + 1;
        if (state.elements.size() > nextIdx) {
            Property subProperty = state.elements.get(nextIdx);
            // If the subProperty is unknown, and the expression can be of type JSON,
            // then we assume JSON.
            if (!pathExpressions.containsKey(subProperty.getName()) && pathExpressions.containsKey("j")) {
                return new SimpleFieldWrapper(pathExpressions.get("j"));
            }
            // We can not accept json, so the subProperty must be a known direction.
            state.finished = true;
            return WrapperHelper.wrapField(pathExpressions.get(subProperty.getName()));
        }
        if (pathExpressions.containsKey(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START)
                && pathExpressions.containsKey(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END)) {
            return new StaTimeIntervalWrapper(pathExpressions);
        }
        return new FieldListWrapper(pathExpressions);
    }

    public Field[] findPair(FieldWrapper p1, FieldWrapper p2) {
        Field[] result = new Field[2];

        result[0] = p1.getFieldAsType(Number.class, true);
        result[1] = p2.getFieldAsType(Number.class, true);
        if (result[0] != null && result[1] != null) {
            return result;
        }

        result[0] = p1.getFieldAsType(Boolean.class, true);
        result[1] = p2.getFieldAsType(Boolean.class, true);
        if (result[0] != null && result[1] != null) {
            return result;
        }

        // If both are strings, use strings.
        result[0] = p1.getFieldAsType(String.class, true);
        result[1] = p2.getFieldAsType(String.class, true);
        if (result[0] != null && result[1] != null) {
            return result;
        }

        LOGGER.warn("Could not match types for {} and {}", p1, p2);
        result[0] = p1.getDefaultField();
        result[1] = p2.getDefaultField();
        return result;
    }

    @Override
    public FieldWrapper visit(BooleanConstant node) {
        return new SimpleFieldWrapper(Boolean.TRUE.equals(node.getValue()) ? DSL.condition("TRUE") : DSL.condition("FALSE"));
    }

    @Override
    public FieldWrapper visit(DateConstant node) {
        PlainDate date = node.getValue();
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        instance.set(date.getYear(), date.getMonth() - 1, date.getDayOfMonth());
        return new SimpleFieldWrapper(DSL.inline(new java.sql.Date(instance.getTimeInMillis())));
    }

    @Override
    public FieldWrapper visit(DateTimeConstant node) {
        ZonalDateTime value = node.getValue();
        return new StaDateTimeWrapper(value.toMoment(), true);
    }

    @Override
    public FieldWrapper visit(DoubleConstant node) {
        return new SimpleFieldWrapper(DSL.val(node.getValue()));
    }

    @Override
    public FieldWrapper visit(DurationConstant node) {
        return new StaDurationWrapper(node);
    }

    @Override
    public FieldWrapper visit(IntervalConstant node) {
        MomentInterval value = node.getValue();
        return new StaTimeIntervalWrapper(
                value.getStartAsMoment(),
                value.getEndAsMoment());
    }

    @Override
    public FieldWrapper visit(IntegerConstant node) {
        return new SimpleFieldWrapper(DSL.val(node.getValue()));
    }

    @Override
    public FieldWrapper visit(LineStringConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new SimpleFieldWrapper(DSL.field(ST_GEOM_FROM_EWKT, PostGisGeometryBinding.dataType(), geom.asText()));
    }

    @Override
    public FieldWrapper visit(NullConstant node) {
        return new NullWrapper();
    }

    @Override
    public FieldWrapper visit(PointConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new SimpleFieldWrapper(DSL.field(ST_GEOM_FROM_EWKT, PostGisGeometryBinding.dataType(), geom.asText()));
    }

    @Override
    public FieldWrapper visit(PolygonConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new SimpleFieldWrapper(DSL.field(ST_GEOM_FROM_EWKT, PostGisGeometryBinding.dataType(), geom.asText()));
    }

    private static Geometry fromGeoJsonConstant(GeoJsonConstant<? extends GeoJsonObject> node) {
        if (node.getValue().getCrs() == null) {
            return Wkt.fromWkt("SRID=4326;" + node.getSource());
        }
        return Wkt.fromWkt(node.getSource());
    }

    @Override
    public FieldWrapper visit(StringConstant node) {
        return new SimpleFieldWrapper(DSL.value(node.getValue()));
    }

    @Override
    public FieldWrapper visit(TimeConstant node) {
        PlainTime time = node.getValue();
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        instance.set(1970, 1, 1, time.getHour(), time.getMinute(), time.getSecond());
        return new SimpleFieldWrapper(DSL.inline(new java.sql.Time(instance.getTimeInMillis())));
    }

    @Override
    public FieldWrapper visit(ConstantList node) {
        return new ArrayConstandFieldWrapper(node);
    }

    @Override
    public FieldWrapper visit(Before node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper timeExpression) {
            return timeExpression.before(p2);
        }
        throw new IllegalArgumentException("Before can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(After node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper timeExpression) {
            return timeExpression.after(p2);
        }
        throw new IllegalArgumentException("After can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Meets node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper timeExpression) {
            return timeExpression.meets(p2);
        }
        throw new IllegalArgumentException("Meets can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(During node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p2 instanceof StaTimeIntervalWrapper ti2) {
            return ti2.contains(p1);
        }
        throw new IllegalArgumentException("Second parameter of 'during' has to be an interval.");
    }

    @Override
    public FieldWrapper visit(Overlaps node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper timeExpression) {
            return timeExpression.overlaps(p2);
        }
        throw new IllegalArgumentException("Overlaps can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Starts node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper timeExpression) {
            return timeExpression.starts(p2);
        }
        throw new IllegalArgumentException("Starts can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Finishes node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper timeExpression) {
            return timeExpression.finishes(p2);
        }
        throw new IllegalArgumentException("Finishes can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Add node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.add(p2);
        }
        if (p2 instanceof TimeFieldWrapper ti2) {
            return ti2.add(p1);
        }
        Field<Number> n1 = p1.getFieldAsType(Number.class, true);
        Field<Number> n2 = p2.getFieldAsType(Number.class, true);
        return new SimpleFieldWrapper(n1.add(n2));
    }

    @Override
    public FieldWrapper visit(Divide node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.div(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            throw new IllegalArgumentException("Can not devide by a TimeExpression.");
        }
        Field<Number> n1 = p1.getFieldAsType(Number.class, true);
        Field<Number> n2 = p2.getFieldAsType(Number.class, true);
        return new SimpleFieldWrapper(n1.divide(n2).coerce(SQLDataType.DOUBLE));
    }

    @Override
    public FieldWrapper visit(Modulo node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        Field<? extends Number> n1 = p1.getFieldAsType(Number.class, true);
        Field<? extends Number> n2 = p2.getFieldAsType(Number.class, true);
        if (n1.getType().equals(Double.class)) {
            n1 = n1.cast(SQLDataType.NUMERIC);
        }
        if (n2.getType().equals(Double.class)) {
            n2 = n2.cast(SQLDataType.NUMERIC);
        }
        return new SimpleFieldWrapper(n1.mod(n2));
    }

    @Override
    public FieldWrapper visit(Multiply node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.mul(p2);
        }
        if (p2 instanceof TimeFieldWrapper ti2) {
            return ti2.mul(p1);
        }
        Field<Number> n1 = p1.getFieldAsType(Number.class, true);
        Field<Number> n2 = p2.getFieldAsType(Number.class, true);
        return new SimpleFieldWrapper(n1.multiply(n2));
    }

    @Override
    public FieldWrapper visit(Subtract node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.sub(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            throw new IllegalArgumentException("Can not sub a time expression from a " + p1.getClass().getName());
        }
        Field<Number> n1 = p1.getFieldAsType(Number.class, true);
        Field<Number> n2 = p2.getFieldAsType(Number.class, true);
        return new SimpleFieldWrapper(n1.subtract(n2));
    }

    @Override
    public FieldWrapper visit(Equal node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof NullWrapper) {
            return new SimpleFieldWrapper(p2.getDefaultField().isNull());
        }
        if (p2 instanceof NullWrapper) {
            return new SimpleFieldWrapper(p1.getDefaultField().isNull());
        }
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.eq(p2);
        }
        if (p2 instanceof TimeFieldWrapper ti2) {
            return ti2.eq(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper l1) {
            return l1.eq(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper l2) {
            return l2.eq(p1);
        }

        Field[] pair = findPair(p1, p2);
        return new SimpleFieldWrapper(pair[0].eq(pair[1]));
    }

    @Override
    public FieldWrapper visit(GreaterEqual node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.goe(p2);
        }
        if (p2 instanceof TimeFieldWrapper ti2) {
            return ti2.loe(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper l1) {
            return l1.goe(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper l2) {
            return l2.loe(p1);
        }
        Field[] pair = findPair(p1, p2);
        return new SimpleFieldWrapper(pair[0].greaterOrEqual(pair[1]));
    }

    @Override
    public FieldWrapper visit(GreaterThan node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.gt(p2);
        }
        if (p2 instanceof TimeFieldWrapper ti2) {
            return ti2.lt(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper l1) {
            return l1.gt(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper l2) {
            return l2.lt(p1);
        }
        Field[] pair = findPair(p1, p2);
        return new SimpleFieldWrapper(pair[0].greaterThan(pair[1]));
    }

    @Override
    public FieldWrapper visit(LessEqual node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.loe(p2);
        }
        if (p2 instanceof TimeFieldWrapper ti2) {
            return ti2.goe(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper l1) {
            return l1.loe(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper l2) {
            return l2.goe(p1);
        }
        Field[] pair = findPair(p1, p2);
        return new SimpleFieldWrapper(pair[0].lessOrEqual(pair[1]));
    }

    @Override
    public FieldWrapper visit(LessThan node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.lt(p2);
        }
        if (p2 instanceof TimeFieldWrapper ti2) {
            return ti2.gt(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper l1) {
            return l1.lt(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper l2) {
            return l2.gt(p1);
        }
        Field[] pair = findPair(p1, p2);
        return new SimpleFieldWrapper(pair[0].lt(pair[1]));
    }

    @Override
    public FieldWrapper visit(NotEqual node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof NullWrapper) {
            return new SimpleFieldWrapper(p2.getDefaultField().isNotNull());
        }
        if (p2 instanceof NullWrapper) {
            return new SimpleFieldWrapper(p1.getDefaultField().isNotNull());
        }
        if (p1 instanceof TimeFieldWrapper ti1) {
            return ti1.neq(p2);
        }
        if (p2 instanceof TimeFieldWrapper ti2) {
            return ti2.neq(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper l1) {
            return l1.ne(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper l2) {
            return l2.ne(p1);
        }
        Field[] pair = findPair(p1, p2);
        return new SimpleFieldWrapper(pair[0].ne(pair[1]));
    }

    @Override
    public FieldWrapper visit(In node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p2 instanceof ArrayConstandFieldWrapper clP2) {
            return new SimpleFieldWrapper(p1.getDefaultField().in(clP2.getValueList()));
        }
        Field[] pair = findPair(p1, p2);
        if (p2 instanceof JsonFieldWrapper jP2) {
            return jP2.contains(pair[0]);
        } else {
            return new SimpleFieldWrapper(pair[0].in(pair[1]));
        }
    }

    @Override
    public FieldWrapper visit(Date node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(DSL.function("date", java.sql.Date.class, timeExpression.getDateTime()));
        }
        Field<java.sql.Date> fieldAsDate = input.getFieldAsType(java.sql.Date.class, true);
        if (fieldAsDate != null) {
            return new SimpleFieldWrapper(fieldAsDate);
        }
        throw new IllegalArgumentException("Date can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Day node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.DAY));
        }
        throw new IllegalArgumentException("Day can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(FractionalSeconds node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(DSL.field("(date_part('SECONDS', TIMESTAMPTZ ?) - floor(date_part('SECONDS', TIMESTAMPTZ ?)))", Double.class, timeExpression.getDateTime(), timeExpression.getDateTime()));
        }
        throw new IllegalArgumentException("FractionalSeconds can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Hour node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.HOUR));
        }
        throw new IllegalArgumentException("Hour can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(MaxDateTime node) {
        return new StaDateTimeWrapper(PostgresPersistenceManager.DATETIME_MAX, true);
    }

    @Override
    public FieldWrapper visit(MinDateTime node) {
        return new StaDateTimeWrapper(PostgresPersistenceManager.DATETIME_MIN, true);
    }

    @Override
    public FieldWrapper visit(Minute node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.MINUTE));
        }
        throw new IllegalArgumentException("Minute can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Month node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.MONTH));
        }
        throw new IllegalArgumentException("Month can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Now node) {
        return new StaDateTimeWrapper(DSL.field("now()", Moment.class));
    }

    @Override
    public FieldWrapper visit(Second node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.SECOND));
        }
        throw new IllegalArgumentException("Second can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Time node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(timeExpression.getDateTime().cast(SQLDataType.TIME));
        }
        throw new IllegalArgumentException("Time can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(TotalOffsetMinutes node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.TIMEZONE).div(60));
        }
        throw new IllegalArgumentException("TotalOffsetMinutes can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Year node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper timeExpression) {
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.YEAR));
        }
        throw new IllegalArgumentException("Year can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(GeoDistance node) {
        Expression p1 = node.getParameters().get(0);
        Expression p2 = node.getParameters().get(1);
        FieldWrapper e1 = p1.accept(this);
        FieldWrapper e2 = p2.accept(this);
        Field<Geometry> g1 = e1.getFieldAsType(Geometry.class, true);
        Field<Geometry> g2 = e2.getFieldAsType(Geometry.class, true);
        if (g1 == null || g2 == null) {
            throw new IllegalArgumentException("GeoDistance requires two geometries, got " + e1 + " & " + e2);
        }
        return new SimpleFieldWrapper(DSL.function("ST_Distance", SQLDataType.NUMERIC, g1, g2));
    }

    @Override
    public FieldWrapper visit(GeoIntersects node) {
        return stCompare(node, "ST_Intersects");
    }

    @Override
    public FieldWrapper visit(GeoLength node) {
        Expression p1 = node.getParameters().get(0);
        FieldWrapper e1 = p1.accept(this);
        Field<Geometry> g1 = e1.getFieldAsType(Geometry.class, true);
        if (g1 == null) {
            throw new IllegalArgumentException("GeoLength requires a geometry, got " + e1);
        }
        return new SimpleFieldWrapper(DSL.function("ST_Length", SQLDataType.NUMERIC, g1));
    }

    @Override
    public FieldWrapper visit(And node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1.isCondition() && p2.isCondition()) {
            return new SimpleFieldWrapper(p1.getCondition().and(p2.getCondition()));
        }
        throw new IllegalArgumentException("And requires two conditions, got " + p1 + " & " + p2);
    }

    @Override
    public FieldWrapper visit(Not node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        if (p1.isCondition()) {
            return new SimpleFieldWrapper(p1.getCondition().not());
        }
        throw new IllegalArgumentException("Not requires a condition, got " + p1);
    }

    @Override
    public FieldWrapper visit(Or node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1.isCondition() && p2.isCondition()) {
            return new SimpleFieldWrapper(p1.getCondition().or(p2.getCondition()));
        }
        throw new IllegalArgumentException("Or requires two conditions, got " + p1 + " & " + p2);
    }

    @Override
    public FieldWrapper visit(Ceiling node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        Field<Number> n1 = p1.getFieldAsType(Number.class, true);
        return new SimpleFieldWrapper(DSL.ceil(n1));
    }

    @Override
    public FieldWrapper visit(Floor node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        Field<Number> n1 = p1.getFieldAsType(Number.class, true);
        return new SimpleFieldWrapper(DSL.floor(n1));
    }

    @Override
    public FieldWrapper visit(Round node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        Field<Number> n1 = p1.getFieldAsType(Number.class, true);
        return new SimpleFieldWrapper(DSL.round(n1));
    }

    private FieldWrapper stCompare(Function node, String functionName) {
        Expression p1 = node.getParameters().get(0);
        Expression p2 = node.getParameters().get(1);
        FieldWrapper e1 = p1.accept(this);
        FieldWrapper e2 = p2.accept(this);
        Field<Geometry> g1 = e1.getFieldAsType(Geometry.class, true);
        Field<Geometry> g2 = e2.getFieldAsType(Geometry.class, true);
        if (g1 == null || g2 == null) {
            throw new IllegalArgumentException(functionName + " requires two geometries, got " + e1 + " & " + e2);
        }
        return new SimpleFieldWrapper(DSL.condition(DSL.function(functionName, SQLDataType.BOOLEAN, g1, g2)));
    }

    @Override
    public FieldWrapper visit(STContains node) {
        return stCompare(node, "ST_Contains");
    }

    @Override
    public FieldWrapper visit(STCrosses node) {
        return stCompare(node, "ST_Crosses");
    }

    @Override
    public FieldWrapper visit(STDisjoint node) {
        return stCompare(node, "ST_Disjoint");
    }

    @Override
    public FieldWrapper visit(STEquals node) {
        return stCompare(node, "ST_Equals");
    }

    @Override
    public FieldWrapper visit(STIntersects node) {
        return stCompare(node, "ST_Intersects");
    }

    @Override
    public FieldWrapper visit(STOverlaps node) {
        return stCompare(node, "ST_Overlaps");
    }

    @Override
    public FieldWrapper visit(STRelate node) {
        Expression p1 = node.getParameters().get(0);
        Expression p2 = node.getParameters().get(1);
        Expression p3 = node.getParameters().get(2);
        FieldWrapper e1 = p1.accept(this);
        FieldWrapper e2 = p2.accept(this);
        FieldWrapper e3 = p3.accept(this);
        Field<Geometry> g1 = e1.getFieldAsType(Geometry.class, true);
        Field<Geometry> g2 = e2.getFieldAsType(Geometry.class, true);
        Field<String> g3 = e3.getFieldAsType(String.class, true);
        if (g1 == null || g2 == null || g3 == null) {
            throw new IllegalArgumentException("STRelate requires two geometries and a string, got " + e1 + ", " + e2 + " & " + e3);
        }
        return new SimpleFieldWrapper(DSL.condition(DSL.function("ST_Relate", SQLDataType.BOOLEAN, g1, g2, g3)));
    }

    @Override
    public FieldWrapper visit(STTouches node) {
        return stCompare(node, "ST_Touches");
    }

    @Override
    public FieldWrapper visit(STWithin node) {
        return stCompare(node, "ST_Within");
    }

    @Override
    public FieldWrapper visit(Concat node) {
        Expression p1 = node.getParameters().get(0);
        Expression p2 = node.getParameters().get(1);
        FieldWrapper e1 = p1.accept(this);
        FieldWrapper e2 = p2.accept(this);
        Field<String> s1 = e1.getFieldAsType(String.class, true);
        Field<String> s2 = e2.getFieldAsType(String.class, true);
        return new SimpleFieldWrapper(s1.concat(s2));
    }

    @Override
    public FieldWrapper visit(EndsWith node) {
        Expression p1 = node.getParameters().get(0);
        Expression p2 = node.getParameters().get(1);
        FieldWrapper e1 = p1.accept(this);
        FieldWrapper e2 = p2.accept(this);
        Field<String> s1 = e1.getFieldAsType(String.class, true);
        Field<String> s2 = e2.getFieldAsType(String.class, true);
        return new SimpleFieldWrapper(s1.endsWith(s2));
    }

    @Override
    public FieldWrapper visit(IndexOf node) {
        Expression p1 = node.getParameters().get(0);
        Expression p2 = node.getParameters().get(1);
        FieldWrapper e1 = p1.accept(this);
        FieldWrapper e2 = p2.accept(this);
        Field<String> s1 = e1.getFieldAsType(String.class, true);
        Field<String> s2 = e2.getFieldAsType(String.class, true);
        return new SimpleFieldWrapper(DSL.position(s1, s2));
    }

    @Override
    public FieldWrapper visit(Length node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper e1 = param.accept(this);
        Field<String> s1 = e1.getFieldAsType(String.class, true);
        return new SimpleFieldWrapper(DSL.length(s1));
    }

    @Override
    public FieldWrapper visit(StartsWith node) {
        Expression p1 = node.getParameters().get(0);
        Expression p2 = node.getParameters().get(1);
        FieldWrapper e1 = p1.accept(this);
        FieldWrapper e2 = p2.accept(this);
        Field<String> s1 = e1.getFieldAsType(String.class, true);
        Field<String> s2 = e2.getFieldAsType(String.class, true);
        return new SimpleFieldWrapper(s1.startsWith(s2));
    }

    @Override
    public FieldWrapper visit(Substring node) {
        List<Expression> params = node.getParameters();
        Expression p1 = node.getParameters().get(0);
        Expression p2 = node.getParameters().get(1);
        FieldWrapper e1 = p1.accept(this);
        FieldWrapper e2 = p2.accept(this);
        Field<String> s1 = e1.getFieldAsType(String.class, true);
        Field<Number> n2 = e2.getFieldAsType(Number.class, true);
        if (params.size() > 2) {
            Expression p3 = node.getParameters().get(2);
            FieldWrapper e3 = p3.accept(this);
            Field<Number> n3 = e3.getFieldAsType(Number.class, true);
            return new SimpleFieldWrapper(DSL.substring(s1, n2, n3));
        }
        return new SimpleFieldWrapper(DSL.substring(s1, n2));
    }

    @Override
    public FieldWrapper visit(SubstringOf node) {
        Expression p1 = node.getParameters().get(0);
        Expression p2 = node.getParameters().get(1);
        FieldWrapper e1 = p1.accept(this);
        FieldWrapper e2 = p2.accept(this);
        Field<String> s1 = e1.getFieldAsType(String.class, true);
        Field<String> s2 = e2.getFieldAsType(String.class, true);
        return new SimpleFieldWrapper(s2.contains(s1));
    }

    @Override
    public FieldWrapper visit(ToLower node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        Field<String> field = input.getFieldAsType(String.class, true);
        return new SimpleFieldWrapper(DSL.lower(field));
    }

    @Override
    public FieldWrapper visit(ToUpper node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        Field<String> field = input.getFieldAsType(String.class, true);
        return new SimpleFieldWrapper(DSL.upper(field));
    }

    @Override
    public FieldWrapper visit(Trim node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        Field<String> field = input.getFieldAsType(String.class, true);
        return new SimpleFieldWrapper(DSL.trim(field));
    }

    @Override
    public FieldWrapper visit(PrincipalName node) {
        return new SimpleFieldWrapper(DSL.value(node.getValue()));
    }

    @Override
    public FieldWrapper visit(ContextEntityProperty node) {
        return new SimpleFieldWrapper(DSL.value(node.getValue()));
    }

    private static class PathState {

        private TableRef pathTableRef;
        private List<Property> elements;
        private FieldWrapper finalExpression = null;
        private int curIndex;
        private boolean finished = false;
    }
}
