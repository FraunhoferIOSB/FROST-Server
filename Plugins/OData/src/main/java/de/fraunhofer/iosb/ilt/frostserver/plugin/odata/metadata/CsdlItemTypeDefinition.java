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

import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimpleCustom;
import java.io.IOException;
import java.io.Writer;

public class CsdlItemTypeDefinition implements CsdlSchemaItem {

    @JsonProperty("$Kind")
    public String kind = "TypeDefinition";

    @JsonProperty("$UnderlyingType")
    public String underlyingType;

    @JsonProperty("@Core.Description")
    public String description;

    public CsdlItemTypeDefinition generateFrom(TypeSimpleCustom tc) {
        underlyingType = tc.getUnderlyingType().getName();
        description = tc.getDescription();
        return this;
    }

    @Override
    public void writeXml(String nameSpace, String name, Writer writer) throws IOException {
        writer.write("<TypeDefinition Name=\"" + name + "\" UnderlyingType=\"" + underlyingType + "\" />");
    }

}
