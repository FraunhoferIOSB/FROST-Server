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
package de.fraunhofer.iosb.ilt.sta.query.expression.function;

import de.fraunhofer.iosb.ilt.sta.query.expression.constant.Constant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class FunctionTypeBinding {

    private final List<Class<? extends Constant>> parameters;
    private final Class<? extends Constant> returnType;

    public FunctionTypeBinding(Class<? extends Constant> returnType, Class<? extends Constant>... parameters) {
        this.returnType = returnType;
        this.parameters = Arrays.asList(parameters);
    }

    public List<Class<? extends Constant>> getParameters() {
        return parameters;
    }

    public Class<? extends Constant> getReturnType() {
        return returnType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.parameters);
        hash = 67 * hash + Objects.hashCode(this.returnType);
        return hash;
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
        final FunctionTypeBinding other = (FunctionTypeBinding) obj;
        if (!Objects.equals(this.parameters, other.parameters)) {
            return false;
        }
        if (!Objects.equals(this.returnType, other.returnType)) {
            return false;
        }
        return true;
    }

}
