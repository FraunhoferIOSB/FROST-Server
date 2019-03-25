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
package de.fraunhofer.iosb.ilt.frostserver.json.serialize;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;

/**
 * Naming strategy for EntityCollection class. Ensures that field names refering to entities begin with a capital
 * letter.
 *
 * @author jab
 */
public class EntitySetCamelCaseNamingStrategy extends PropertyNamingStrategy {

    UpperCamelCaseStrategy baseStrategy;

    public EntitySetCamelCaseNamingStrategy() {
        baseStrategy = new UpperCamelCaseStrategy();
    }

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return translate(defaultName, field.getRawType());
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translate(defaultName, method.getRawReturnType());
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translate(defaultName, method.getRawParameterType(0));
    }

    private String translate(String defaultName, Class<?> type) {
        if (NavigableElement.class.isAssignableFrom(type)) {
            return baseStrategy.translate(defaultName);
        }
        return defaultName;
    }
}
