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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ParserContext;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.PropertyReference;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;
import java.util.List;
import java.util.Objects;

/**
 * The any Function.
 */
public class Any extends Function<Any> {

    private Path collection;
    private String lambdaName;

    public Any() {
        super("any");
    }

    public Any(Path collection, String lambdaName) {
        this.collection = collection;
        this.lambdaName = lambdaName;
    }

    public Any(Path collection, String lambdaName, Expression filter) {
        this();
        addParameters(filter);
        this.collection = collection;
        this.lambdaName = lambdaName;
    }

    @Override
    protected void initAllowedTypeBindings() {
        addAllowedTypeBinding(new FunctionTypeBinding(BooleanConstant.class, BooleanConstant.class));
    }

    @Override
    public void validate(ParserContext parentContext, EntityType type) {
        ParserContext context = new ParserContext(parentContext);
        collection.validate(context, type);
        Property lastElement = collection.lastElement();
        if (lastElement instanceof NavigationPropertyEntitySet npes) {
            EntityType anyType = npes.getEntityType();
            context.registerVariable(lambdaName, new PropertyReference<>(lambdaName, npes));
            super.validate(context, anyType);
        } else {
            throw new IllegalArgumentException("Any can only be used on an EntitySet, " + lastElement + " is not an EntitySet.");
        }
    }

    public String getLambdaName() {
        return lambdaName;
    }

    public void setCollection(Path collection) {
        this.collection = collection;
    }

    public Path getCollection() {
        return collection;
    }

    @Override
    public String toUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(collection.toUrl());
        sb.append("/any(");
        sb.append(lambdaName);
        sb.append(':');
        boolean first = true;
        List<Expression<?>> parameters = getParameters();
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

    @Override
    public String toString() {
        return toUrl();
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
        final Any other = (Any) obj;
        if (!Objects.equals(this.lambdaName, other.lambdaName)) {
            return false;
        }
        if (!Objects.equals(this.collection, other.collection)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.collection);
        hash = 23 * hash + Objects.hashCode(this.lambdaName);
        return 23 * hash + super.hashCode();
    }

    @Override
    public Any newInstance() {
        return new Any()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }
}
