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
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeEnumeration;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsdlItemEnumType implements CsdlSchemaItem {

    @JsonProperty("$Kind")
    public String kind = "EnumType";

    @JsonProperty("@Core.Description")
    public String description;

    @JsonAnyGetter
    @JsonAnySetter
    public Map<String, Number> values = new LinkedHashMap<>();

    public CsdlItemEnumType generateFrom(String nameSpace, CoreSettings settings, TypeEnumeration<?> te) {
        description = te.getDescription();
        for (Map.Entry<String, Number> entry : te.getValues().entrySet()) {
            final String name = entry.getKey();
            final Number value = entry.getValue();
            values.put(name, value);
        }
        return this;
    }

    @Override
    public void writeXml(String nameSpace, String name, Writer writer) throws IOException {
        writer.write("<EnumType Name=\"" + name + "\" UnderlyingType=\"Edm.Int32\">");
        for (Map.Entry<String, Number> entry : values.entrySet()) {
            String memberName = entry.getKey();
            Number memberValue = entry.getValue();
            writer.write("<Member Name=\"" + memberName + "\"   Value=\"" + memberValue + "\" />");
        }
        writer.write("</EnumType>");
    }

}
