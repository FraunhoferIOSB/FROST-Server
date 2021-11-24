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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData.VERSION_ODATA_40;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author hylke
 */
public class CsdlDocument {

    @JsonProperty("$Version")
    public String version = "4.01";

    @JsonProperty("$EntityContainer")
    public String entityContainer = "de.FROST.FrostService";

    @JsonAnyGetter
    @JsonAnySetter
    public Map<String, CsdlSchema> nameSpaces = new LinkedHashMap<>();

    /**
     * Fill the document using the given Settings, and return itself.
     *
     * @param settings the CoreSettings to use.
     * @return this.
     */
    public CsdlDocument generateFrom(Version version, CoreSettings settings) {
        this.version = version == VERSION_ODATA_40 ? "4.0" : "4.01";
        String nameSpace = "de.FROST";
        nameSpaces.put(nameSpace, new CsdlSchema().generateFrom(version, nameSpace, settings));

        return this;
    }

    public void writeXml(Version version, Writer writer) throws IOException {
        if (version == VERSION_ODATA_40) {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\"><edmx:DataServices>");
        } else {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><edmx:Edmx Version=\"4.01\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\"><edmx:DataServices>");
        }
        for (Map.Entry<String, CsdlSchema> entry : nameSpaces.entrySet()) {
            String name = entry.getKey();
            CsdlSchema schema = entry.getValue();
            schema.writeXml(name, writer);
        }

        writer.write("</edmx:DataServices></edmx:Edmx>");
    }
}
