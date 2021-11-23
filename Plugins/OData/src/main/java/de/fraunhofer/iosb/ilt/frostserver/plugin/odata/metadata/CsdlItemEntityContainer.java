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
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsdlItemEntityContainer implements CsdlSchemaItem {

    @JsonProperty("$Kind")
    public String kind = "EntityContainer";

    @JsonAnyGetter
    @JsonAnySetter
    public Map<String, ContainerItem> properties = new LinkedHashMap<>();

    public CsdlItemEntityContainer generateFrom(String nameSpace, CoreSettings settings) {
        for (EntityType et : settings.getModelRegistry().getEntityTypes()) {
            properties.put(et.plural, new ContainerItem().generateFrom(nameSpace, settings, et));
        }
        return this;
    }

    @Override
    public void writeXml(String nameSpace, String name, Writer writer) throws IOException {
        writer.write("<EntityContainer Name=\"" + name + "\">");
        for (Map.Entry<String, ContainerItem> entry : properties.entrySet()) {
            String propName = entry.getKey();
            ContainerItem property = entry.getValue();
            property.writeXml(nameSpace, propName, writer);
        }

        writer.write("</EntityContainer>");
    }

    public static class ContainerItem {

        @JsonProperty("$Collection")
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public Boolean collection;

        @JsonProperty("$Type")
        public String type;

        @JsonProperty("$NavigationPropertyBinding")
        public Map<String, String> navPropBinding = new HashMap<>();

        public ContainerItem generateFrom(String nameSpace, CoreSettings settings, EntityType et) {
            collection = true;
            type = nameSpace + "." + et.entityName;
            for (NavigationPropertyMain np : et.getNavigationProperties()) {
                navPropBinding.put(np.getName(), np.getEntityType().plural);
            }
            return this;
        }

        public void writeXml(String nameSpace, String name, Writer writer) throws IOException {
            writer.write("<EntitySet Name=\"" + name + "\" EntityType=\"" + type + "\">");
            for (Map.Entry<String, String> entry : navPropBinding.entrySet()) {
                writer.write("<NavigationPropertyBinding Path=\"" + entry.getKey() + "\" Target=\"" + entry.getValue() + "\" />");
            }
            writer.write("</EntitySet>");
        }

    }

}
