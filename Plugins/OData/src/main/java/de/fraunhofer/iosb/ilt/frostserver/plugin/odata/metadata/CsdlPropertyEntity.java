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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
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
    public String type = TYPE_DEFAULT;

    @JsonProperty("$Nullable")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean nullable;

    @JsonProperty("$Collection")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean collection;

    @JsonIgnore
    private final List<CsdlAnnotation> annotations = new ArrayList<>();

    public <P> CsdlPropertyEntity generateFrom(CsdlDocument doc, CsdlDocument.ODataVersion version, EntityType et, EntityPropertyMain<P> ep) {
        final PropertyType pt = ep.getType();
        String namespace = pt.getNamespace();
        final String name = pt.getName();
        String fullName = ModelRegistry.fullName(namespace, name);
        type = fullName;
        collection = pt.isCollection();
        if (TYPE_DEFAULT.equals(type)) {
            type = null;
        }
        if (TypeSimplePrimitive.EDM_UNTYPED == pt && version == CsdlDocument.ODataVersion.V4_0) {
            type = TypeSimplePrimitive.EDM_STRING.getName();
        }
        if (!et.getPrimaryKey().getKeyProperties().contains(ep)) {
            nullable = ep.isNullable();
        }
        if (collection) {
            nullable = false;
        }
        for (Annotation an : ep.getAnnotations()) {
            annotations.add(new CsdlAnnotation().generateFrom(doc, an));
        }
        return this;
    }

    public CsdlPropertyEntity generateFrom(CsdlDocument doc, PropertyType pt, boolean nullable) {
        String namespace = pt.getNamespace();
        final String name = pt.getName();
        String fullName = ModelRegistry.fullName(namespace, name);
        type = fullName;
        if (TYPE_DEFAULT.equals(type)) {
            type = null;
        }
        this.nullable = nullable;
        for (Annotation an : pt.getAnnotations()) {
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
    public void writeXml(String name, Writer writer) throws IOException {
        String typeString = type == null ? TYPE_DEFAULT : type;
        if (collection) {
            typeString = "Collection(" + typeString + ")";
        }
        String nullableString = (nullable) ? "" : " Nullable=\"" + Boolean.toString(nullable) + "\"";
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
