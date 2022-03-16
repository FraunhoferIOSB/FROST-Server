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
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsdlPropertyNavigation implements CsdlProperty {

    @JsonProperty("$Kind")
    public String kind = "NavigationProperty";

    @JsonProperty("$Collection")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public Boolean collection;

    @JsonProperty("$Partner")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String partner;

    @JsonProperty("$Type")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String type;

    @JsonProperty("$Nullable")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public Boolean nullable;

    @JsonIgnore
    private final List<CsdlAnnotation> annotations = new ArrayList<>();

    public CsdlPropertyNavigation generateFrom(CsdlDocument doc, String nameSpace, EntityType et, NavigationPropertyMain<?> np) {
        type = nameSpace + "." + np.getType().getName();
        final NavigationPropertyMain inverse = np.getInverse();
        if (inverse != null) {
            partner = inverse.getName();
        }
        if (np.isEntitySet()) {
            collection = true;
        }
        if (et.isRequired(np)) {
            nullable = false;
        }
        for (Annotation an : np.getAnnotations()) {
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
        String finalType = type;
        if (collection != null && collection) {
            finalType = "Collection(" + type + ")";
        }
        String nullableString = "";
        if (nullable != null && nullable) {
            nullableString = " Nullable=\"" + Boolean.toString(nullable) + "\"";
        }
        String partnerString = "";
        if (!StringHelper.isNullOrEmpty(partner)) {
            partnerString = " Partner=\"" + partner + "\"";
        }
        writer.write("<NavigationProperty Name=\"" + name + "\" Type=\"" + finalType + "\"" + nullableString + partnerString);
        if (annotations.isEmpty()) {
            writer.write(" />");
        } else {
            writer.write(">");
            for (CsdlAnnotation an : annotations) {
                an.writeXml(writer);
            }
            writer.write("</NavigationProperty>");
        }
    }

}
