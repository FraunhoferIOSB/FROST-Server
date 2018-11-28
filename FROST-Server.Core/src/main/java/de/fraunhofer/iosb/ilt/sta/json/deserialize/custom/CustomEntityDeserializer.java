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
package de.fraunhofer.iosb.ilt.sta.json.deserialize.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import de.fraunhofer.iosb.ilt.sta.json.serialize.custom.CustomSerialization;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author jab
 * @param <T> The type of the entity to deserialize.
 */
public class CustomEntityDeserializer<T extends Entity> extends JsonDeserializer<Entity> {

    private final Class<? extends Entity> clazz;

    public CustomEntityDeserializer(Class<? extends Entity> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        Entity result;
        try {
            result = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IOException("Error deserializing JSON!");
        }
        // need to make subclass of this class for every Entity subclass with custom field to get expected class!!!
        BeanDescription beanDescription = ctxt.getConfig().introspect(ctxt.constructType(clazz));
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode obj = mapper.readTree(parser);
        List<BeanPropertyDefinition> properties = beanDescription.findProperties();
        Iterator<Map.Entry<String, JsonNode>> i = obj.fields();

        // First check if we know all properties that are present.
        if (ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            while (i.hasNext()) {
                Map.Entry<String, JsonNode> next = i.next();
                String fieldName = next.getKey();
                Optional<BeanPropertyDefinition> findFirst = properties.stream().filter(p -> p.getName().equals(fieldName)).findFirst();
                if (!findFirst.isPresent()) {
                    throw new UnrecognizedPropertyException(parser, "Unknown field: " + fieldName, parser.getCurrentLocation(), clazz, fieldName, null);
                }
            }
        }

        for (BeanPropertyDefinition classProperty : properties) {
            deserialiseProperty(obj, classProperty, properties, mapper, result);
        }
        return (T) result;
    }

    private void deserialiseProperty(JsonNode obj, BeanPropertyDefinition classProperty, List<BeanPropertyDefinition> properties, ObjectMapper mapper, Entity result) throws IOException {
        if (obj.has(classProperty.getName())) {
            // property is present in class and json
            Annotation annotation = classProperty.getAccessor().getAnnotation(CustomSerialization.class);
            if (annotation == null) {
                Object value = mapper.convertValue(
                        obj.get(classProperty.getName()),
                        classProperty.getField().getType());
                classProperty.getMutator().setValue(result, value);
            } else {
                // property has custom annotation
                // check if encoding property is also present in json (and also in class itself for sanity reasons)
                CustomSerialization customAnnotation = (CustomSerialization) annotation;
                Optional<BeanPropertyDefinition> encodingClassProperty = properties.stream().filter(p -> p.getName().equals(customAnnotation.encoding())).findFirst();
                if (!encodingClassProperty.isPresent()) {
                    throw new IOException("Error deserializing JSON as class '" + clazz.toString() + "' \n"
                            + "Reason: field '" + customAnnotation.encoding() + "' specified by annotation as encoding field is not defined in class!");
                }
                String customEncoding = null;
                if (obj.has(customAnnotation.encoding())) {
                    customEncoding = obj.get(customAnnotation.encoding()).asText();
                }
                Object customDeserializedValue = CustomDeserializationManager.getInstance()
                        .getDeserializer(customEncoding)
                        .deserialize(mapper.writeValueAsString(obj.get(classProperty.getName())));
                classProperty.getMutator().setValue(result, customDeserializedValue);
            }
        }
    }

}
