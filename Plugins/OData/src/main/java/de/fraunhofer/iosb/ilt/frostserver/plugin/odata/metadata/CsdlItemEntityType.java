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
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CsdlItemEntityType implements CsdlSchemaItem {

    @JsonProperty("$Kind")
    public String kind = "EntityType";

    @JsonProperty("$Key")
    public List<String> key = new ArrayList<>();

    @JsonIgnore
    public Map<String, CsdlProperty> properties = new LinkedHashMap<>();
    @JsonIgnore
    private final List<CsdlAnnotation> annotations = new ArrayList<>();

    public CsdlItemEntityType generateFrom(CsdlDocument doc, Version version, String nameSpace, EntityType et) {
        String keyName = et.getPrimaryKey().name;
        if ("@iot.id".equals(keyName)) {
            keyName = "id";
        }
        key.add(keyName);

        for (EntityPropertyMain ep : et.getEntityProperties()) {
            if (ep == ModelRegistry.EP_SELFLINK) {
                continue;
            }
            String propertyName = ep.getJsonName();
            if ("@iot.id".equals(propertyName)) {
                propertyName = "id";
            }
            properties.put(propertyName, new CsdlPropertyEntity().generateFrom(doc, version, nameSpace, et, ep));
        }
        for (NavigationPropertyMain np : et.getNavigationProperties()) {
            properties.put(np.getJsonName(), new CsdlPropertyNavigation().generateFrom(doc, nameSpace, et, np));
        }
        for (Annotation an : et.getAnnotations()) {
            annotations.add(new CsdlAnnotation().generateFrom(doc, an));
        }

        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> otherProperties() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Entry<String, CsdlProperty> entry : properties.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        for (CsdlAnnotation annotation : annotations) {
            result.put('@' + annotation.getQualifiedName(), annotation.getValue());
        }
        return result;
    }

    @Override
    public void writeXml(String nameSpace, String name, Writer writer) throws IOException {
        writer.write("<EntityType Name=\"" + name + "\">");
        writer.write("<Key>");
        for (String keyName : key) {
            writer.write("<PropertyRef Name=\"" + keyName + "\" />");
        }
        writer.write("</Key>");
        for (Entry<String, CsdlProperty> entry : properties.entrySet()) {
            String propName = entry.getKey();
            CsdlProperty property = entry.getValue();
            property.writeXml(nameSpace, propName, writer);
        }
        for (CsdlAnnotation an : annotations) {
            an.writeXml(writer);
        }
        writer.write("</EntityType>");
    }

}
