/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsdlPropertyEntity implements CsdlProperty {

    private static final String TYPE_DEFAULT = "Edm.String";

    @JsonProperty("$Type")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String type;

    @JsonProperty("$Nullable")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean nullable;

    @JsonProperty("$Collection")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean collection;

    @JsonIgnore
    private final List<CsdlAnnotation> annotations = new ArrayList<>();

    public CsdlPropertyEntity generateFrom(CsdlDocument doc, Version version, String nameSpace, EntityType et, EntityPropertyMain<?> ep) {
        final PropertyType propertyType = ep.getType();
        type = propertyType.getName();
        collection = propertyType.isCollection();
        if (!type.startsWith("Edm.")) {
            type = nameSpace + "." + type;
        }
        if (TYPE_DEFAULT.equals(type)) {
            type = null;
        }
        if (TypeSimplePrimitive.EDM_UNTYPED == propertyType && version == PluginOData.VERSION_ODATA_40) {
            type = TypeSimplePrimitive.EDM_STRING.getName();
        }
        if (et.getPrimaryKey() != ep && !et.isRequired(ep)) {
            nullable = true;
        }
        for (Annotation an : ep.getAnnotations()) {
            annotations.add(new CsdlAnnotation().generateFrom(doc, an));
        }
        return this;
    }

    public CsdlPropertyEntity generateFrom(CsdlDocument doc, String nameSpace, PropertyType value, boolean nullable) {
        type = value.getName();
        if (!type.startsWith("Edm.")) {
            type = nameSpace + "." + type;
        }
        if (TYPE_DEFAULT.equals(type)) {
            type = null;
        }
        this.nullable = nullable;
        for (Annotation an : value.getAnnotations()) {
            annotations.add(new CsdlAnnotation().generateFrom(doc, an));
        }
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> otherProperties() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (CsdlAnnotation annotation : annotations) {
            result.put('@' + annotation.getQualifiedName(), annotation.getValue());
        }
        return result;
    }

    @Override
    public void writeXml(String nameSpace, String name, Writer writer) throws IOException {
        String typeString = type == null ? TYPE_DEFAULT : type;
        if (collection) {
            typeString = "Collection(" + typeString + ")";
        }
        String nullableString = (nullable) ? " Nullable=\"" + Boolean.toString(nullable) + "\"" : "";
        writer.write("<Property Name=\"" + name + "\" Type=\"" + typeString + "\"" + nullableString);
        if (annotations.isEmpty()) {
            writer.write(" />");
        } else {
            writer.write(">");
            for (CsdlAnnotation an : annotations) {
                an.writeXml(writer);
            }
            writer.write("</Property>");
        }
    }

}
