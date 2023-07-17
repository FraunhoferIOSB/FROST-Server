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
package de.fraunhofer.iosb.ilt.frostserver.parser.query;

import de.fraunhofer.iosb.ilt.frostserver.query.PropertyPlaceholder;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.DynamicContext;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.NullConstant;
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
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.Node;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.Node.Visitor;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.Token;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_AdditiveExpression;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_BoolFunction;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_ComparativeExpression;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_ConstantsList;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_LogicalAnd;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_LogicalExpression;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_MathFunction;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_MultiplicativeExpression;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_NegationExpression;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_PlainPath;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.P_UnaryExpression;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_BOOL;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_DATE;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_DATETIME;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_DATETIMEINTERVAL;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_DOUBLE;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_DURATION;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_GEO_STR_LIT;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_LONG;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_NULL;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_STR_LIT;
import de.fraunhofer.iosb.ilt.frostserver.util.queryparser.nodes.T_TIME;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jab
 */
public class ExpressionParser extends Visitor {

    private static final String GEOGRAPHY_REGEX = "^geography'([^']+)'$";
    private static final Pattern GEORAPHY_PATTERN = Pattern.compile(GEOGRAPHY_REGEX);

    public enum Operator {
        // Logical
        OP_NOT("not", Not.class),
        OP_AND("and", And.class),
        OP_OR("or", Or.class),
        // Math
        OP_ADD("add", Add.class),
        OP_SUB("sub", Subtract.class),
        OP_MUL("mul", Multiply.class),
        OP_DIV("div", Divide.class),
        OP_MOD("mod", Modulo.class),
        // Comparison
        OP_EQUAL("eq", Equal.class),
        OP_NOT_EQUAL("ne", NotEqual.class),
        OP_GREATER_THAN("gt", GreaterThan.class),
        OP_GREATER_EQUAL("ge", GreaterEqual.class),
        OP_LESS_THAN("lt", LessThan.class),
        OP_LESS_EQUAL("le", LessEqual.class),
        OP_IN("in", In.class),
        // String
        OP_SUBSTRING_OF("substringof", SubstringOf.class),
        OP_ENDS_WITH("endswith", EndsWith.class),
        OP_STARTS_WITH("startswith", StartsWith.class),
        OP_LENGTH("length", Length.class),
        OP_INDEX_OF("indexof", IndexOf.class),
        OP_SUBSTRING("substring", Substring.class),
        OP_TO_LOWER("tolower", ToLower.class),
        OP_TO_UPPER("toupper", ToUpper.class),
        OP_TRIM("trim", Trim.class),
        OP_CONCAT("concat", Concat.class),
        // DateTime
        OP_YEAR("year", Year.class),
        OP_MONTH("month", Month.class),
        OP_DAY("day", Day.class),
        OP_HOUR("hour", Hour.class),
        OP_MINUTE("minute", Minute.class),
        OP_SECOND("second", Second.class),
        OP_FRACTIONAL_SECONDS("fractionalseconds", FractionalSeconds.class),
        OP_DATE("date", Date.class),
        OP_TIME("time", Time.class),
        OP_TOTAL_OFFSET_MINUTES("totaloffsetminutes", TotalOffsetMinutes.class),
        OP_NOW("now", Now.class),
        OP_MIN_DATETIME("mindatetime", MinDateTime.class),
        OP_MAX_DATETIME("maxdatetime", MaxDateTime.class),
        // Allen's interval algebra
        OP_BEFORE("before", Before.class),
        OP_AFTER("after", After.class),
        OP_MEETS("meets", Meets.class),
        OP_DURING("during", During.class),
        OP_OVERLAPS("overlaps", Overlaps.class),
        OP_STARTS("starts", Starts.class),
        OP_FINISHES("finishes", Finishes.class),
        // Math
        OP_ROUND("round", Round.class),
        OP_FLOOR("floor", Floor.class),
        OP_CEILING("ceiling", Ceiling.class),
        // Geo
        OP_GEO_DISTANCE("geo.distance", GeoDistance.class),
        OP_GEO_LENGTH("geo.length", GeoLength.class),
        OP_GEO_INTERSECTS("geo.intersects", GeoIntersects.class),
        OP_ST_EQUALS("st_equals", STEquals.class),
        OP_ST_DISJOINT("st_disjoint", STDisjoint.class),
        OP_ST_TOUCHES("st_touches", STTouches.class),
        OP_ST_WITHIN("st_within", STWithin.class),
        OP_ST_OVERLAPS("st_overlaps", STOverlaps.class),
        OP_ST_CROSSES("st_crosses", STCrosses.class),
        OP_ST_INTERSECTS("st_intersects", STIntersects.class),
        OP_ST_CONTAINS("st_contains", STContains.class),
        OP_ST_RELATE("st_relate", STRelate.class),
        // Current user related
        OP_PRINCIPAL_NAME("principalName", PrincipalName.class, true);

