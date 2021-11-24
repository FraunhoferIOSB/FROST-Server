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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.io.Writer;

public class CsdlPropertyEntity implements CsdlProperty {

    private final String TYPE_DEFAULT = "Edm.String";

    @JsonProperty("$Type")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String type;

    @JsonProperty("$Nullable")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean nullable;

    public CsdlPropertyEntity generateFrom(Version version, String nameSpace, CoreSettings settings, EntityType et, EntityPropertyMain ep) {
        final PropertyType propertyType = ep.getType();
        type = propertyType.getName();
        if (!type.startsWith("Edm.")) {
            type = nameSpace + "." + type;
        }
        if (TYPE_DEFAULT.equals(type)) {
            type = null;
        }
        if (TypeSimplePrimitive.EDM_UNTYPED == propertyType && version == PluginOData.VERSION_ODATA_40) {
            type = TypeSimplePrimitive.EDM_DECIMAL.getName();
        }
        if (et.getPrimaryKey() != ep && !et.isRequired(ep)) {
            nullable = true;
        }
        return this;
    }

    public CsdlPropertyEntity generateFrom(String nameSpace, CoreSettings settings, PropertyType value) {
        type = value.getName();
        if (!type.startsWith("Edm.")) {
            type = nameSpace + "." + type;
        }
        if (TYPE_DEFAULT.equals(type)) {
            type = null;
        }
        return this;
    }

    @Override
    public void writeXml(String nameSpace, String name, Writer writer) throws IOException {
        String typeString = type == null ? "String" : type;
        String nullableString = (nullable) ? " Nullable=\"" + Boolean.toString(nullable) + "\"" : "";
        writer.write("<Property Name=\"" + name + "\" Type=\"" + typeString + "\"" + nullableString + " />");
    }

}
