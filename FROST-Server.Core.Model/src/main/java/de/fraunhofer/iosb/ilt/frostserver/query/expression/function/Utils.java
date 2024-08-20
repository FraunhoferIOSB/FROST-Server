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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import java.util.List;

/**
 *
 * @author scf
 */
public class Utils {

    private Utils() {
        // Utility class, not to be instantiated.
    }

    public static void allowTypeBindingsCommonNumbers(List<FunctionTypeBinding> allowedTypeBindings) {
        allowedTypeBindings.add(new FunctionTypeBinding(IntegerConstant.class, IntegerConstant.class, IntegerConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(DoubleConstant.class, DoubleConstant.class, DoubleConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(DoubleConstant.class, IntegerConstant.class, DoubleConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(DoubleConstant.class, DoubleConstant.class, IntegerConstant.class));
    }

}
