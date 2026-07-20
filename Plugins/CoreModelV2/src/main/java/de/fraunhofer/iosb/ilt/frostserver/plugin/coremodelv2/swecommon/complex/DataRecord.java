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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * SWE Class DataRecord.
 */
public class DataRecord extends AbstractDataComponent<DataRecord, Map<String, Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRecord.class.getName());

    private List<AbstractDataComponent> fields;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.fields);
        hash = 29 * hash + super.hashCode();
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
        final DataRecord other = (DataRecord) obj;
        if (!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        return super.equals(obj);
    }

    public List<AbstractDataComponent> getFields() {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }

    /**
     * Alias for setFields, since some JSON has the fields in the field field.
     *
     * @param fields The fields to set on the DataRecord.
     * @return this.
     */
    public DataRecord setField(List<AbstractDataComponent> fields) {
        return setFields(fields);
    }

    public DataRecord setFields(List<AbstractDataComponent> fields) {
        this.fields = fields;
        return this;
    }

    public Optional<AbstractDataComponent> getFieldByName(String name) {
        return getFields().stream().filter(f -> f.getName().equals(name)).findFirst();
    }

    public DataRecord addDataComponent(String name, AbstractDataComponent field) {
        if (!name.equals(field.getName())) {
            field.setName(name);
        }
        return addDataComponent(field);
    }

    public DataRecord addDataComponent(AbstractDataComponent field) {
        String name = field.getName();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Field must have a non-empty name");
        }
        if (getFieldByName(name).isPresent()) {
            throw new IllegalArgumentException("Field with name " + name + " is already present");
        }
        getFields().add(field);
        return this;
    }

    @Override
    public Map<String, Object> getValue() {
        Map<String, Object> value = new LinkedHashMap<>();
        for (AbstractDataComponent f : fields) {
            value.put(f.getName(), f.getValue());
        }
        return value;
    }

    @Override
    public DataRecord setValue(Map<String, Object> value) {
        if (fields == null) {
            return this;
        }
        for (AbstractDataComponent f : fields) {
            Object fieldValue = value.get(f.getName());
            if (fieldValue != null) {
                f.setValue(fieldValue);
            }
        }
        return this;
    }

    @Override
    public boolean valueIsValid() {
        if (fields == null) {
            return true;
        }
        for (AbstractDataComponent f : fields) {
            if (!f.valueIsValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validate(Object input) {
        switch (input) {
            case Map mapValue -> {
                return validate(mapValue);
            }
            case List list -> {
                return validate(list);
            }
            case JsonNode jsonValue -> {
                return validate(jsonValue);
            }
            default -> {
                LOGGER.debug("Value is not a Map or JsonNode: {}", input);
                return false;
            }
        }
    }

    @Override
    public boolean validate(JsonNode input) {
        if (!input.isObject() && !input.isArray()) {
            LOGGER.debug("Value is not an Array or Object: {}", input);
            return false;
        }
        if (fields == null) {
            return true;
        }
        if (input.isObject()) {
            return validateObject(input);
        }
        return validateArray(input);
    }

    public boolean validateObject(JsonNode input) {
        for (AbstractDataComponent field : fields) {
            final String fieldName = field.getName();
            final JsonNode fieldValue = input.get(fieldName);
            if (fieldValue == null) {
                if (field.isOptional() || field.isSecret()) {
                    continue;
                } else {
                    LOGGER.debug("No value for non-optional field {}", fieldName);
                    return false;
                }
            }
            if (!field.validate(fieldValue)) {
                return false;
            }
        }
        return true;
    }

    public boolean validate(Object... input) {
        return validate(Arrays.asList(input));
    }

    public boolean validate(List<Object> input) {
        if (input.size() != fields.size()) {
            LOGGER.debug("Length of value list {} differs from fields array {}", input.size(), fields.size());
            return false;
        }
        for (int idx = 0; idx < fields.size(); idx++) {
            AbstractDataComponent field = fields.get(idx);
            Object fieldValue = input.get(idx);
            if (!field.validate(fieldValue)) {
                return false;
            }
        }
        return true;
    }

    public boolean validateArray(JsonNode input) {
        if (input.size() != fields.size()) {
            LOGGER.debug("Length of value array {} differs from fields array {}", input.size(), fields.size());
            return false;
        }
        for (int idx = 0; idx < fields.size(); idx++) {
            AbstractDataComponent field = fields.get(idx);
            JsonNode fieldValue = input.get(idx);
            if (!field.validate(fieldValue)) {
                return false;
            }
        }
        return true;
    }

    public boolean validate(Map<String, Object> input) {
        if (fields == null) {
            return true;
        }
        for (AbstractDataComponent field : fields) {
            final String fieldName = field.getName();
            final Object fieldValue = input.get(fieldName);
            if (fieldValue == null) {
                if (field.isOptional() || field.isSecret()) {
                    continue;
                } else {
                    LOGGER.debug("No value for non-optional field {}", fieldName);
                    return false;
                }
            }
            if (!field.validate(fieldValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected DataRecord self() {
        return this;
    }

    @Override
    public void gatherDefinitions(Set<String> target) {
        super.gatherDefinitions(target);
        for (AbstractDataComponent field : fields) {
            field.gatherDefinitions(target);
        }
    }

}
