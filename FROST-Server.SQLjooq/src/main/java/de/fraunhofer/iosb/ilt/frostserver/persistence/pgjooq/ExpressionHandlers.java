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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState.ALIAS_ROOT;

import de.fraunhofer.iosb.ilt.frostserver.parser.query.FunctionRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.ExpressionHandlers.JooqExpHlpr.PathState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.ArrayConstandFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.FieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.NullWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.SimpleFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaDateTimeWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaDurationWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.TimeFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.PropertyReference;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionHelper;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.ConstantList;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntervalConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.LineStringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.NullConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PointConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PolygonConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.TimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.TimeObjectConstant;
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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.Any;
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
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.PlainTime;
import net.time4j.ZonalDateTime;
import net.time4j.range.MomentInterval;
import org.geolatte.geom.Geometry;
import org.jooq.Condition;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers experssion handlers for default expressions for the jooq
 * persistance managers.
 */
public class ExpressionHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionHandlers.class.getName());

    public interface JooqExpHlpr extends ExpressionHelper<FieldWrapper> {

        public QueryBuilder getQueryBuilder();

        public QueryState getQueryState();

        public void setQueryState(QueryState state);

        public void walkPath(PathState state, int startIdx, Path path) throws IllegalArgumentException;

        public QueryState walkAnyPath(final QueryState<?> parentQueryState, Any node, final TableCollection tc) throws IllegalArgumentException;

        public Field[] findPair(FieldWrapper p1, FieldWrapper p2);

        public FieldWrapper handle(Expression<?> e);

        public static class PathState {

            public TableRef pathTableRef;
            public List<Property> elements;
            public FieldWrapper finalExpression = null;
            public int curIndex;
            public boolean finished = false;
        }
    }

    public static void addExpressionHandlers(FunctionRegistry fr, PersistenceManager pm) {
        if (pm instanceof JooqPersistenceManager) {
            fr.getExpression(Add.class).setHandler((Add exp, ExpressionHelper<FieldWrapper> h) -> ImpMath.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(After.class).setHandler((After exp, ExpressionHelper<FieldWrapper> h) -> ImpIntrvls.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(And.class).setHandler((And exp, ExpressionHelper<FieldWrapper> h) -> ImpLogicOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Any.class).setHandler((Any exp, ExpressionHelper<FieldWrapper> h) -> ImpOther.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Before.class).setHandler((Before exp, ExpressionHelper<FieldWrapper> h) -> ImpIntrvls.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(BooleanConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(Ceiling.class).setHandler((Ceiling exp, ExpressionHelper<FieldWrapper> h) -> ImpMath.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Concat.class).setHandler((Concat exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(ConstantList.class).setHandler((Expression expression, ExpressionHelper helper) -> new ArrayConstandFieldWrapper((ConstantList) expression));
            fr.getExpression(Date.class).setHandler((Date exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(DateConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(DateTimeConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(Day.class).setHandler((Day exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Divide.class).setHandler((Divide exp, ExpressionHelper<FieldWrapper> h) -> ImpMath.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(DoubleConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(DurationConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(During.class).setHandler((During exp, ExpressionHelper<FieldWrapper> h) -> ImpIntrvls.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(EndsWith.class).setHandler((EndsWith exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Equal.class).setHandler((Equal exp, ExpressionHelper<FieldWrapper> h) -> ImpCmpOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Finishes.class).setHandler((Finishes exp, ExpressionHelper<FieldWrapper> h) -> ImpIntrvls.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Floor.class).setHandler((Floor exp, ExpressionHelper<FieldWrapper> h) -> ImpMath.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(FractionalSeconds.class).setHandler((FractionalSeconds exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(GeoDistance.class).setHandler((GeoDistance exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(GeoIntersects.class).setHandler((GeoIntersects exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(GeoLength.class).setHandler((GeoLength exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(GreaterEqual.class).setHandler((GreaterEqual exp, ExpressionHelper<FieldWrapper> h) -> ImpCmpOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(GreaterThan.class).setHandler((GreaterThan exp, ExpressionHelper<FieldWrapper> h) -> ImpCmpOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Hour.class).setHandler((Hour exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(In.class).setHandler((In exp, ExpressionHelper<FieldWrapper> h) -> ImpCmpOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(IndexOf.class).setHandler((IndexOf exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(IntegerConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(IntervalConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(Length.class).setHandler((Length exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(LessEqual.class).setHandler((LessEqual exp, ExpressionHelper<FieldWrapper> h) -> ImpCmpOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(LessThan.class).setHandler((LessThan exp, ExpressionHelper<FieldWrapper> h) -> ImpCmpOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(MaxDateTime.class).setHandler((exp, h) -> new StaDateTimeWrapper(JooqAbsPersistenceManager.DATETIME_MAX, true));
            fr.getExpression(Meets.class).setHandler((Meets exp, ExpressionHelper<FieldWrapper> h) -> ImpIntrvls.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(MinDateTime.class).setHandler((exp, h) -> new StaDateTimeWrapper(JooqAbsPersistenceManager.DATETIME_MIN, true));
            fr.getExpression(Minute.class).setHandler((Minute exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Modulo.class).setHandler((Modulo exp, ExpressionHelper<FieldWrapper> h) -> ImpMath.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Month.class).setHandler((Month exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Multiply.class).setHandler((Multiply exp, ExpressionHelper<FieldWrapper> h) -> ImpMath.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Not.class).setHandler((Not exp, ExpressionHelper<FieldWrapper> h) -> ImpLogicOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(NotEqual.class).setHandler((NotEqual exp, ExpressionHelper<FieldWrapper> h) -> ImpCmpOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Now.class).setHandler((Now exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(NullConstant.class).setHandler((exp, h) -> new NullWrapper());
            fr.getExpression(Or.class).setHandler((Or exp, ExpressionHelper<FieldWrapper> h) -> ImpLogicOps.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Overlaps.class).setHandler((Overlaps exp, ExpressionHelper<FieldWrapper> h) -> ImpIntrvls.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Path.class).setHandler((Path exp, ExpressionHelper<FieldWrapper> h) -> ImpOther.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(PrincipalName.class).setHandler((exp, h) -> ImpOther.handle(exp));
            fr.getExpression(Round.class).setHandler((Round exp, ExpressionHelper<FieldWrapper> h) -> ImpMath.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Second.class).setHandler((Second exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Starts.class).setHandler((Starts exp, ExpressionHelper<FieldWrapper> h) -> ImpIntrvls.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(StartsWith.class).setHandler((StartsWith exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(STContains.class).setHandler((STContains exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(STCrosses.class).setHandler((STCrosses exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(STDisjoint.class).setHandler((STDisjoint exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(STEquals.class).setHandler((STEquals exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(STIntersects.class).setHandler((STIntersects exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(STOverlaps.class).setHandler((STOverlaps exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(STRelate.class).setHandler((STRelate exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(StringConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(STTouches.class).setHandler((STTouches exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(STWithin.class).setHandler((STWithin exp, ExpressionHelper<FieldWrapper> h) -> ImpGeo.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Substring.class).setHandler((Substring exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(SubstringOf.class).setHandler((SubstringOf exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Subtract.class).setHandler((Subtract exp, ExpressionHelper<FieldWrapper> h) -> ImpMath.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Time.class).setHandler((Time exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(TimeConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(TimeObjectConstant.class).setHandler((exp, h) -> ImpConst.handle(exp));
            fr.getExpression(ToLower.class).setHandler((ToLower exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(TotalOffsetMinutes.class).setHandler((TotalOffsetMinutes exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(ToUpper.class).setHandler((ToUpper exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Trim.class).setHandler((Trim exp, ExpressionHelper<FieldWrapper> h) -> ImpString.handle(exp, (JooqExpHlpr) h));
            fr.getExpression(Year.class).setHandler((Year exp, ExpressionHelper<FieldWrapper> h) -> ImpTime.handle(exp, (JooqExpHlpr) h));
        }
        if (pm instanceof PostgresPersistenceManager) {
            fr.getExpression(LineStringConstant.class).setHandler((exp, h) -> ImpPostgis.handle(exp));
            fr.getExpression(PointConstant.class).setHandler((exp, h) -> ImpPostgis.handle(exp));
            fr.getExpression(PolygonConstant.class).setHandler((exp, h) -> ImpPostgis.handle(exp));
        }
        if (pm instanceof MariadbPersistenceManager) {
            fr.getExpression(LineStringConstant.class).setHandler((exp, h) -> ImpMariaDb.handle(exp));
            fr.getExpression(PointConstant.class).setHandler((exp, h) -> ImpMariaDb.handle(exp));
            fr.getExpression(PolygonConstant.class).setHandler((exp, h) -> ImpMariaDb.handle(exp));
        }
        for (Expression<?> e : fr.getExpressions()) {
            if (!e.hasHandler()) {
                LOGGER.warn("Unhandled expression: {}", e);
            }
        }

    }

    public static class ImpConst {

        public static FieldWrapper handle(BooleanConstant node) {
            return new SimpleFieldWrapper(Boolean.TRUE.equals(node.getValue()) ? DSL.condition("TRUE") : DSL.condition("FALSE"));
        }

        public static FieldWrapper handle(DateConstant node) {
            PlainDate date = node.getValue();
            Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            instance.set(date.getYear(), date.getMonth() - 1, date.getDayOfMonth());
            return new SimpleFieldWrapper(DSL.inline(new java.sql.Date(instance.getTimeInMillis())));
        }

        public static FieldWrapper handle(DateTimeConstant node) {
            ZonalDateTime value = node.getValue();
            return new StaDateTimeWrapper(value.toMoment(), true);
        }

        public static FieldWrapper handle(DoubleConstant node) {
            return new SimpleFieldWrapper(DSL.val(node.getValue()));
        }

        public static FieldWrapper handle(DurationConstant node) {
            return new StaDurationWrapper(node);
        }

        public static FieldWrapper handle(IntervalConstant node) {
            MomentInterval value = node.getValue();
            return new StaTimeIntervalWrapper(
                    value.getStartAsMoment(),
                    value.getEndAsMoment());
        }

        public static FieldWrapper handle(TimeObjectConstant node) {
            LOGGER.error("TimeObjects should never appear in expressions!");
            throw new IllegalArgumentException("TimeObjects should never appear in expressions!");
        }

        public static FieldWrapper handle(IntegerConstant node) {
            return new SimpleFieldWrapper(DSL.val(node.getValue()));
        }

        public static FieldWrapper handle(StringConstant node) {
            return new SimpleFieldWrapper(DSL.value(node.getValue()));
        }

        public static FieldWrapper handle(TimeConstant node) {
            PlainTime time = node.getValue();
            Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            instance.set(1970, 1, 1, time.getHour(), time.getMinute(), time.getSecond());
            return new SimpleFieldWrapper(DSL.inline(new java.sql.Time(instance.getTimeInMillis())));
        }

    }

    public static class ImpCmpOps {

        public static FieldWrapper handle(Equal node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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

            Field[] pair = h.findPair(p1, p2);
            return new SimpleFieldWrapper(pair[0].eq(pair[1]));
        }

        public static FieldWrapper handle(GreaterEqual node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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
            Field[] pair = h.findPair(p1, p2);
            return new SimpleFieldWrapper(pair[0].greaterOrEqual(pair[1]));
        }

        public static FieldWrapper handle(GreaterThan node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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
            Field[] pair = h.findPair(p1, p2);
            return new SimpleFieldWrapper(pair[0].greaterThan(pair[1]));
        }

        public static FieldWrapper handle(LessEqual node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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
            Field[] pair = h.findPair(p1, p2);
            return new SimpleFieldWrapper(pair[0].lessOrEqual(pair[1]));
        }

        public static FieldWrapper handle(LessThan node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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
            Field[] pair = h.findPair(p1, p2);
            return new SimpleFieldWrapper(pair[0].lt(pair[1]));
        }

        public static FieldWrapper handle(NotEqual node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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
            Field[] pair = h.findPair(p1, p2);
            return new SimpleFieldWrapper(pair[0].ne(pair[1]));
        }

        public static FieldWrapper handle(In node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p2 instanceof ArrayConstandFieldWrapper clP2) {
                return new SimpleFieldWrapper(p1.getDefaultField().in(clP2.getValueList()));
            }
            Field[] pair = h.findPair(p1, p2);
            if (p2 instanceof JsonFieldFactory.JsonFieldWrapper jP2) {
                return jP2.contains(pair[0]);
            } else {
                return new SimpleFieldWrapper(pair[0].in(pair[1]));
            }
        }

    }

    public static class ImpLogicOps {

        public static FieldWrapper handle(And node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p1.isCondition() && p2.isCondition()) {
                return new SimpleFieldWrapper(p1.getCondition().and(p2.getCondition()));
            }
            throw new IllegalArgumentException("And requires two conditions, got " + p1 + " & " + p2);
        }

        public static FieldWrapper handle(Not node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            if (p1.isCondition()) {
                return new SimpleFieldWrapper(p1.getCondition().not());
            }
            throw new IllegalArgumentException("Not requires a condition, got " + p1);
        }

        public static FieldWrapper handle(Or node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p1.isCondition() && p2.isCondition()) {
                return new SimpleFieldWrapper(p1.getCondition().or(p2.getCondition()));
            }
            throw new IllegalArgumentException("Or requires two conditions, got " + p1 + " & " + p2);
        }

    }

    public static class ImpMath {

        public static FieldWrapper handle(Add node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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

        public static FieldWrapper handle(Divide node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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

        public static FieldWrapper handle(Modulo node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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

        public static FieldWrapper handle(Multiply node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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

        public static FieldWrapper handle(Subtract node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
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

        public static FieldWrapper handle(Ceiling node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            Field<Number> n1 = p1.getFieldAsType(Number.class, true);
            return new SimpleFieldWrapper(DSL.ceil(n1));
        }

        public static FieldWrapper handle(Floor node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            Field<Number> n1 = p1.getFieldAsType(Number.class, true);
            return new SimpleFieldWrapper(DSL.floor(n1));
        }

        public static FieldWrapper handle(Round node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            Field<Number> n1 = p1.getFieldAsType(Number.class, true);
            return new SimpleFieldWrapper(DSL.round(n1));
        }

    }

    public static class ImpIntrvls {

        public static FieldWrapper handle(After node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p1 instanceof TimeFieldWrapper timeExpression) {
                return timeExpression.after(p2);
            }
            throw new IllegalArgumentException("After can only be used on times, not on " + p1.getClass().getName());
        }

        public static FieldWrapper handle(Before node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p1 instanceof TimeFieldWrapper timeExpression) {
                return timeExpression.before(p2);
            }
            throw new IllegalArgumentException("Before can only be used on times, not on " + p1.getClass().getName());
        }

        public static FieldWrapper handle(Meets node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p1 instanceof TimeFieldWrapper timeExpression) {
                return timeExpression.meets(p2);
            }
            throw new IllegalArgumentException("Meets can only be used on times, not on " + p1.getClass().getName());
        }

        public static FieldWrapper handle(During node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p2 instanceof StaTimeIntervalWrapper ti2) {
                return ti2.contains(p1);
            }
            throw new IllegalArgumentException("Second parameter of 'during' has to be an interval.");
        }

        public static FieldWrapper handle(Overlaps node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p1 instanceof TimeFieldWrapper timeExpression) {
                return timeExpression.overlaps(p2);
            }
            throw new IllegalArgumentException("Overlaps can only be used on times, not on " + p1.getClass().getName());
        }

        public static FieldWrapper handle(Starts node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p1 instanceof TimeFieldWrapper timeExpression) {
                return timeExpression.starts(p2);
            }
            throw new IllegalArgumentException("Starts can only be used on times, not on " + p1.getClass().getName());
        }

        public static FieldWrapper handle(Finishes node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            FieldWrapper p1 = h.handle(params.get(0));
            FieldWrapper p2 = h.handle(params.get(1));
            if (p1 instanceof TimeFieldWrapper timeExpression) {
                return timeExpression.finishes(p2);
            }
            throw new IllegalArgumentException("Finishes can only be used on times, not on " + p1.getClass().getName());
        }

    }

    public static class ImpTime {

        public static FieldWrapper handle(Date node, JooqExpHlpr h) {
            List<Expression<?>> params = node.getParameters();
            Expression<?> p1 = params.get(0);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = null;
            if (params.size() > 1) {
                Expression<?> p2 = params.get(1);
                e2 = h.handle(p2);
            }
            if (e1 instanceof TimeFieldWrapper timeExpression) {
                if (e2 != null) {
                    final Field<String> zone = e2.getFieldAsType(String.class, true);
                    final Field<Timestamp> zonedTime = DSL.function("timezone_with_iso_offsets", java.sql.Timestamp.class, zone, timeExpression.getDateTime());
                    return new SimpleFieldWrapper(DSL.function("date", java.sql.Date.class, zonedTime));
                } else {
                    return new SimpleFieldWrapper(DSL.function("date", java.sql.Date.class, timeExpression.getDateTime()));
                }
            }
            Field<java.sql.Date> fieldAsDate = e1.getFieldAsType(java.sql.Date.class, true);
            if (fieldAsDate != null) {
                return new SimpleFieldWrapper(fieldAsDate);
            }
            throw new IllegalArgumentException("Date can only be used on times, not on " + e1.getClass().getName());
        }

        public static FieldWrapper datePartExtract(DatePart part, Function<?> node, JooqExpHlpr h) {
            final List<Expression<?>> params = node.getParameters();
            Expression<?> p1 = params.get(0);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = null;
            if (params.size() > 1) {
                Expression<?> p2 = params.get(1);
                e2 = h.handle(p2);
            }
            if (e1 instanceof TimeFieldWrapper timeExpression) {
                if (e2 != null) {
                    final Field<String> zone = e2.getFieldAsType(String.class, true);
                    final Field<Timestamp> zonedTime = DSL.function("timezone_with_iso_offsets", java.sql.Timestamp.class, zone, timeExpression.getDateTime());
                    return new SimpleFieldWrapper(DSL.extract(zonedTime, part));
                } else {
                    return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), part));
                }
            }
            throw new IllegalArgumentException(node.getName() + " can only be used on times, not on " + e1.getClass().getName());

        }

        public static FieldWrapper handle(Day node, JooqExpHlpr h) {
            return datePartExtract(DatePart.DAY, node, h);
        }

        public static FieldWrapper handle(FractionalSeconds node, JooqExpHlpr h) {
            Expression<?> param = node.getParameters().get(0);
            FieldWrapper input = h.handle(param);
            if (input instanceof TimeFieldWrapper timeExpression) {
                return new SimpleFieldWrapper(DSL.field("(date_part('SECONDS', TIMESTAMPTZ ?) - floor(date_part('SECONDS', TIMESTAMPTZ ?)))", Double.class, timeExpression.getDateTime(), timeExpression.getDateTime()));
            }
            throw new IllegalArgumentException("FractionalSeconds can only be used on times, not on " + input.getClass().getName());
        }

        public static FieldWrapper handle(Hour node, JooqExpHlpr h) {
            return datePartExtract(DatePart.HOUR, node, h);
        }

        public static FieldWrapper handle(Minute node, JooqExpHlpr h) {
            return datePartExtract(DatePart.MINUTE, node, h);
        }

        public static FieldWrapper handle(Month node, JooqExpHlpr h) {
            return datePartExtract(DatePart.MONTH, node, h);
        }

        public static FieldWrapper handle(Now node, JooqExpHlpr h) {
            return new StaDateTimeWrapper(DSL.field("now()", Moment.class));
        }

        public static FieldWrapper handle(Second node, JooqExpHlpr h) {
            return datePartExtract(DatePart.SECOND, node, h);
        }

        public static FieldWrapper handle(Time node, JooqExpHlpr h) {
            final List<Expression<?>> params = node.getParameters();
            Expression<?> p1 = params.get(0);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = null;
            if (params.size() > 1) {
                Expression<?> p2 = params.get(1);
                e2 = h.handle(p2);
            }
            if (e1 instanceof TimeFieldWrapper timeExpression) {
                if (e2 != null) {
                    final Field<String> zone = e2.getFieldAsType(String.class, true);
                    final Field<Timestamp> zonedTime = DSL.function("timezone_with_iso_offsets", java.sql.Timestamp.class, zone, timeExpression.getDateTime());
                    return new SimpleFieldWrapper(zonedTime.cast(SQLDataType.TIME));
                } else {
                    return new SimpleFieldWrapper(timeExpression.getDateTime().cast(SQLDataType.TIME));
                }
            }
            throw new IllegalArgumentException("Time can only be used on times, not on " + e1.getClass().getName());
        }

        public static FieldWrapper handle(TotalOffsetMinutes node, JooqExpHlpr h) {
            final List<Expression<?>> params = node.getParameters();
            Expression<?> p1 = params.get(0);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = null;
            if (params.size() > 1) {
                Expression<?> p2 = params.get(1);
                e2 = h.handle(p2);
            }
            if (e1 instanceof TimeFieldWrapper timeExpression) {
                if (e2 != null) {
                    final Field<String> zone = e2.getFieldAsType(String.class, true);
                    final Field<Timestamp> zonedTime = DSL.function("timezone_with_iso_offsets", java.sql.Timestamp.class, zone, timeExpression.getDateTime());
                    return new SimpleFieldWrapper(DSL.extract(zonedTime, DatePart.TIMEZONE).div(60));
                } else {
                    return new SimpleFieldWrapper(DSL.extract(timeExpression.getDateTime(), DatePart.TIMEZONE).div(60));
                }
            }
            throw new IllegalArgumentException("TotalOffsetMinutes can only be used on times, not on " + e1.getClass().getName());
        }

        public static FieldWrapper handle(Year node, JooqExpHlpr h) {
            return datePartExtract(DatePart.YEAR, node, h);
        }

    }

    public static class ImpGeo {

        public static FieldWrapper handle(GeoDistance node, JooqExpHlpr h) {
            Expression<?> p1 = node.getParameters().get(0);
            Expression<?> p2 = node.getParameters().get(1);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = h.handle(p2);
            Field<Geometry> g1 = e1.getFieldAsType(Geometry.class, true);
            Field<Geometry> g2 = e2.getFieldAsType(Geometry.class, true);
            if (g1 == null || g2 == null) {
                throw new IllegalArgumentException("GeoDistance requires two geometries, got " + e1 + " & " + e2);
            }
            return new SimpleFieldWrapper(DSL.function("ST_Distance", SQLDataType.NUMERIC, g1, g2));
        }

        public static FieldWrapper handle(GeoIntersects node, JooqExpHlpr h) {
            return stCompare(node, "ST_Intersects", h);
        }

        public static FieldWrapper handle(GeoLength node, JooqExpHlpr h) {
            Expression<?> p1 = node.getParameters().get(0);
            FieldWrapper e1 = h.handle(p1);
            Field<Geometry> g1 = e1.getFieldAsType(Geometry.class, true);
            if (g1 == null) {
                throw new IllegalArgumentException("GeoLength requires a geometry, got " + e1);
            }
            return new SimpleFieldWrapper(DSL.function("ST_Length", SQLDataType.NUMERIC, g1));
        }

        public static FieldWrapper handle(STContains node, JooqExpHlpr h) {
            return stCompare(node, "ST_Contains", h);
        }

        public static FieldWrapper handle(STCrosses node, JooqExpHlpr h) {
            return stCompare(node, "ST_Crosses", h);
        }

        public static FieldWrapper handle(STDisjoint node, JooqExpHlpr h) {
            return stCompare(node, "ST_Disjoint", h);
        }

        public static FieldWrapper handle(STEquals node, JooqExpHlpr h) {
            return stCompare(node, "ST_Equals", h);
        }

        public static FieldWrapper handle(STIntersects node, JooqExpHlpr h) {
            return stCompare(node, "ST_Intersects", h);
        }

        public static FieldWrapper handle(STOverlaps node, JooqExpHlpr h) {
            return stCompare(node, "ST_Overlaps", h);
        }

        public static FieldWrapper handle(STRelate node, JooqExpHlpr h) {
            Expression<?> p1 = node.getParameters().get(0);
            Expression<?> p2 = node.getParameters().get(1);
            Expression<?> p3 = node.getParameters().get(2);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = h.handle(p2);
            FieldWrapper e3 = h.handle(p3);
            Field<Geometry> g1 = e1.getFieldAsType(Geometry.class, true);
            Field<Geometry> g2 = e2.getFieldAsType(Geometry.class, true);
            Field<String> g3 = e3.getFieldAsType(String.class, true);
            if (g1 == null || g2 == null || g3 == null) {
                throw new IllegalArgumentException("STRelate requires two geometries and a string, got " + e1 + ", " + e2 + " & " + e3);
            }
            return new SimpleFieldWrapper(DSL.condition(DSL.function("ST_Relate", SQLDataType.BOOLEAN, g1, g2, g3)));
        }

        public static FieldWrapper handle(STTouches node, JooqExpHlpr h) {
            return stCompare(node, "ST_Touches", h);
        }

        public static FieldWrapper handle(STWithin node, JooqExpHlpr h) {
            return stCompare(node, "ST_Within", h);
        }

        public static FieldWrapper stCompare(Function<?> node, String functionName, JooqExpHlpr h) {
            Expression<?> p1 = node.getParameters().get(0);
            Expression<?> p2 = node.getParameters().get(1);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = h.handle(p2);
            Field<Geometry> g1 = e1.getFieldAsType(Geometry.class, true);
            Field<Geometry> g2 = e2.getFieldAsType(Geometry.class, true);
            if (g1 == null || g2 == null) {
                throw new IllegalArgumentException(functionName + " requires two geometries, got " + e1 + " & " + e2);
            }
            return new SimpleFieldWrapper(DSL.condition(DSL.function(functionName, SQLDataType.BOOLEAN, g1, g2)));
        }

    }

    public static class ImpOther {

        public static FieldWrapper handle(Path node, JooqExpHlpr h) {
            PathState state = new PathState();
            state.elements = node.getElements();
            var storedQueryState = h.getQueryState();
            try {
                Property firstElement = state.elements.get(0);
                int startIdx = 0;
                if (firstElement instanceof PropertyReference pr) {
                    startIdx = 1;
                    h.setQueryState(h.getQueryState().findStateForAlias(pr.getName()));
                } else {
                    h.setQueryState(h.getQueryState().findStateForAlias(ALIAS_ROOT));
                }
                state.pathTableRef = h.getQueryState().getTableRef();
                h.walkPath(state, startIdx, node);
            } finally {
                h.setQueryState(storedQueryState);
            }
            return state.finalExpression;
        }

        public static FieldWrapper handle(Any node, JooqExpHlpr h) {
            final TableCollection tc = h.getQueryBuilder().getTableCollection();
            final QueryState<?> parentQueryState = h.getQueryState();

            QueryState existsQueryState = h.walkAnyPath(parentQueryState, node, tc);
            if (existsQueryState == null) {
                throw new IllegalStateException("Failed to parse any().");
            }

            // Set our subQuery state to be the active one.
            h.setQueryState(existsQueryState);
            try {
                List<Expression<?>> params = node.getParameters();
                FieldWrapper p1 = h.handle(params.get(0));
                if (!p1.isCondition()) {
                    throw new IllegalArgumentException("Any() requires a condition, got " + p1);
                }
                Condition exists = DSL.exists(DSL.selectOne().from(existsQueryState.getSqlFrom()).where(existsQueryState.getSqlWhere().and(p1.getCondition())));
                return new SimpleFieldWrapper(exists);
            } finally {
                // Set the query state back to what it was.
                h.setQueryState(parentQueryState);
            }
        }

        public static FieldWrapper handle(PrincipalName node) {
            return new SimpleFieldWrapper(DSL.value(node.getValue()));
        }

        public static FieldWrapper handle(ContextEntityProperty node) {
            return new SimpleFieldWrapper(DSL.value(node.getValue()));
        }
    }

    public static class ImpString {

        public static FieldWrapper handle(Concat node, JooqExpHlpr h) {
            Expression<?> p1 = node.getParameters().get(0);
            Expression<?> p2 = node.getParameters().get(1);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = h.handle(p2);
            Field<String> s1 = e1.getFieldAsType(String.class, true);
            Field<String> s2 = e2.getFieldAsType(String.class, true);
            return new SimpleFieldWrapper(s1.concat(s2));
        }

        public static FieldWrapper handle(EndsWith node, JooqExpHlpr h) {
            Expression<?> p1 = node.getParameters().get(0);
            Expression<?> p2 = node.getParameters().get(1);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = h.handle(p2);
            Field<String> s1 = e1.getFieldAsType(String.class, true);
            Field<String> s2 = e2.getFieldAsType(String.class, true);
            return new SimpleFieldWrapper(s1.endsWith(s2));
        }

        public static FieldWrapper handle(IndexOf node, JooqExpHlpr h) {
            Expression<?> p1 = node.getParameters().get(0);
            Expression<?> p2 = node.getParameters().get(1);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = h.handle(p2);
            Field<String> s1 = e1.getFieldAsType(String.class, true);
            Field<String> s2 = e2.getFieldAsType(String.class, true);
            return new SimpleFieldWrapper(DSL.position(s1, s2));
        }

        public static FieldWrapper handle(Length node, JooqExpHlpr h) {
            Expression<?> param = node.getParameters().get(0);
            FieldWrapper e1 = h.handle(param);
            Field<String> s1 = e1.getFieldAsType(String.class, true);
            return new SimpleFieldWrapper(DSL.length(s1));
        }

        public static FieldWrapper handle(StartsWith node, JooqExpHlpr h) {
            Expression<?> p1 = node.getParameters().get(0);
            Expression<?> p2 = node.getParameters().get(1);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = h.handle(p2);
            Field<String> s1 = e1.getFieldAsType(String.class, true);
            Field<String> s2 = e2.getFieldAsType(String.class, true);
            return new SimpleFieldWrapper(s1.startsWith(s2));
        }

        public static FieldWrapper handle(Substring node, JooqExpHlpr h) {
            final List<Expression<?>> params = node.getParameters();
            Expression<?> p1 = params.get(0);
            Expression<?> p2 = params.get(1);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = h.handle(p2);
            Field<String> s1 = e1.getFieldAsType(String.class, true);
            Field<Integer> n2 = e2.getFieldAsType(Integer.class, true);
            if (params.size() > 2) {
                Expression<?> p3 = params.get(2);
                FieldWrapper e3 = h.handle(p3);
                Field<Integer> n3 = e3.getFieldAsType(Integer.class, true);
                return new SimpleFieldWrapper(DSL.substring(s1, n2.add(1), n3));
            }
            return new SimpleFieldWrapper(DSL.substring(s1, n2.add(1)));
        }

        public static FieldWrapper handle(SubstringOf node, JooqExpHlpr h) {
            Expression<?> p1 = node.getParameters().get(0);
            Expression<?> p2 = node.getParameters().get(1);
            FieldWrapper e1 = h.handle(p1);
            FieldWrapper e2 = h.handle(p2);
            Field<String> s1 = e1.getFieldAsType(String.class, true);
            Field<String> s2 = e2.getFieldAsType(String.class, true);
            return new SimpleFieldWrapper(s2.contains(s1));
        }

        public static FieldWrapper handle(ToLower node, JooqExpHlpr h) {
            Expression<?> param = node.getParameters().get(0);
            FieldWrapper input = h.handle(param);
            Field<String> field = input.getFieldAsType(String.class, true);
            return new SimpleFieldWrapper(DSL.lower(field));
        }

        public static FieldWrapper handle(ToUpper node, JooqExpHlpr h) {
            Expression<?> param = node.getParameters().get(0);
            FieldWrapper input = h.handle(param);
            Field<String> field = input.getFieldAsType(String.class, true);
            return new SimpleFieldWrapper(DSL.upper(field));
        }

        public static FieldWrapper handle(Trim node, JooqExpHlpr h) {
            Expression<?> param = node.getParameters().get(0);
            FieldWrapper input = h.handle(param);
            Field<String> field = input.getFieldAsType(String.class, true);
            return new SimpleFieldWrapper(DSL.trim(field));
        }

    }

    public static class ImpPostgis {

        private static final String ST_GEOM_FROM_EWKT = "ST_GeomFromEWKT(?)";

        public static FieldWrapper handle(LineStringConstant node) {
            final String wktString = node.getWktWithSrid(4326);
            return new SimpleFieldWrapper(DSL.field(ST_GEOM_FROM_EWKT, PostGisGeometryBinding.dataType(), wktString));
        }

        public static FieldWrapper handle(PointConstant node) {
            final String wktString = node.getWktWithSrid(4326);
            return new SimpleFieldWrapper(DSL.field(ST_GEOM_FROM_EWKT, PostGisGeometryBinding.dataType(), wktString));
        }

        public static FieldWrapper handle(PolygonConstant node) {
            final String wktString = node.getWktWithSrid(4326);
            return new SimpleFieldWrapper(DSL.field(ST_GEOM_FROM_EWKT, PostGisGeometryBinding.dataType(), wktString));
        }

    }

    public static class ImpMariaDb {

        private static final String ST_GEOMFROMTEXT = "ST_GeomFromText(?)";

        public static FieldWrapper handle(LineStringConstant node) {
            return new SimpleFieldWrapper(DSL.field(ST_GEOMFROMTEXT, PostGisGeometryBinding.dataType(), node.getSource()));
        }

        public static FieldWrapper handle(PointConstant node) {
            return new SimpleFieldWrapper(DSL.field(ST_GEOMFROMTEXT, PostGisGeometryBinding.dataType(), node.getSource()));
        }

        public static FieldWrapper handle(PolygonConstant node) {
            return new SimpleFieldWrapper(DSL.field(ST_GEOMFROMTEXT, PostGisGeometryBinding.dataType(), node.getSource()));
        }

    }
}
