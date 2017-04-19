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
package de.fraunhofer.iosb.ilt.sta.parser.query;

import de.fraunhofer.iosb.ilt.sta.path.CustomProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.query.expression.Expression;
import de.fraunhofer.iosb.ilt.sta.query.expression.Path;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.Constant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DateConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.GeoJsonConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.IntervalConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.TimeConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.FunctionTypeBinding;
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
import de.fraunhofer.iosb.ilt.sta.query.expression.function.geospatial.GeoDistance;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.geospatial.GeoIntersects;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.geospatial.GeoLength;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.logical.Not;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.logical.Or;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.math.Ceiling;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.math.Floor;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.math.Round;
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
import de.fraunhofer.iosb.ilt.sta.util.ParserHelper;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;

/**
 *
 * @author jab
 */
public class ExpressionParser extends AbstractParserVisitor {

    private static final String OP_NOT = "not";
    private static final String OP_AND = "and";
    private static final String OP_OR = "or";
    private static final String OP_ADD = "+";
    private static final String OP_SUB = "-";
    private static final String OP_MUL = "mul";
    private static final String OP_DIV = "div";
    private static final String OP_MOD = "mod";
    private static final String OP_EQUAL = "eq";
    private static final String OP_NOT_EQUAL = "ne";
    private static final String OP_GREATER_THAN = "gt";
    private static final String OP_GREATER_EQUAL = "ge";
    private static final String OP_LESS_THAN = "lt";
    private static final String OP_LESS_EQUAL = "le";
    private static final String OP_SUBSTRING_OF = "substringof";
    private static final String OP_ENDS_WITH = "endswith";
    private static final String OP_STARTS_WITH = "startswith";
    private static final String OP_LENGTH = "length";
    private static final String OP_INDEX_OF = "indexof";
    private static final String OP_SUBSTRING = "substring";
    private static final String OP_TO_LOWER = "tolower";
    private static final String OP_TO_UPPER = "toupper";
    private static final String OP_TRIM = "trim";
    private static final String OP_CONCAT = "concat";
    private static final String OP_YEAR = "year";
    private static final String OP_MONTH = "month";
    private static final String OP_DAY = "day";
    private static final String OP_HOUR = "hour";
    private static final String OP_MINUTE = "minute";
    private static final String OP_SECOND = "second";
    private static final String OP_FRACTIONAL_SECONDS = "fractionalseconds";
    private static final String OP_DATE = "date";
    private static final String OP_TIME = "time";
    private static final String OP_TOTAL_OFFSET_MINUTES = "totaloffsetminutes";
    private static final String OP_NOW = "now";
    private static final String OP_MIN_DATETIME = "mindatetime";
    private static final String OP_MAX_DATETIME = "maxdatetime";
    private static final String OP_BEFORE = "before";
    private static final String OP_AFTER = "after";
    private static final String OP_MEETS = "meets";
    private static final String OP_DURING = "during";
    private static final String OP_OVERLAPS = "overlaps";
    private static final String OP_STARTS = "starts";
    private static final String OP_FINISHES = "finishes";
    private static final String OP_ROUND = "round";
    private static final String OP_FLOOR = "floor";
    private static final String OP_CEILING = "ceiling";
    private static final String OP_GEO_DISTANCE = "geo.distance";
    private static final String OP_GEO_LENGTH = "geo.length";
    private static final String OP_GEO_INTERSECTS = "geo.intersects";
    private static final String OP_ST_EQUALS = "st_equals";
    private static final String OP_ST_DISJOINT = "st_disjoint";
    private static final String OP_ST_TOUCHES = "st_touches";
    private static final String OP_ST_WITHIN = "st_within";
    private static final String OP_ST_OVERLAPS = "st_overlaps";
    private static final String OP_ST_CROSSES = "st_crosses";
    private static final String OP_ST_INTERSECTS = "st_intersects";
    private static final String OP_ST_CONTAINS = "st_contains";
    private static final String OP_ST_RELATE = "st_relate";

    public static Expression parseExpression(Node node) {
        return new ExpressionParser().visit(node, null);
    }