        private static final Map<String, Operator> BY_KEY = new HashMap<>();

        static {
            for (Operator o : Operator.values()) {
                BY_KEY.put(o.urlKey, o);
            }
        }
        /**
         * The operator/function name as it appears in URLs.
         */
        public final String urlKey;

        /**
         * The java class implementing the function.
         */
        public final Class<? extends Function> implementingClass;

        /**
         * Flag indicating only admin-users may use this function.
         */
        public final boolean adminOnly;

        private Operator(String urlKey, Class<? extends Function> implementingClass) {
            this(urlKey, implementingClass, false);
        }

        private Operator(String urlKey, Class<? extends Function> implementingClass, boolean adminOnly) {
            this.urlKey = urlKey;
            this.implementingClass = implementingClass;
            this.adminOnly = adminOnly;
        }

        public Function instantiate(DynamicContext context) {
            try {
                return implementingClass.getDeclaredConstructor().newInstance().setContext(context);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                throw new IllegalStateException("problem executing '" + this + "'", ex);
            }
        }

        public static Operator fromKey(String key, boolean admin) {
            if (key.endsWith("(")) {
                key = key.substring(0, key.length() - 1);
            }
            Operator operator = BY_KEY.get(key);
            if (operator == null || operator.adminOnly && !admin) {
                throw new IllegalArgumentException("Unknown operator: '" + key + "'.");
            }
            return operator;
        }
    }

    private final QueryParser queryParser;
    private final boolean admin;
    private final DynamicContext context;
    private Expression currentExpression;

    public ExpressionParser(QueryParser queryParser, boolean admin, DynamicContext context) {
        this.queryParser = queryParser;
        this.admin = admin;
        this.context = context;
    }

    public Expression parseExpression(Node node) {
        currentExpression = null;
        visit(node);
        return currentExpression;
    }

    private void addToCurrentExpression(Expression toAdd) {
        if (currentExpression == null) {
            currentExpression = toAdd;
        } else {
            currentExpression.addParameter(toAdd);
        }
    }

    public void visit(P_PlainPath node) {
        PropertyPlaceholder property = queryParser.handle(node);
        Path path = new Path(property);
        addToCurrentExpression(path);
    }

    public void visit(P_LogicalExpression node) {
        handleOperatorFunction(node);
    }

    public void visit(P_LogicalAnd node) {
        handleOperatorFunction(node);
    }

    public void visit(P_MultiplicativeExpression node) {
        handleOperatorFunction(node);
    }

    public void visit(P_AdditiveExpression node) {
        handleOperatorFunction(node);
    }

    private void handleOperatorFunction(Node node) {
        Expression previousExpression = currentExpression;

        final int childCount = node.getChildCount();
        if (childCount < 3 || childCount % 2 == 0) {
            throw new IllegalArgumentException("'" + node.getClass().getName() + "' must have at least two parameters");
        }
        // Find first operator
        String operatorName = ((Token) node.getChild(1)).getImage();
        Function function = getFunction(operatorName);
        currentExpression = function;
        // Put the first two parameters into the first (current) operator
        visit(node.getChild(0));
        visit(node.getChild(2));

        for (int i = 3; i < childCount; i += 2) {
            operatorName = ((Token) node.getChild(i)).getImage();
            function = getFunction(operatorName);
            function.addParameter(currentExpression);
            currentExpression = function;
            visit(node.getChild(i + 1));
        }
        currentExpression = previousExpression;
        addToCurrentExpression(function);
    }

