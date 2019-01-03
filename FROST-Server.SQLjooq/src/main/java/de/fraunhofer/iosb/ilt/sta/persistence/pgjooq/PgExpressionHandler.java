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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.sta.path.CustomProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.ConstantNumberExpression;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.FieldWrapper;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.JsonExpressionFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.ListExpression;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.SimpleFieldWrapper;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.StaDateTimeExpression;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.StaDurationExpression;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.StaTimeIntervalExpression;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.TimeExpression;
import de.fraunhofer.iosb.ilt.sta.query.OrderBy;
import de.fraunhofer.iosb.ilt.sta.query.expression.Expression;
import de.fraunhofer.iosb.ilt.sta.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.sta.query.expression.Path;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DateConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.GeoJsonConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.IntervalConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.LineStringConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.PointConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.PolygonConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.TimeConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Add;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Divide;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Modulo;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Multiply;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic.Subtract;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.Equal;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.GreaterEqual;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.GreaterThan;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.LessEqual;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.LessThan;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.NotEqual;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.Date;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.Day;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.FractionalSeconds;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.Hour;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.MaxDateTime;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.MinDateTime;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.Minute;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.Month;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.Now;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.Second;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.Time;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.TotalOffsetMinutes;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.date.Year;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.logical.Not;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.logical.Or;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.math.Ceiling;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.math.Floor;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.math.Round;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.GeoDistance;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.GeoIntersects;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.GeoLength;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.STContains;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.STCrosses;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.STDisjoint;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.STEquals;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.STIntersects;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.STOverlaps;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.STRelate;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.STTouches;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.spatialrelation.STWithin;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.Concat;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.EndsWith;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.IndexOf;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.Length;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.StartsWith;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.Substring;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.SubstringOf;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.ToLower;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.ToUpper;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.string.Trim;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.temporal.After;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.temporal.Before;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.temporal.During;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.temporal.Finishes;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.temporal.Meets;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.temporal.Overlaps;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.temporal.Starts;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Collection;
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
import org.jooq.Field;
import org.jooq.OrderField;
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
    private final QueryBuilder queryBuilder;
    /**
     * The table reference for the main table of the request.
     */
    private final TableRef tableRef;

    public PgExpressionHandler(QueryBuilder queryBuilder, TableRef tableRef) {
        this.queryBuilder = queryBuilder;
        this.tableRef = tableRef;

    }

    public Condition addFilterToWhere(Expression filter, Condition sqlWhere) {
        Object filterField = filter.accept(this);
        if (filterField instanceof Condition) {
            Condition predicate = (Condition) filterField;
            return sqlWhere.and(predicate);

        } else if (filterField instanceof ListExpression) {
            ListExpression listExpression = (ListExpression) filterField;
            for (Field expression : listExpression.getExpressions().values()) {
                if (expression instanceof Condition) {
                    Condition predicate = (Condition) expression;
                    return sqlWhere.and(predicate);
                }
            }
        }
        LOGGER.error("Filter is not a predicate but a {}.", filterField.getClass().getName());
        throw new IllegalArgumentException("Filter is not a predicate but a " + filterField.getClass().getName());
    }

    public void addOrderbyToQuery(OrderBy orderBy, List<OrderField> orderFields) {
        Object resultExpression = orderBy.getExpression().accept(this);
        if (resultExpression instanceof StaTimeIntervalExpression) {
            StaTimeIntervalExpression ti = (StaTimeIntervalExpression) resultExpression;
            addToQuery(orderBy, ti.getStart(), orderFields);
            addToQuery(orderBy, ti.getEnd(), orderFields);
            return;
        }
        if (resultExpression instanceof StaDurationExpression) {
            StaDurationExpression duration = (StaDurationExpression) resultExpression;
            addToQuery(orderBy, duration.getDuration(), orderFields);
            return;
        }
        if (resultExpression instanceof StaDateTimeExpression) {
            StaDateTimeExpression dateTime = (StaDateTimeExpression) resultExpression;
            addToQuery(orderBy, dateTime.getDateTime(), orderFields);
            return;
        }
        if (resultExpression instanceof ListExpression) {
            for (Field sqlExpression : ((ListExpression) resultExpression).getExpressionsForOrder().values()) {
                addToQuery(orderBy, sqlExpression, orderFields);
            }
            return;
        }
        if (resultExpression instanceof Field) {
            Field field = (Field) resultExpression;
            addToQuery(orderBy, field, orderFields);
        }
    }

    public void addToQuery(OrderBy orderBy, Field field, List<OrderField> orderFields) {
        if (orderBy.getType() == OrderBy.OrderType.ASCENDING) {
            orderFields.add(field.asc());
        } else {
            orderFields.add(field.desc());
        }
    }

    public static <T> Field<T> getSingleOfType(Class<T> expectedClazz, Object input) {
        if (input instanceof ListExpression) {
            ListExpression listExpression = (ListExpression) input;
            Map<String, Field> expressions = listExpression.getExpressions();
            Collection<Field> values = expressions.values();
            // Two passes, first do an exact check (no casting allowed)
            for (Field subResult : values) {
                try {
                    return checkType(expectedClazz, subResult, false);
                } catch (IllegalArgumentException e) {
                    LOGGER.trace("Parameter not of type {}.", expectedClazz.getName());
                    LOGGER.trace("", e);
                }
            }
            // No exact check. Now check again, but allow casting.
            for (Field subResult : values) {
                try {
                    return checkType(expectedClazz, subResult, true);
                } catch (IllegalArgumentException e) {
                    LOGGER.trace("Parameter not of type {}.", expectedClazz.getName());
                    LOGGER.trace("", e);
                }
            }
            throw new IllegalArgumentException("Non of the entries could be converted to type " + expectedClazz.getName());
        } else {
            return checkType(expectedClazz, input, true);
        }
    }

    private static class PathState {

        TableRef pathTableRef;
        List<Property> elements;
        FieldWrapper finalExpression = null;
        int curIndex;
        boolean finished = false;
    }

    @Override
    public FieldWrapper visit(Path path) {
        PathState state = new PathState();
        state.pathTableRef = tableRef.copy();
        state.elements = path.getElements();
        for (state.curIndex = 0; state.curIndex < state.elements.size() && !state.finished; state.curIndex++) {
            Property element = state.elements.get(state.curIndex);
            if (element instanceof CustomProperty) {
                handleCustomProperty(state, path);

            } else if (element instanceof EntityProperty) {
                handleEntityProperty(state, path, element);

            } else if (element instanceof NavigationProperty) {
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
                state.finalExpression = new StaDateTimeExpression(dateTimePath);
            }
        }
        return state.finalExpression;
    }

    private void handleCustomProperty(PathState state, Path path) {
        if (state.finalExpression == null) {
            throw new IllegalArgumentException("CustomProperty must follow an EntityProperty: " + path);
        }
        // generate finalExpression::jsonb#>>'{x,y,z}'
        JsonExpressionFactory jsonFactory = new JsonExpressionFactory(state.finalExpression);
        for (; state.curIndex < state.elements.size(); state.curIndex++) {
            jsonFactory.addToPath(state.elements.get(state.curIndex).getName());
        }
        state.finalExpression = jsonFactory.build();
        state.finished = true;
    }

    private void handleEntityProperty(PathState state, Path path, Property element) {
        if (state.finalExpression != null) {
            throw new IllegalArgumentException("EntityProperty can not follow an other EntityProperty: " + path);
        }
        EntityProperty entityProperty = (EntityProperty) element;
        Map<String, Field> pathExpressions = queryBuilder
                .getPropertyResolver()
                .expressionsForProperty(entityProperty, state.pathTableRef.getTable(), new LinkedHashMap<>());
        if (pathExpressions.size() == 1) {
            state.finalExpression = new SimpleFieldWrapper(pathExpressions.values().iterator().next());
        } else {
            state.finalExpression = getSubExpression(state, pathExpressions);
        }
    }

    private void handleNavigationProperty(PathState state, Path path, Property element) {
        if (state.finalExpression != null) {
            throw new IllegalArgumentException("NavigationProperty can not follow an EntityProperty: " + path);
        }
        NavigationProperty navigationProperty = (NavigationProperty) element;
        queryBuilder.queryEntityType(navigationProperty.getType(), null, state.pathTableRef);
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
            return new SimpleFieldWrapper(pathExpressions.get(subProperty.getName()));
        } else {
            if (pathExpressions.containsKey(StaTimeIntervalExpression.KEY_TIME_INTERVAL_START)
                    && pathExpressions.containsKey(StaTimeIntervalExpression.KEY_TIME_INTERVAL_END)) {
                return new StaTimeIntervalExpression(pathExpressions);
            }
            return new ListExpression(pathExpressions);
        }
    }

    public Expression[] findPair(Expression<?> p1, Expression<?> p2) {
        Expression<?>[] result = new Expression<?>[2];
        try {
            result[0] = getSingleOfType(NumberExpression.class, p1);
            result[1] = getSingleOfType(NumberExpression.class, p2);
            return result;
        } catch (IllegalArgumentException e) {
            // Not of the requested type.
        }
        try {
            result[0] = getSingleOfType(BooleanExpression.class, p1);
            result[1] = getSingleOfType(BooleanExpression.class, p2);
            return result;
        } catch (IllegalArgumentException e) {
            // Not of the requested type.
        }
        // If both are strings, use strings.
        boolean firstIsString = false;
        try {
            result[0] = getSingleOfType(StringExpression.class, p1);
            firstIsString = true;
            result[1] = getSingleOfType(StringExpression.class, p2);
            return result;
        } catch (IllegalArgumentException e) {
            // Not of the requested type.
        }
        // If one of the two is a string, cast the other
        if (firstIsString) {
            result[1] = StringCastExpressionFactory.build(p2);
            return result;
        } else {
            try {
                result[1] = getSingleOfType(StringExpression.class, p2);
                result[0] = StringCastExpressionFactory.build(p1);
                return result;
            } catch (IllegalArgumentException e) {
                // Not of the requested type.
            }
        }

        result[0] = getSingleOfType(ComparableExpression.class, p1);
        result[1] = getSingleOfType(ComparableExpression.class, p2);
        return result;
    }

    @Override
    public FieldWrapper visit(BooleanConstant node) {
        return node.getValue() ? Expressions.TRUE : Expressions.FALSE;
    }

    @Override
    public FieldWrapper visit(DateConstant node) {
        LocalDate date = node.getValue();
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        instance.set(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        return new ConstantDateExpression(new java.sql.Date(instance.getTimeInMillis()));
    }

    @Override
    public FieldWrapper visit(DateTimeConstant node) {
        DateTime value = node.getValue();
        DateTimeZone zone = value.getZone();
        return new StaDateTimeExpression(new Timestamp(value.getMillis()), zone == DateTimeZone.UTC);
    }

    @Override
    public FieldWrapper visit(DoubleConstant node) {
        return new ConstantNumberExpression(node.getValue());
    }

    @Override
    public FieldWrapper visit(DurationConstant node) {
        return new StaDurationExpression(node);
    }

    @Override
    public FieldWrapper visit(IntervalConstant node) {
        Interval value = node.getValue();
        return new StaTimeIntervalExpression(
                new Timestamp(value.getStartMillis()),
                new Timestamp(value.getEndMillis())
        );
    }

    @Override
    public FieldWrapper visit(IntegerConstant node) {
        return new ConstantNumberExpression(node.getValue());
    }

    @Override
    public FieldWrapper visit(LineStringConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new ConstantGeometryExpression(geom);
    }

    @Override
    public FieldWrapper visit(PointConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new ConstantGeometryExpression(geom);
    }

    @Override
    public FieldWrapper visit(PolygonConstant node) {
        Geometry geom = fromGeoJsonConstant(node);
        return new ConstantGeometryExpression(geom);
    }

    private Geometry fromGeoJsonConstant(GeoJsonConstant<? extends GeoJsonObject> node) {
        if (node.getValue().getCrs() == null) {
            return Wkt.fromWkt("SRID=4326;" + node.getSource());
        }
        return Wkt.fromWkt(node.getSource());
    }

    @Override
    public FieldWrapper visit(StringConstant node) {
        return new ConstantStringExpression(node.getValue());
    }

    @Override
    public FieldWrapper visit(TimeConstant node) {
        LocalTime time = node.getValue();
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        instance.set(1970, 1, 1, time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
        return new ConstantTimeExpression(new java.sql.Time(instance.getTimeInMillis()));
    }

    @Override
    public FieldWrapper visit(Before node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        TimeExpression d1 = getSingleOfType(TimeExpression.class, p1);
        TimeExpression d2 = getSingleOfType(TimeExpression.class, p2);
        return d1.before(d2);
    }

    @Override
    public FieldWrapper visit(After node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        TimeExpression d1 = getSingleOfType(TimeExpression.class, p1);
        TimeExpression d2 = getSingleOfType(TimeExpression.class, p2);
        return d1.after(d2);
    }

    @Override
    public FieldWrapper visit(Meets node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        TimeExpression d1 = getSingleOfType(TimeExpression.class, p1);
        TimeExpression d2 = getSingleOfType(TimeExpression.class, p2);
        return d1.meets(d2);
    }

    @Override
    public FieldWrapper visit(During node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p2 instanceof StaTimeIntervalExpression) {
            StaTimeIntervalExpression ti2 = (StaTimeIntervalExpression) p2;
            return ti2.contains(p1);
        } else {
            throw new IllegalArgumentException("Second parameter of 'during' has to be an interval.");
        }
    }

    @Override
    public FieldWrapper visit(Overlaps node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        TimeExpression d1 = getSingleOfType(TimeExpression.class, p1);
        TimeExpression d2 = getSingleOfType(TimeExpression.class, p2);
        return d1.overlaps(d2);
    }

    @Override
    public FieldWrapper visit(Starts node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        TimeExpression d1 = getSingleOfType(TimeExpression.class, p1);
        TimeExpression d2 = getSingleOfType(TimeExpression.class, p2);
        return d1.starts(d2);
    }

    @Override
    public FieldWrapper visit(Finishes node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        TimeExpression d1 = getSingleOfType(TimeExpression.class, p1);
        TimeExpression d2 = getSingleOfType(TimeExpression.class, p2);
        return d1.finishes(d2);
    }

    @Override
    public FieldWrapper visit(Add node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.add(p2);
        }
        if (p2 instanceof TimeExpression) {
            TimeExpression ti2 = (TimeExpression) p2;
            return ti2.add(p1);
        }
        NumberExpression n1 = getSingleOfType(NumberExpression.class, p1);
        NumberExpression n2 = getSingleOfType(NumberExpression.class, p2);
        return n1.add(n2);
    }

    @Override
    public FieldWrapper visit(Divide node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.div(p2);
        }
        if (p2 instanceof TimeExpression) {
            throw new UnsupportedOperationException("Can not devide by a TimeExpression.");
        }
        NumberExpression n1 = getSingleOfType(NumberExpression.class, p1);
        NumberExpression n2 = getSingleOfType(NumberExpression.class, p2);
        return n1.divide(n2);
    }

    @Override
    public FieldWrapper visit(Modulo node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        NumberExpression n1 = getSingleOfType(NumberExpression.class, p1);
        NumberExpression n2 = getSingleOfType(NumberExpression.class, p2);
        return n1.castToNum(BigDecimal.class).mod(n2.castToNum(BigDecimal.class));
    }

    @Override
    public FieldWrapper visit(Multiply node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.mul(p2);
        }
        if (p2 instanceof TimeExpression) {
            TimeExpression ti2 = (TimeExpression) p2;
            return ti2.mul(p1);
        }
        NumberExpression n1 = getSingleOfType(NumberExpression.class, p1);
        NumberExpression n2 = getSingleOfType(NumberExpression.class, p2);
        return n1.multiply(n2);
    }

    @Override
    public FieldWrapper visit(Subtract node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.sub(p2);
        }
        if (p2 instanceof TimeExpression) {
            throw new UnsupportedOperationException("Can not sub a time expression from a " + p1.getClass().getName());
        }
        NumberExpression n1 = getSingleOfType(NumberExpression.class, p1);
        NumberExpression n2 = getSingleOfType(NumberExpression.class, p2);
        return n1.subtract(n2);
    }

    @Override
    public FieldWrapper visit(Equal node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.eq(p2);
        }
        if (p2 instanceof TimeExpression) {
            TimeExpression ti2 = (TimeExpression) p2;
            return ti2.eq(p1);
        }
        if (p1 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l1 = (JsonExpressionFactory.ListExpressionJson) p1;
            return l1.eq(p2);
        }
        if (p2 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l2 = (JsonExpressionFactory.ListExpressionJson) p2;
            return l2.eq(p1);
        }
        Expression<?>[] pair = findPair(p1, p2);
        if (pair[0] instanceof NumberExpression) {
            return ((NumberExpression) pair[0]).eq(pair[1]);
        }
        return ((ComparableExpression) pair[0]).eq(pair[1]);
    }

    @Override
    public FieldWrapper visit(GreaterEqual node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.goe(p2);
        }
        if (p2 instanceof TimeExpression) {
            TimeExpression ti2 = (TimeExpression) p2;
            return ti2.loe(p1);
        }
        if (p1 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l1 = (JsonExpressionFactory.ListExpressionJson) p1;
            return l1.goe(p2);
        }
        if (p2 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l2 = (JsonExpressionFactory.ListExpressionJson) p2;
            return l2.loe(p1);
        }
        Expression<?>[] pair = findPair(p1, p2);
        if (pair[0] instanceof NumberExpression) {
            return ((NumberExpression) pair[0]).goe(pair[1]);
        }
        return ((ComparableExpression) pair[0]).goe(pair[1]);
    }

    @Override
    public FieldWrapper visit(GreaterThan node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.gt(p2);
        }
        if (p2 instanceof TimeExpression) {
            TimeExpression ti2 = (TimeExpression) p2;
            return ti2.lt(p1);
        }
        if (p1 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l1 = (JsonExpressionFactory.ListExpressionJson) p1;
            return l1.gt(p2);
        }
        if (p2 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l2 = (JsonExpressionFactory.ListExpressionJson) p2;
            return l2.lt(p1);
        }
        Expression<?>[] pair = findPair(p1, p2);
        if (pair[0] instanceof NumberExpression) {
            return ((NumberExpression) pair[0]).gt(pair[1]);
        }
        return ((ComparableExpression) pair[0]).gt(pair[1]);
    }

    @Override
    public FieldWrapper visit(LessEqual node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.loe(p2);
        }
        if (p2 instanceof TimeExpression) {
            TimeExpression ti2 = (TimeExpression) p2;
            return ti2.goe(p1);
        }
        if (p1 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l1 = (JsonExpressionFactory.ListExpressionJson) p1;
            return l1.loe(p2);
        }
        if (p2 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l2 = (JsonExpressionFactory.ListExpressionJson) p2;
            return l2.goe(p1);
        }
        Expression<?>[] pair = findPair(p1, p2);
        if (pair[0] instanceof NumberExpression) {
            return ((NumberExpression) pair[0]).loe(pair[1]);
        }
        return ((ComparableExpression) pair[0]).loe(pair[1]);

    }

    @Override
    public FieldWrapper visit(LessThan node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.lt(p2);
        }
        if (p2 instanceof TimeExpression) {
            TimeExpression ti2 = (TimeExpression) p2;
            return ti2.gt(p1);
        }
        if (p1 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l1 = (JsonExpressionFactory.ListExpressionJson) p1;
            return l1.lt(p2);
        }
        if (p2 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l2 = (JsonExpressionFactory.ListExpressionJson) p2;
            return l2.gt(p1);
        }
        Expression<?>[] pair = findPair(p1, p2);
        if (pair[0] instanceof NumberExpression) {
            return ((NumberExpression) pair[0]).lt(pair[1]);
        }
        return ((ComparableExpression) pair[0]).lt(pair[1]);
    }

    @Override
    public FieldWrapper visit(NotEqual node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        if (p1 instanceof TimeExpression) {
            TimeExpression ti1 = (TimeExpression) p1;
            return ti1.neq(p2);
        }
        if (p2 instanceof TimeExpression) {
            TimeExpression ti2 = (TimeExpression) p2;
            return ti2.neq(p1);
        }
        if (p1 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l1 = (JsonExpressionFactory.ListExpressionJson) p1;
            return l1.ne(p2);
        }
        if (p2 instanceof JsonExpressionFactory.ListExpressionJson) {
            JsonExpressionFactory.ListExpressionJson l2 = (JsonExpressionFactory.ListExpressionJson) p2;
            return l2.ne(p1);
        }
        Expression<?>[] pair = findPair(p1, p2);
        if (pair[0] instanceof NumberExpression) {
            return ((NumberExpression) pair[0]).ne(pair[1]);
        }
        return ((ComparableExpression) pair[0]).ne(pair[1]);
    }

    @Override
    public FieldWrapper visit(Date node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        return SQLExpressions.date(inExp.getDateTime());
    }

    @Override
    public FieldWrapper visit(Day node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        return inExp.getDateTime().dayOfMonth();
    }

    @Override
    public FieldWrapper visit(FractionalSeconds node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        return inExp.getDateTime().milliSecond();
    }

    @Override
    public FieldWrapper visit(Hour node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        return inExp.getDateTime().hour();
    }

    @Override
    public FieldWrapper visit(MaxDateTime node) {
        return new StaDateTimeExpression(new Timestamp(PostgresPersistenceManager.DATETIME_MAX.getMillis()), true);
    }

    @Override
    public FieldWrapper visit(MinDateTime node) {
        return new StaDateTimeExpression(new Timestamp(PostgresPersistenceManager.DATETIME_MIN.getMillis()), true);
    }

    @Override
    public FieldWrapper visit(Minute node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        return inExp.getDateTime().minute();
    }

    @Override
    public FieldWrapper visit(Month node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        return inExp.getDateTime().month();
    }

    @Override
    public FieldWrapper visit(Now node) {
        return new StaDateTimeExpression(DateTimeExpression.currentTimestamp());
    }

    @Override
    public FieldWrapper visit(Second node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        return inExp.getDateTime().second();
    }

    @Override
    public FieldWrapper visit(Time node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        if (!inExp.isUtc()) {
            throw new IllegalArgumentException("Constants passed to the time() function have to be in UTC.");
        }
        return Expressions.timeTemplate(java.sql.Time.class, "pg_catalog.time({0})", inExp.getDateTime());
    }

    @Override
    public FieldWrapper visit(TotalOffsetMinutes node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        return Expressions.numberTemplate(Integer.class, "timezone({0})", inExp.getDateTime()).divide(60);
    }

    @Override
    public FieldWrapper visit(Year node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        TimeExpression inExp = getSingleOfType(TimeExpression.class, input);
        return inExp.getDateTime().year();
    }

    @Override
    public FieldWrapper visit(GeoDistance node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.distance(g2);
    }

    @Override
    public FieldWrapper visit(GeoIntersects node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.intersects(g2);
    }

    @Override
    public FieldWrapper visit(GeoLength node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        Expression<?> e1 = p1.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        return Expressions.numberTemplate(Double.class, "ST_Length({0})", g1);
    }

    @Override
    public FieldWrapper visit(And node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        BooleanExpression b1 = getSingleOfType(BooleanExpression.class, p1);
        BooleanExpression b2 = getSingleOfType(BooleanExpression.class, p2);
        return b1.and(b2);
    }

    @Override
    public FieldWrapper visit(Not node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        BooleanExpression b1 = getSingleOfType(BooleanExpression.class, p1);
        return b1.not();
    }

    @Override
    public FieldWrapper visit(Or node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        Expression<?> p2 = params.get(1).accept(this);
        BooleanExpression b1 = getSingleOfType(BooleanExpression.class, p1);
        BooleanExpression b2 = getSingleOfType(BooleanExpression.class, p2);
        return b1.or(b2);
    }

    @Override
    public FieldWrapper visit(Ceiling node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        NumberExpression n1 = getSingleOfType(NumberExpression.class, p1);
        return n1.ceil();
    }

    @Override
    public FieldWrapper visit(Floor node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        NumberExpression n1 = getSingleOfType(NumberExpression.class, p1);
        return n1.floor();
    }

    @Override
    public FieldWrapper visit(Round node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        Expression<?> p1 = params.get(0).accept(this);
        NumberExpression n1 = getSingleOfType(NumberExpression.class, p1);
        return n1.round();
    }

    @Override
    public FieldWrapper visit(STContains node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.contains(g2);
    }

    @Override
    public FieldWrapper visit(STCrosses node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.crosses(g2);
    }

    @Override
    public FieldWrapper visit(STDisjoint node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.disjoint(g2);
    }

    @Override
    public FieldWrapper visit(STEquals node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.eq(g2);
    }

    @Override
    public FieldWrapper visit(STIntersects node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.intersects(g2);
    }

    @Override
    public FieldWrapper visit(STOverlaps node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.overlaps(g2);
    }

    @Override
    public FieldWrapper visit(STRelate node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p3 = node.getParameters().get(2);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        if (p3 instanceof StringConstant) {
            StringConstant e3 = (StringConstant) p3;
            String s3 = e3.getValue();
            return g1.relate(g2, s3);
        }
        throw new IllegalArgumentException("ST_RELATE can only be used with a string constant as third parameter.");
    }

    @Override
    public FieldWrapper visit(STTouches node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.touches(g2);
    }

    @Override
    public FieldWrapper visit(STWithin node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        GeometryExpression g1 = getSingleOfType(GeometryExpression.class, e1);
        GeometryExpression g2 = getSingleOfType(GeometryExpression.class, e2);
        return g1.within(g2);
    }

    @Override
    public FieldWrapper visit(Concat node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        StringExpression s1 = getSingleOfType(StringExpression.class, e1);
        StringExpression s2 = getSingleOfType(StringExpression.class, e2);
        return s1.concat(s2);
    }

    @Override
    public FieldWrapper visit(EndsWith node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        StringExpression s1 = getSingleOfType(StringExpression.class, e1);
        StringExpression s2 = getSingleOfType(StringExpression.class, e2);
        return s1.endsWith(s2);
    }

    @Override
    public FieldWrapper visit(IndexOf node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        StringExpression s1 = getSingleOfType(StringExpression.class, e1);
        StringExpression s2 = getSingleOfType(StringExpression.class, e2);
        return s1.indexOf(s2);
    }

    @Override
    public FieldWrapper visit(Length node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        StringExpression inExp = getSingleOfType(StringExpression.class, input);
        return inExp.length();
    }

    @Override
    public FieldWrapper visit(StartsWith node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        StringExpression s1 = getSingleOfType(StringExpression.class, e1);
        StringExpression s2 = getSingleOfType(StringExpression.class, e2);
        return s1.startsWith(s2);
    }

    @Override
    public FieldWrapper visit(Substring node) {
        List<de.fraunhofer.iosb.ilt.sta.query.expression.Expression> params = node.getParameters();
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        StringExpression s1 = getSingleOfType(StringExpression.class, e1);
        NumberExpression n2 = getSingleOfType(NumberExpression.class, e2);
        if (params.size() > 2) {
            de.fraunhofer.iosb.ilt.sta.query.expression.Expression p3 = node.getParameters().get(2);
            Expression<?> e3 = p3.accept(this);
            NumberExpression n3 = getSingleOfType(NumberExpression.class, e3);
            return s1.substring(n2, n3);
        }
        return s1.substring(n2);
    }

    @Override
    public FieldWrapper visit(SubstringOf node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p1 = node.getParameters().get(0);
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression p2 = node.getParameters().get(1);
        Expression<?> e1 = p1.accept(this);
        Expression<?> e2 = p2.accept(this);
        StringExpression s1 = getSingleOfType(StringExpression.class, e1);
        StringExpression s2 = getSingleOfType(StringExpression.class, e2);
        return s2.contains(s1);
    }

    @Override
    public FieldWrapper visit(ToLower node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        StringExpression inExp = getSingleOfType(StringExpression.class, input);
        return inExp.toLowerCase();
    }

    @Override
    public FieldWrapper visit(ToUpper node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        StringExpression inExp = getSingleOfType(StringExpression.class, input);
        return inExp.toUpperCase();
    }

    @Override
    public FieldWrapper visit(Trim node) {
        de.fraunhofer.iosb.ilt.sta.query.expression.Expression param = node.getParameters().get(0);
        Expression<?> input = param.accept(this);
        StringExpression inExp = getSingleOfType(StringExpression.class, input);
        return inExp.trim();
    }
}