    @Override
    public Path visit(ASTPlainPath node, Object data) {
        Path path = new Path();
        Property previous = null;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            if (!(child instanceof ASTPathElement)) {
                throw new IllegalArgumentException("alle childs of ASTPlainPath must be of type ASTPathElement");
            }
            Property property = visit((ASTPathElement) child, previous);
            if (property instanceof CustomProperty) {
                if (!(previous instanceof EntityProperty) && !(previous instanceof CustomProperty)) {
                    throw new IllegalArgumentException("Custom properties (" + property.getName() + ") are only allowed below entity properties or other custom properties.");
                }
                if (previous instanceof EntityProperty && !((EntityProperty) previous).hasCustomProperties) {
                    throw new IllegalArgumentException("Entity property " + previous.getName() + " does not have custom properties (" + property.getName() + ").");
                }
            }
            path.getElements().add(property);
            previous = property;
        }
        return path;
    }

    @Override
    public Property visit(ASTPathElement node, Object data) {
        // TODO only name or also ID allowed???
        if (node.getIdentifier() != null && !node.getIdentifier().isEmpty()) {
            throw new IllegalArgumentException("no identified paths are allowed inside expressions");
        }
        Property previous = null;
        if (data != null && data instanceof Property) {
            previous = (Property) data;
        }
        return ParserHelper.parseProperty(node.getName(), previous);
    }

    @Override
    public Expression visit(ASTFilter node, Object data) {
        if (node.jjtGetNumChildren() != 1) {
            throw new IllegalArgumentException("filter node must have exactly one child!");
        }
        return visit(node.jjtGetChild(0), data);
    }

    @Override
    public Or visit(ASTLogicalOr node, Object data) {
        return (Or) visitLogicalFunction(OP_OR, node, data);
    }

    @Override
    public And visit(ASTLogicalAnd node, Object data) {
        return (And) visitLogicalFunction(OP_AND, node, data);
    }

    private Function getLogicalFunction(String operator) {
        switch (operator) {
            case OP_AND: {
                return new And();
            }
            case OP_OR: {
                return new Or();
            }
            default:
                throw new IllegalArgumentException("unknown operator '" + operator + "'");
        }
    }

    private Function visitLogicalFunction(String operator, Node node, Object data) {
        if (node.jjtGetNumChildren() < 2) {
            throw new IllegalArgumentException("'" + operator + "' must have at least two parameters");
        }
        Function function = getLogicalFunction(operator);
        Expression result = visitChildWithType(function, node.jjtGetChild(node.jjtGetNumChildren() - 1), data, 1);
        for (int i = node.jjtGetNumChildren() - 2; i >= 0; i--) {
            function = getLogicalFunction(operator);
            Expression lhs = visitChildWithType(function, node.jjtGetChild(i), data, 0);
            function.setParameters(lhs, result);
            result = function;
        }

        return (Function) result;
    }

    @Override
    public Function visit(ASTNot node, Object data) {
        if (node.jjtGetNumChildren() != 1) {
            throw new IllegalArgumentException("'not' must have exactly one parameter");
        }
        return visit((ASTFunction) node, data);
    }

    @Override
    public Function visit(ASTBooleanFunction node, Object data) {
        return visit((ASTFunction) node, data);
    }

    @Override
    public Function visit(ASTComparison node, Object data) {
        if (node.jjtGetNumChildren() != 2) {
            throw new IllegalArgumentException("comparison must have exactly 2 children");
        }
        return visit((ASTFunction) node, data);
    }

    private Expression visit(Node node, Object data) {
        return (Expression) node.jjtAccept(this, data);
    }

    private Function getArithmeticFunction(String operator) {
        switch (operator) {
            case OP_ADD: {
                return new Add();
            }
            case OP_SUB: {
                return new Subtract();
            }
            case OP_MUL: {
                return new Multiply();
            }
            case OP_DIV: {
                return new Divide();
            }
            case OP_MOD: {
                return new Modulo();
            }
            default:
                throw new IllegalArgumentException("unknown operator '" + operator + "'");
        }
    }

    private Function visitArithmeticFunction(SimpleNode node, Object data) {
        int childCount = node.jjtGetNumChildren();
        if (childCount < 3 || childCount % 2 == 0) {
            throw new IllegalArgumentException("add/sub with wrong number of arguments");
        }
        // can be n-ary relation -> need to binaryfy
        // backwards iteration over children incl. visit(this) to handle expressions
        Expression rhs;
        Expression lhs;
        Function result = null;
        for (int i = 0; i < childCount; i++) {
            assert (i < childCount - 1);
            int operatorIndex = result == null ? i + 1 : i;
            if (!(node.jjtGetChild(operatorIndex) instanceof ASTOperator)) {
                throw new IllegalArgumentException("operator expected but '" + node.jjtGetChild(i).getClass().getName() + "' found");
            }
            String operator = ((ASTOperator) node.jjtGetChild(operatorIndex)).getName().trim().toLowerCase();
            Function function = getArithmeticFunction(operator);

            if (result == null) {
                lhs = visitChildWithType(function, node.jjtGetChild(i), data, 1);
                i++;
            } else {
                lhs = result;
            }
            i++;
            rhs = visitChildWithType(function, node.jjtGetChild(i), data, 0);
            function.setParameters(lhs, rhs);
            result = function;
        }
        return result;
    }

    @Override
    public Function visit(ASTPlusMin node, Object data) {
        return visitArithmeticFunction(node, data);
    }

    @Override
    public Function visit(ASTMulDiv node, Object data) {
        return visitArithmeticFunction(node, data);
    }

    private Function getFunction(String operator) {
        switch (operator) {
            /* comparison functions */
            case OP_NOT: {
                return new Not();
            }
            case OP_EQUAL: {
                return new Equal();
            }
            case OP_NOT_EQUAL: {
                return new NotEqual();
            }
            case OP_GREATER_THAN: {
                return new GreaterThan();
            }
            case OP_GREATER_EQUAL: {
                return new GreaterEqual();
            }
            case OP_LESS_THAN: {
                return new LessThan();
            }
            case OP_LESS_EQUAL: {
                return new LessEqual();
            }
            /* string functions */
            case OP_SUBSTRING_OF: {
                return new SubstringOf();
            }
            case OP_ENDS_WITH: {
                return new EndsWith();
            }
            case OP_STARTS_WITH: {
                return new StartsWith();
            }
            case OP_LENGTH: {
                return new Length();
            }
            case OP_INDEX_OF: {
                return new IndexOf();
            }
            case OP_SUBSTRING: {
                return new Substring();
            }
            case OP_TO_LOWER: {
                return new ToLower();
            }
            case OP_TO_UPPER: {
                return new ToUpper();
            }
            case OP_TRIM: {
                return new Trim();
            }
            case OP_CONCAT: {
                return new Concat();
            }
            case OP_YEAR: {
                return new Year();
            }
            case OP_MONTH: {
                return new Month();
            }
            case OP_DAY: {
                return new Day();
            }
            case OP_HOUR: {
                return new Hour();
            }
            case OP_MINUTE: {
                return new Minute();
            }
            case OP_SECOND: {
                return new Second();
            }
            case OP_FRACTIONAL_SECONDS: {
                return new FractionalSeconds();
            }
            case OP_DATE: {
                return new Date();
            }
            case OP_TIME: {
                return new Time();
            }
            case OP_TOTAL_OFFSET_MINUTES: {
                return new TotalOffsetMinutes();
            }
            case OP_NOW: {
                return new Now();
            }
            case OP_MIN_DATETIME: {
                return new MinDateTime();
            }
            case OP_MAX_DATETIME: {
                return new MaxDateTime();
            }
            case OP_BEFORE: {
                return new Before();
            }
            case OP_AFTER: {
                return new After();
            }
            case OP_MEETS: {
                return new Meets();
            }
            case OP_DURING: {
                return new During();
            }
            case OP_OVERLAPS: {
                return new Overlaps();
            }
            case OP_STARTS: {
                return new Starts();
            }
            case OP_FINISHES: {
                return new Finishes();
            }
            case OP_ROUND: {
                return new Round();
            }
            case OP_FLOOR: {
                return new Floor();
            }
            case OP_CEILING: {
                return new Ceiling();
            }
            case OP_GEO_DISTANCE: {
                return new GeoDistance();
            }
            case OP_GEO_LENGTH: {
                return new GeoLength();
            }
            case OP_GEO_INTERSECTS: {
                return new GeoIntersects();
            }
            case OP_ST_EQUALS: {
                return new STEquals();
            }
            case OP_ST_DISJOINT: {
                return new STDisjoint();
            }
            case OP_ST_TOUCHES: {
                return new STTouches();
            }
            case OP_ST_WITHIN: {
                return new STWithin();
            }
            case OP_ST_OVERLAPS: {
                return new STOverlaps();
            }
            case OP_ST_CROSSES: {
                return new STCrosses();
            }
            case OP_ST_INTERSECTS: {
                return new STIntersects();
            }
            case OP_ST_CONTAINS: {
                return new STContains();
            }
            case OP_ST_RELATE: {
                return new STRelate();
            }
            default:
                throw new IllegalArgumentException("unknown function '" + operator + "'");
        }
    }

    @Override
    public Function visit(ASTFunction node, Object data) {
        String operator = node.getName().trim().toLowerCase();
        Function function = getFunction(operator);
        function.setParameters(visitChildsWithType(function, node, data));
        return function;
    }

    private Expression[] visitChildsWithType(Function function, Node node, Object data) {
        List<FunctionTypeBinding> allowedBindings = function.getAllowedTypeBindings();
        if (data != null) {
            try {
                List<Class<? extends Constant>> allowedReturnTypes = (List<Class<? extends Constant>>) data;
                allowedBindings = allowedBindings.stream().filter(x -> allowedReturnTypes.contains(x.getReturnType())).collect(Collectors.toList());
            } catch (Exception e) {

            }
        }
        Expression[] parameters = new Expression[node.jjtGetNumChildren()];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = visit(node.jjtGetChild(i), allowedBindings.stream().map(x -> x.getParameters().get(0)).collect(Collectors.toList()));
        }
        return parameters;
    }

    private Expression visitChildWithType(Function function, Node child, Object data, int parameterIndex) {
        List<FunctionTypeBinding> allowedBindings = function.getAllowedTypeBindings();
        if (data != null) {
            try {
                List<Class<? extends Constant>> allowedReturnTypes = (List<Class<? extends Constant>>) data;
                allowedBindings = allowedBindings.stream().filter(x -> allowedReturnTypes.contains(x.getReturnType())).collect(Collectors.toList());
            } catch (Exception e) {

            }
        }
        return visit(child, allowedBindings.stream().map(x -> x.getParameters().get(parameterIndex)).collect(Collectors.toList()));
    }

    @Override
    public Constant visit(ASTValueNode node, Object data) {
        Object value = node.jjtGetValue();
        if (value instanceof Boolean) {
            return new BooleanConstant((Boolean) value);
        } else if (value instanceof Double) {
            return new DoubleConstant((Double) value);
        } else if (value instanceof Integer) {
            return new IntegerConstant((Integer) value);
        } else if (value instanceof Long) {
            return new IntegerConstant(((Long) value).intValue());
        } else if (value instanceof DateTime) {
            return new DateTimeConstant((DateTime) value);
        } else if (value instanceof LocalDate) {
            return new DateConstant((LocalDate) value);
        } else if (value instanceof LocalTime) {
            return new TimeConstant((LocalTime) value);
        } else if (value instanceof Period) {
            return new DurationConstant((Period) value);
        } else if (value instanceof Interval) {
            return new IntervalConstant((Interval) value);
        } else {
            return new StringConstant(node.jjtGetValue().toString());
        }
    }

    private static final String GEOGRAPHY_REGEX = "^geography\\s*'\\s*(.*)'$";
    private static final Pattern GEORAPHY_PATTERN = Pattern.compile(GEOGRAPHY_REGEX);

    @Override
    public GeoJsonConstant visit(ASTGeoStringLit node, Object data) {
        String raw = node.jjtGetValue().toString().trim();
        Matcher matcher = GEORAPHY_PATTERN.matcher(raw);
        if (matcher.matches()) {
            return GeoJsonConstant.fromString(matcher.group(1).trim());
        } else {
            throw new IllegalArgumentException("invalid geography string '" + raw + "'");
        }
    }

    @Override
    public BooleanConstant visit(ASTBool node, Object data) {
        return new BooleanConstant(node.getValue());
    }

}
