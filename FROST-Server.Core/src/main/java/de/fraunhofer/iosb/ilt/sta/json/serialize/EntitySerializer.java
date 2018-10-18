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
package de.fraunhofer.iosb.ilt.sta.json.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import static com.fasterxml.jackson.databind.ser.BeanPropertyWriter.MARKER_FOR_EMPTY;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import de.fraunhofer.iosb.ilt.sta.json.serialize.custom.CustomSerialization;
import de.fraunhofer.iosb.ilt.sta.json.serialize.custom.CustomSerializationManager;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.NavigableElement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles serialization of Entity objects. If a field is of type Entity and
 * contains a non-empty navigationLink the field will be renamed with the suffix
 * '@iot.navigationLink' and will only contain the navigationLink as String.
 *
 * @author jab
 */
public class EntitySerializer extends JsonSerializer<Entity> {

    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntitySerializer.class);

    @Override
    public void serialize(Entity entity, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        try {
            BasicBeanDescription beanDescription = serializers.getConfig().introspect(serializers.constructType(entity.getClass()));
            List<BeanPropertyDefinition> properties = beanDescription.findProperties();
            Set<String> selectedProperties = entity.getSelectedPropertyNames();
            for (BeanPropertyDefinition property : properties) {
                // 0. check if it should be serialized
                // If not, we still have to check if it is expanded, hence no
                // direct continue.
                boolean selected = true;
                if (selectedProperties != null) {
                    if (!selectedProperties.contains(property.getName())) {
                        selected = false;
                    }
                }
                // 1. is it a NavigableElement?
                if (NavigableElement.class.isAssignableFrom(property.getAccessor().getRawType())) {
                    Object rawValue = property.getAccessor().getValue(entity);
                    if (rawValue != null) {
                        NavigableElement value = (NavigableElement) rawValue;
                        // If navigation link set, and selected, output navigation link.
                        if (selected && value.getNavigationLink() != null && !value.getNavigationLink().isEmpty()) {
                            gen.writeFieldName(property.getName() + "@iot.navigationLink");
                            gen.writeString(value.getNavigationLink());
                        }
                        // If object should not be exported, skip any further processing.
                        selected = value.isExportObject();
                    }
                }
                if (!selected) {
                    continue;
                }

                // 2. check if property has CustomSerialization annotation -> use custom serializer
                Annotation annotation = property.getAccessor().getAnnotation(CustomSerialization.class);
                if (annotation != null) {
                    serializeFieldCustomized(
                            entity,
                            gen,
                            property,
                            properties,
                            (CustomSerialization) annotation);
                } else {
                    serializeField(entity, gen, serializers, beanDescription, property);
                }
                // 3. check if property is EntitySet than write count if needed.
                if (EntitySet.class.isAssignableFrom(property.getAccessor().getRawType())) {
                    Object rawValue = property.getAccessor().getValue(entity);
                    if (rawValue != null) {
                        EntitySet set = (EntitySet) rawValue;
                        long count = set.getCount();
                        if (count >= 0) {
                            gen.writeNumberField(property.getName() + "@iot.count", count);
                        }
                        String nextLink = set.getNextLink();
                        if (nextLink != null) {
                            gen.writeStringField(property.getName() + "@iot.nextLink", nextLink);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("could not serialize Entity", e);
            throw new IOException("could not serialize Entity", e);
        } finally {
            gen.writeEndObject();
        }
    }

    protected void serializeFieldCustomized(
            Entity entity,
            JsonGenerator gen,
            BeanPropertyDefinition property,
            List<BeanPropertyDefinition> properties,
            CustomSerialization annotation) throws Exception {
        // check if encoding field is present in current bean
        // get value
        // call CustomSerializationManager
        Optional<BeanPropertyDefinition> encodingProperty = properties.stream().filter(p -> p.getName().equals(annotation.encoding())).findFirst();
        if (!encodingProperty.isPresent()) {
            throw new JsonGenerationException("can not serialize instance of class '" + entity.getClass() + "'! \n"
                    + "Reason: trying to use custom serialization for field '" + property.getName() + "' but field '" + annotation.encoding() + "' specifying enconding is not present!",
                    gen);
        }
        Object value = encodingProperty.get().getAccessor().getValue(entity);
        String encodingType = null;
        if (value != null) {
            encodingType = value.toString();
        }
        String customJson = CustomSerializationManager.getInstance()
                .getSerializer(encodingType)
                .serialize(property.getAccessor().getValue(entity));
        if (customJson != null && !customJson.isEmpty()) {
            gen.writeFieldName(property.getName());
            gen.writeRawValue(customJson);
        }
    }

    protected void serializeField(
            Entity entity,
            JsonGenerator gen,
            SerializerProvider serializers,
            BeanDescription beanDescription,
            BeanPropertyDefinition beanPropertyDefinition) throws Exception {
        serializeFieldTyped(entity, gen, serializers, beanDescription, beanPropertyDefinition, null);
    }

    protected void serializeFieldTyped(
            Entity entity,
            JsonGenerator gen,
            SerializerProvider serializers,
            BeanDescription beanDescription,
            BeanPropertyDefinition beanPropertyDefinition,
            TypeSerializer typeSerializer) throws Exception {
        try {
            if (typeSerializer == null) {
                typeSerializer = serializers.findTypeSerializer(serializers.constructType(beanPropertyDefinition.getAccessor().getRawType()));
            }
            if (typeSerializer == null) {
                // if not static type if available use dynamic type if available
                Object propertyValue = beanPropertyDefinition.getAccessor().getValue(entity);
                if (propertyValue != null) {
                    typeSerializer = serializers.findTypeSerializer(serializers.constructType(propertyValue.getClass()));
                }
            }

            JsonInclude.Value inclusion = beanPropertyDefinition.findInclusion();
            JsonInclude.Value defaultInclusion = serializers.getConfig().getDefaultPropertyInclusion();
            JsonInclude.Value usedInclusion = defaultInclusion.withOverrides(inclusion);
            BeanPropertyWriter bpw = new BeanPropertyWriter(
                    beanPropertyDefinition,
                    beanPropertyDefinition.getAccessor(),
                    beanDescription.getClassAnnotations(),
                    beanPropertyDefinition.getAccessor().getType(),
                    null, // will be searched automatically
                    typeSerializer, // will not be searched automatically
                    beanPropertyDefinition.getAccessor().getType(),
                    suppressNulls(usedInclusion),
                    suppressableValue(serializers.getConfig().getDefaultPropertyInclusion()));
            if (!bpw.willSuppressNulls()) {
                bpw.assignNullSerializer(NullSerializer.instance);
            }
            bpw.serializeAsField(entity, gen, serializers);
        } catch (JsonMappingException ex) {
            Logger.getLogger(EntitySerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected static boolean suppressNulls(JsonInclude.Value inclusion) {
        if (inclusion == null) {
            return false;
        }
        JsonInclude.Include incl = inclusion.getValueInclusion();
        return (incl != JsonInclude.Include.ALWAYS) && (incl != JsonInclude.Include.USE_DEFAULTS);
    }

    protected static Object suppressableValue(JsonInclude.Value inclusion) {
        if (inclusion == null) {
            return false;
        }
        JsonInclude.Include incl = inclusion.getValueInclusion();
        if ((incl == JsonInclude.Include.ALWAYS)
                || (incl == JsonInclude.Include.NON_NULL)
                || (incl == JsonInclude.Include.USE_DEFAULTS)) {
            return null;
        }
        return MARKER_FOR_EMPTY;
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Entity value) {
        if (value == null) {
            return true;
        }
        Entity emptyInstance;
        try {
            emptyInstance = value.getClass().newInstance();
            return emptyInstance.equals(value);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(EntitySerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
