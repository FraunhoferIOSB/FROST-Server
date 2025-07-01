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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.complex;

import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.AbstractDataComponent;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * SWE Class DataArray.
 */
public class DataArray extends AbstractDataComponent<DataArray, List<Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataArray.class.getName());

    private AbstractDataComponent elementType;
    private List<Object> values;

    public AbstractDataComponent getElementType() {
        return elementType;
    }

    public DataArray setElementType(AbstractDataComponent elementType) {
        this.elementType = elementType;
        return self();
    }

    @Override
    public List<Object> getValue() {
        return values;
    }

    @Override
    protected DataArray self() {
        return this;
    }

    @Override
    public DataArray setValue(List<Object> value) {
        this.values = value;
        return self();
    }

    @Override
    public boolean validate(Object input) {
        if (input instanceof List list) {
            return validate(list);
        } else if (input instanceof JsonNode jn) {
            return validate(jn);
        } else {
            LOGGER.error("Input is not a List or JsonArray");
            return false;
        }
    }

    public boolean validate(List input) {
        if (elementType == null) {
            LOGGER.error("ElementType is not set.");
            return false;
        }
        for (var item : input) {
            if (!elementType.validate(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validate(JsonNode input) {
        if (input.isArray()) {
            for (var item : input.values()) {
                if (!elementType.validate(item)) {
                    return false;
                }
            }
            return true;
        } else {
            LOGGER.error("Input is not a JsonArray");
            return false;
        }
    }

    @Override
    public boolean valueIsValid() {
        return validate(values);
    }

    @Override
    public void gatherDefinitions(Set<String> target) {
        super.gatherDefinitions(target);
        elementType.gatherDefinitions(target);
    }

}
