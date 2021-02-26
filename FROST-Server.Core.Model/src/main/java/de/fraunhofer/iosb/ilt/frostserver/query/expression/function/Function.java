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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.Constant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.LineStringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PointConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PolygonConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a function of the API which can be used in a query.
 *
 * @author jab
 */
public abstract class Function implements Expression {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Function.class);

    protected List<Expression> parameters;
    protected List<FunctionTypeBinding> allowedTypeBindings;
    private final String functionName;

    protected Function() {
        allowedTypeBindings = new ArrayList();
        functionName = getClass().getSimpleName().toLowerCase();
    }

    protected Function(Expression... parameters) {
        this.parameters = Arrays.asList(parameters);
        allowedTypeBindings = new ArrayList();
        functionName = getClass().getSimpleName().toLowerCase();
    }

    protected Function(String functionName) {
        this.functionName = functionName;
        allowedTypeBindings = new ArrayList();
    }

    protected Function(String functionName, Expression... parameters) {
        this.functionName = functionName;
        this.parameters = Arrays.asList(parameters);
        allowedTypeBindings = new ArrayList();
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public String toUrl() {
        StringBuilder sb = new StringBuilder(getFunctionName());
        sb.append("(");
        boolean first = true;
        for (Expression p : parameters) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(p.toUrl());
        }
        sb.append(")");
        return sb.toString();
    }

    public void setParameters(List<Expression> parameters) {
        this.parameters = parameters;
    }

    public void setParameters(Expression... parameters) {
        this.parameters = Arrays.asList(parameters);
    }

    public List<Expression> getParameters() {
        return parameters;
    }

    /**
     * Compresses the expression as far as possible by recursively calling
     * compress on the parameters.
     *
     * @return A subclass of Constant if all parameters could be materialized
     * otherwise a maximally compressed Expr.
     */
    @Override
    public final Expression compress() {
        return eval(
                parameters
                        .stream()
                        .map(Expression::compress)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Searches in dynamic type for a method with name 'eval' and suitable
     * parameters and then invokes it. If there is no suitable method or calling
     * the method fails, 'this' is returned.
     *
     * @param parameters Parameters to invoke the function on
     * @return The result of the evaluation. Should be subclass of Constant for
     * executable functions, otherwhise this.
     */
    private Expression eval(List<Expression> parameters) {
        try {
            // getDeclaredMethod not working with inheritance, must find suited method myself
            Method method = findMethod(parameters);
            if (method != null) {
                return (Expression) method.invoke(this, parameters.toArray());
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.info("Could not eval.", ex);
        }
        return this;
    }

    private Method findMethod(List<Expression> parameters) {
        Method[] methods = getClass().getDeclaredMethods();
        List<Method> suitableMethods = new ArrayList<>();
        for (Method method : methods) {
            if (!method.getName().equals("eval") || parameters.size() != method.getParameterCount()) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            boolean assignable = true;
            for (int i = 0; i < parameters.size(); i++) {
                if (!parameterTypes[i].isAssignableFrom(parameters.get(i).getClass())) {
                    assignable = false;
                    break;
                }
            }
            if (assignable) {
                suitableMethods.add(method);
            }
        }
        // we need to find the most specific method. This is done argument by argument but not now
        if (!suitableMethods.isEmpty()) {
            return suitableMethods.get(0);
        }
        return null;
    }

    protected abstract void initAllowedTypeBindings();

    public List<FunctionTypeBinding> getAllowedTypeBindings() {
        if (allowedTypeBindings == null || allowedTypeBindings.isEmpty()) {
            initAllowedTypeBindings();
        }
        return allowedTypeBindings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, allowedTypeBindings);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Function other = (Function) obj;
        return Objects.equals(parameters, other.parameters)
                && Objects.equals(getAllowedTypeBindings(), other.getAllowedTypeBindings());
    }

    protected static List<FunctionTypeBinding> getTypeBindingForAllTypes() {
        List<FunctionTypeBinding> result = new ArrayList<>();
        result.add(new FunctionTypeBinding(BooleanConstant.class, BooleanConstant.class, BooleanConstant.class));
        result.add(new FunctionTypeBinding(DateConstant.class, DateConstant.class, DateConstant.class));
        result.add(new FunctionTypeBinding(DateTimeConstant.class, DateTimeConstant.class, DateTimeConstant.class));
        result.add(new FunctionTypeBinding(DoubleConstant.class, DoubleConstant.class, DoubleConstant.class));
        result.add(new FunctionTypeBinding(DurationConstant.class, DurationConstant.class, DurationConstant.class));
        result.add(new FunctionTypeBinding(IntegerConstant.class, IntegerConstant.class, IntegerConstant.class));
        result.add(new FunctionTypeBinding(LineStringConstant.class, LineStringConstant.class, LineStringConstant.class));
        result.add(new FunctionTypeBinding(PointConstant.class, PointConstant.class, PointConstant.class));
        result.add(new FunctionTypeBinding(PolygonConstant.class, PolygonConstant.class, PolygonConstant.class));
        result.add(new FunctionTypeBinding(StringConstant.class, StringConstant.class, StringConstant.class));
        result.add(new FunctionTypeBinding(IntegerConstant.class, IntegerConstant.class, IntegerConstant.class));
        return result;
    }

    protected static List<FunctionTypeBinding> getTypeBindingForAllTypesWithReturnType(Class<? extends Constant> returnType) {
        List<FunctionTypeBinding> result = new ArrayList<>();
        result.add(new FunctionTypeBinding(BooleanConstant.class, BooleanConstant.class, returnType));
        result.add(new FunctionTypeBinding(DateConstant.class, DateConstant.class, returnType));
        result.add(new FunctionTypeBinding(DateTimeConstant.class, DateTimeConstant.class, returnType));
        result.add(new FunctionTypeBinding(DoubleConstant.class, DoubleConstant.class, returnType));
        result.add(new FunctionTypeBinding(DurationConstant.class, DurationConstant.class, returnType));
        result.add(new FunctionTypeBinding(IntegerConstant.class, IntegerConstant.class, returnType));
        result.add(new FunctionTypeBinding(LineStringConstant.class, LineStringConstant.class, returnType));
        result.add(new FunctionTypeBinding(PointConstant.class, PointConstant.class, returnType));
        result.add(new FunctionTypeBinding(PolygonConstant.class, PolygonConstant.class, returnType));
        result.add(new FunctionTypeBinding(StringConstant.class, StringConstant.class, returnType));
        result.add(new FunctionTypeBinding(IntegerConstant.class, IntegerConstant.class, returnType));
        return result;
    }
}
