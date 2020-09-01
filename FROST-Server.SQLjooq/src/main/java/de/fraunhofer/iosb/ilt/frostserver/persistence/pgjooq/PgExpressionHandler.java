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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.FieldListWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.FieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
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
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.GeoJsonConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntervalConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.LineStringConstant;
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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.LessEqual;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.LessThan;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.NotEqual;
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
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.UTC;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.geojson.GeoJsonObject;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
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
 * @param <J> The type of the ID fields.
 */
public class PgExpressionHandler<J extends Comparable> implements ExpressionVisitor<FieldWrapper> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PgExpressionHandler.class);
    private static final String ST_GEOM_FROM_EWKT = "ST_GeomFromEWKT(?)";

    private final QueryBuilder<J> queryBuilder;
    /**
     * The table reference for the main table of the request.
     */
    private final TableRef<J> tableRef;

    private int maxCustomLinkDepth = -1;

    public PgExpressionHandler(CoreSettings settings, QueryBuilder<J> queryBuilder, TableRef<J> tableRef) {
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

        } else if (filterField instanceof FieldListWrapper) {
            FieldListWrapper listExpression = (FieldListWrapper) filterField;
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
        if (resultExpression instanceof StaTimeIntervalWrapper) {
            StaTimeIntervalWrapper ti = (StaTimeIntervalWrapper) resultExpression;
            addToQuery(orderBy, ti.getStart(), orderFields);
            addToQuery(orderBy, ti.getEnd(), orderFields);
            return;
        }
        if (resultExpression instanceof StaDurationWrapper) {
            StaDurationWrapper duration = (StaDurationWrapper) resultExpression;
            addToQuery(orderBy, duration.getDuration(), orderFields);
            return;
        }
        if (resultExpression instanceof StaDateTimeWrapper) {
            StaDateTimeWrapper dateTime = (StaDateTimeWrapper) resultExpression;
            addToQuery(orderBy, dateTime.getDateTime(), orderFields);
            return;
        }
        if (resultExpression instanceof FieldListWrapper) {
            for (Field sqlExpression : ((FieldListWrapper) resultExpression).getExpressionsForOrder().values()) {
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
        PathState<J> state = new PathState<>();
        state.pathTableRef = tableRef;
        state.elements = path.getElements();
        for (state.curIndex = 0; state.curIndex < state.elements.size() && !state.finished; state.curIndex++) {
            Property element = state.elements.get(state.curIndex);
            if (element instanceof EntityPropertyCustom) {
                handleCustomProperty(state, path);

            } else if (element instanceof EntityPropertyCustomLink) {
                handleCustomProperty(state, path);

            } else if (element instanceof EntityPropertyMain) {
                handleEntityProperty(state, path, element);

            } else if (element instanceof NavigationPropertyMain) {
                handleNavigationProperty(state, path, element);
            }
        }
        if (state.finalExpression == null) {
            throw new IllegalArgumentException("Path does not end in an EntityProperty: " + path);
        }
        if (state.finalExpression instanceof Field) {
            Field field = (Field) state.finalExpression;
            if (OffsetDateTime.class.isAssignableFrom(field.getType())) {
                Field<OffsetDateTime> dateTimePath = (Field<OffsetDateTime>) state.finalExpression;
                state.finalExpression = new StaDateTimeWrapper(dateTimePath);
            }
        }
        return state.finalExpression;
    }

    private void handleCustomProperty(PathState<J> state, Path path) {
        if (state.finalExpression == null) {
            throw new IllegalArgumentException("CustomProperty must follow an EntityProperty: " + path);
        }
        // generate finalExpression::jsonb#>>'{x,y,z}'
        JsonFieldFactory jsonFactory = new JsonFieldFactory(state.finalExpression);
        for (; state.curIndex < state.elements.size(); state.curIndex++) {
            final Property property = state.elements.get(state.curIndex);
            String name = property.getName();
            if (property instanceof EntityPropertyCustomLink) {
                int maxDepth = state.curIndex + maxCustomLinkDepth;
                if (state.curIndex <= maxDepth) {
                    handleCustomLink(property, jsonFactory, name, state);
                    return;
                } else {
                    jsonFactory.addToPath(name);
                }
            } else {
                jsonFactory.addToPath(name);
            }
        }
        state.finalExpression = jsonFactory.build();
        state.finished = true;
    }

    private void handleCustomLink(final Property property, JsonFieldFactory jsonFactory, String name, PathState<J> state) {
        EntityType targetEntityType = ((EntityPropertyCustomLink) property).getTargetEntityType();
        JsonFieldFactory.JsonFieldWrapper sourceIdFieldWrapper = jsonFactory.addToPath(name + AT_IOT_ID).build();
        Field<Number> sourceIdField = sourceIdFieldWrapper.getFieldAsType(Number.class, true);
        state.pathTableRef = queryBuilder.queryEntityType(targetEntityType, state.pathTableRef, sourceIdField);
        state.finalExpression = null;
    }

    private void handleEntityProperty(PathState<J> state, Path path, Property element) {
        if (state.finalExpression != null) {
            throw new IllegalArgumentException("EntityProperty can not follow an other EntityProperty: " + path);
        }
        EntityPropertyMain entityProperty = (EntityPropertyMain) element;
        Map<String, Field> pathExpressions = state.pathTableRef.getTable()
                .getPropertyFieldRegistry()
                .getAllFieldsForProperty(entityProperty, new LinkedHashMap<>());
        if (pathExpressions.size() == 1) {
            state.finalExpression = WrapperHelper.wrapField(pathExpressions.values().iterator().next());
        } else {
            state.finalExpression = getSubExpression(state, pathExpressions);
        }
    }

    private void handleNavigationProperty(PathState<J> state, Path path, Property element) {
        if (state.finalExpression != null) {
            throw new IllegalArgumentException("NavigationProperty can not follow an EntityProperty: " + path);
        }
        NavigationPropertyMain navigationProperty = (NavigationPropertyMain) element;
        state.pathTableRef = queryBuilder.queryEntityType(navigationProperty.getType(), null, state.pathTableRef);
    }

    private FieldWrapper getSubExpression(PathState<J> state, Map<String, Field> pathExpressions) {
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
            return new SimpleFieldWrapper(pathExpressions.get(subProperty.getName()));
        } else {
            if (pathExpressions.containsKey(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START)
                    && pathExpressions.containsKey(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END)) {
                return new StaTimeIntervalWrapper(pathExpressions);
            }
            return new FieldListWrapper(pathExpressions);
        }
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
        LocalDate date = node.getValue();
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        instance.set(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        return new SimpleFieldWrapper(DSL.inline(new java.sql.Date(instance.getTimeInMillis())));
    }

    @Override
    public FieldWrapper visit(DateTimeConstant node) {
        DateTime value = node.getValue();
        DateTimeZone zone = value.getZone();
        return new StaDateTimeWrapper(OffsetDateTime.ofInstant(Instant.ofEpochMilli(value.getMillis()), UTC), zone == DateTimeZone.UTC);
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
        Interval value = node.getValue();
        return new StaTimeIntervalWrapper(
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(value.getStartMillis()), UTC),
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(value.getEndMillis()), UTC)
        );
    }

    @Override
    public FieldWrapper visit(IntegerConstant node) {
        return new SimpleFieldWrapper(DSL.val(node.getValue()));
    }

    @Override
    public FieldWrapper visit(LineStringConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new SimpleFieldWrapper(DSL.field(ST_GEOM_FROM_EWKT, Geometry.class, geom.asText()));
    }

    @Override
    public FieldWrapper visit(PointConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new SimpleFieldWrapper(DSL.field(ST_GEOM_FROM_EWKT, Geometry.class, geom.asText()));
    }

    @Override
    public FieldWrapper visit(PolygonConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new SimpleFieldWrapper(DSL.field(ST_GEOM_FROM_EWKT, Geometry.class, geom.asText()));
    }

    private Geometry fromGeoJsonConstant(GeoJsonConstant<? extends GeoJsonObject> node) {
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
        LocalTime time = node.getValue();
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        instance.set(1970, 1, 1, time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
        return new SimpleFieldWrapper(DSL.inline(new java.sql.Time(instance.getTimeInMillis())));
    }

    @Override
    public FieldWrapper visit(Before node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) p1;
            return timeExpression.before(p2);
        }
        throw new IllegalArgumentException("Before can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(After node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) p1;
            return timeExpression.after(p2);
        }
        throw new IllegalArgumentException("After can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Meets node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) p1;
            return timeExpression.meets(p2);
        }
        throw new IllegalArgumentException("Meets can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(During node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p2 instanceof StaTimeIntervalWrapper) {
            StaTimeIntervalWrapper ti2 = (StaTimeIntervalWrapper) p2;
            return ti2.contains(p1);
        } else {
            throw new IllegalArgumentException("Second parameter of 'during' has to be an interval.");
        }
    }

    @Override
    public FieldWrapper visit(Overlaps node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) p1;
            return timeExpression.overlaps(p2);
        }
        throw new IllegalArgumentException("Overlaps can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Starts node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) p1;
            return timeExpression.starts(p2);
        }
        throw new IllegalArgumentException("Starts can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Finishes node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) p1;
            return timeExpression.finishes(p2);
        }
        throw new IllegalArgumentException("Finishes can only be used on times, not on " + p1.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Add node) {
        List<Expression> params = node.getParameters();
        FieldWrapper p1 = params.get(0).accept(this);
        FieldWrapper p2 = params.get(1).accept(this);
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
            return ti1.add(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti2 = (TimeFieldWrapper) p2;
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
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
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
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
            return ti1.mul(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti2 = (TimeFieldWrapper) p2;
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
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
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
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
            return ti1.eq(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti2 = (TimeFieldWrapper) p2;
            return ti2.eq(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l1 = (JsonFieldFactory.JsonFieldWrapper) p1;
            return l1.eq(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l2 = (JsonFieldFactory.JsonFieldWrapper) p2;
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
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
            return ti1.goe(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti2 = (TimeFieldWrapper) p2;
            return ti2.loe(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l1 = (JsonFieldFactory.JsonFieldWrapper) p1;
            return l1.goe(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l2 = (JsonFieldFactory.JsonFieldWrapper) p2;
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
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
            return ti1.gt(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti2 = (TimeFieldWrapper) p2;
            return ti2.lt(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l1 = (JsonFieldFactory.JsonFieldWrapper) p1;
            return l1.gt(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l2 = (JsonFieldFactory.JsonFieldWrapper) p2;
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
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
            return ti1.loe(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti2 = (TimeFieldWrapper) p2;
            return ti2.goe(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l1 = (JsonFieldFactory.JsonFieldWrapper) p1;
            return l1.loe(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l2 = (JsonFieldFactory.JsonFieldWrapper) p2;
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
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
            return ti1.lt(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti2 = (TimeFieldWrapper) p2;
            return ti2.gt(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l1 = (JsonFieldFactory.JsonFieldWrapper) p1;
            return l1.lt(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l2 = (JsonFieldFactory.JsonFieldWrapper) p2;
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
        if (p1 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti1 = (TimeFieldWrapper) p1;
            return ti1.neq(p2);
        }
        if (p2 instanceof TimeFieldWrapper) {
            TimeFieldWrapper ti2 = (TimeFieldWrapper) p2;
            return ti2.neq(p1);
        }
        if (p1 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l1 = (JsonFieldFactory.JsonFieldWrapper) p1;
            return l1.ne(p2);
        }
        if (p2 instanceof JsonFieldFactory.JsonFieldWrapper) {
            JsonFieldFactory.JsonFieldWrapper l2 = (JsonFieldFactory.JsonFieldWrapper) p2;
            return l2.ne(p1);
        }
        Field[] pair = findPair(p1, p2);
        return new SimpleFieldWrapper(pair[0].ne(pair[1]));
    }

    @Override
    public FieldWrapper visit(Date node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
            return new SimpleFieldWrapper(DSL.function("date", Date.class, timeExpression.getDateTime()));
        }
        throw new IllegalArgumentException("Date can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Day node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.DAY));
        }
        throw new IllegalArgumentException("Day can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(FractionalSeconds node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
            return new SimpleFieldWrapper(DSL.field("(date_part('SECONDS', TIMESTAMPTZ ?) - floor(date_part('SECONDS', TIMESTAMPTZ ?)))", Double.class, timeExpression.getDateTime(), timeExpression.getDateTime()));
        }
        throw new IllegalArgumentException("FractionalSeconds can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Hour node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
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
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.MINUTE));
        }
        throw new IllegalArgumentException("Minute can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Month node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.MONTH));
        }
        throw new IllegalArgumentException("Month can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Now node) {
        return new StaDateTimeWrapper(DSL.currentOffsetDateTime());
    }

    @Override
    public FieldWrapper visit(Second node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.SECOND));
        }
        throw new IllegalArgumentException("Second can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Time node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
            if (!timeExpression.isUtc()) {
                throw new IllegalArgumentException("Constants passed to the time() function have to be in UTC.");
            }
            return new SimpleFieldWrapper(timeExpression.getDateTime().cast(SQLDataType.TIME));
        }
        throw new IllegalArgumentException("Time can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(TotalOffsetMinutes node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
            return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.TIMEZONE).div(60));
        }
        throw new IllegalArgumentException("TotalOffsetMinutes can only be used on times, not on " + input.getClass().getName());
    }

    @Override
    public FieldWrapper visit(Year node) {
        Expression param = node.getParameters().get(0);
        FieldWrapper input = param.accept(this);
        if (input instanceof TimeFieldWrapper) {
            TimeFieldWrapper timeExpression = (TimeFieldWrapper) input;
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

    private static class PathState<J extends Comparable> {

        private TableRef<J> pathTableRef;
        private List<Property> elements;
        private FieldWrapper finalExpression = null;
        private int curIndex;
        private boolean finished = false;
    }
}