    public void handleFunction(Node node) {
        Expression previousExpression = currentExpression;
        final int childCount = node.getChildCount();

        String operator = node.getFirstToken().getImage();
        Function function = getFunction(operator);
        currentExpression = function;
        for (int i = 1; i < childCount; i += 2) {
            visit(node.getChild(i));
        }

        currentExpression = previousExpression;
        addToCurrentExpression(function);
    }

    public void visit(P_NegationExpression node) {
        if (node.getChildCount() != 2) {
            throw new IllegalArgumentException("'not' must have exactly one parameter");
        }
        handleFunction(node);
    }

    public void visit(P_UnaryExpression node) {
        if (node.getChildCount() != 3) {
            throw new IllegalArgumentException("Unary experssion must have exactly one parameter");
        }
        visit(node.getChild(1));
    }

    public void visit(P_BoolFunction node) {
        handleFunction(node);
    }

    public void visit(P_MathFunction node) {
        handleFunction(node);
    }

    public void visit(P_ComparativeExpression node) {
        Expression previousExpression = currentExpression;

        if (node.getChildCount() != 3) {
            throw new IllegalArgumentException("comparison must have exactly 2 children");
        }

        String operator = ((Token) node.getChild(1)).getImage();
        Function function = getFunction(operator);
        currentExpression = function;
        visit(node.getChild(0));
        visit(node.getChild(2));

        currentExpression = previousExpression;
        addToCurrentExpression(function);
    }

    private Function getFunction(String operator) {
        return Operator.fromKey(operator, admin).instantiate(context);
    }

    public void visit(P_ConstantsList node) {
        Expression previousExpression = currentExpression;
        ConstantList<Object> cl = new ConstantList<>();
        currentExpression = cl;
        for (Node child : node.children()) {
            visit(child);
        }
        currentExpression = previousExpression;
        addToCurrentExpression(cl);
    }

    public void visit(T_STR_LIT node) {
        String image = node.getImage();
        if (image.length() < 2) {
            throw new IllegalArgumentException("String constant too short.");
        }
        addToCurrentExpression(new StringConstant(image.substring(1, image.length() - 1)));
    }

    public void visit(T_GEO_STR_LIT node) {
        String image = node.getImage();
        Matcher matcher = GEORAPHY_PATTERN.matcher(image);
        if (matcher.matches()) {
            addToCurrentExpression(GeoJsonConstant.fromString(matcher.group(1).trim()));
        } else {
            throw new IllegalArgumentException("invalid geography string '" + StringHelper.cleanForLogging(image) + "'");
        }
    }

    public void visit(T_DURATION node) {
        String image = node.getImage();
        DurationConstant value = DurationConstant.parse(image.substring(9, image.length() - 1));
        addToCurrentExpression(value);
    }

    public void visit(T_DATETIMEINTERVAL node) {
        String image = node.getImage();
        IntervalConstant value = IntervalConstant.parse(image);
        addToCurrentExpression(value);
    }

    public void visit(T_DATETIME node) {
        String image = node.getImage();
        DateTimeConstant value = DateTimeConstant.parse(image);
        addToCurrentExpression(value);
    }

    public void visit(T_DATE node) {
        String image = node.getImage();
        DateConstant value = DateConstant.parse(image);
        addToCurrentExpression(value);
    }

    public void visit(T_TIME node) {
        String image = node.getImage();
        TimeConstant value = TimeConstant.parse(image);
        addToCurrentExpression(value);
    }

    public void visit(T_LONG node) {
        String image = node.getImage();
        IntegerConstant value = new IntegerConstant(Long.valueOf(image));
        addToCurrentExpression(value);
    }

    public void visit(T_DOUBLE node) {
        String image = node.getImage();
        DoubleConstant value = new DoubleConstant(Double.valueOf(image));
        addToCurrentExpression(value);
    }

    public void visit(T_BOOL node) {
        String image = node.getImage();
        BooleanConstant value = new BooleanConstant(image);
        addToCurrentExpression(value);
    }

    public void visit(T_NULL node) {
        NullConstant value = new NullConstant();
        addToCurrentExpression(value);
    }

}
