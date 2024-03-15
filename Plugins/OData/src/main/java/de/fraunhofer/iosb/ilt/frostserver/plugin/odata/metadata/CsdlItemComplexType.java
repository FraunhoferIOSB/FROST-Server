/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CsdlItemComplexType implements CsdlSchemaItem {

    @JsonProperty("$Kind")
    public String kind = "ComplexType";

    @JsonProperty("$OpenType")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean openType;

    @JsonProperty("@Core.Description")
    public String description;

    @JsonAnyGetter
    @JsonAnySetter
    public Map<String, CsdlProperty> properties = new LinkedHashMap<>();

    public CsdlItemComplexType generateFrom(CsdlDocument doc, String nameSpace, TypeComplex tc) {
        description = tc.getDescription();
        openType = tc.isOpenType();
        for (Entry<String, Property> entry : tc.getPropertiesByName().entrySet()) {
            final String name = entry.getKey();
            final Property prop = entry.getValue();
            final boolean nullable = prop.isNullable();
            properties.put(name, new CsdlPropertyEntity().generateFrom(doc, nameSpace, prop.getType(), nullable));
        }
        return this;
    }

    @Override
    public void writeXml(String nameSpace, String name, Writer writer) throws IOException {
        writer.write("<ComplexType Name=\"" + name + "\" OpenType=\"" + Boolean.toString(openType) + "\">");
        for (Entry<String, CsdlProperty> entry : properties.entrySet()) {
            String propName = entry.getKey();
            CsdlProperty property = entry.getValue();
            property.writeXml(nameSpace, propName, writer);
        }
        writer.write("</ComplexType>");
    }

}
