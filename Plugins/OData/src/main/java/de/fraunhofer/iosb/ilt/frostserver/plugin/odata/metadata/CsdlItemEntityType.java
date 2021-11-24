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
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsdlItemEntityType implements CsdlSchemaItem {

    @JsonProperty("$Kind")
    public String kind = "EntityType";

    @JsonProperty("$Key")
    public List<String> key = new ArrayList<>();

    @JsonAnyGetter
    @JsonAnySetter
    public Map<String, CsdlProperty> properties = new LinkedHashMap<>();

    public CsdlItemEntityType generateFrom(String nameSpace, CoreSettings settings, EntityType et) {
        ModelRegistry mr = settings.getModelRegistry();
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
            properties.put(propertyName, new CsdlPropertyEntity().generateFrom(nameSpace, settings, et, ep));
        }
        for (NavigationPropertyMain np : et.getNavigationProperties()) {
            properties.put(np.getJsonName(), new CsdlPropertyNavigation().generateFrom(nameSpace, settings, et, np));
        }

        return this;
    }

    @Override
    public void writeXml(String nameSpace, String name, Writer writer) throws IOException {
        writer.write("<EntityType Name=\"" + name + "\">");
        writer.write("<Key>");
        for (String keyName : key) {
            writer.write("<PropertyRef Name=\"" + keyName + "\" />");
        }
        writer.write("</Key>");
        for (Map.Entry<String, CsdlProperty> entry : properties.entrySet()) {
            String propName = entry.getKey();
            CsdlProperty property = entry.getValue();
            property.writeXml(nameSpace, propName, writer);
        }
        writer.write("</EntityType>");
    }

}
