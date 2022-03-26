/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import net.time4j.Moment;
import org.jooq.Field;

/**
 *
 * @author hylke
 */
public class WrapperHelper {

    private WrapperHelper() {
        // Utility class.
    }

    public static FieldWrapper wrapField(Field field) {
        Class<?> fieldType = field.getType();
        if (Moment.class.isAssignableFrom(fieldType)) {
            return new StaDateTimeWrapper(field);
        }
        if (JsonValue.class.isAssignableFrom(fieldType)) {
            return new JsonFieldFactory.JsonFieldWrapper(field);
        }
        return new SimpleFieldWrapper(field);
    }
}
