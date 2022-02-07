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
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeEnumeration;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimpleCustom;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class CsdlSchema {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsdlSchema.class.getName());

    @JsonAnyGetter
    @JsonAnySetter
    public Map<String, CsdlSchemaItem> schemaItems = new LinkedHashMap<>();

    public CsdlSchema generateFrom(Version version, String nameSpace, CoreSettings settings) {
        ModelRegistry mr = settings.getModelRegistry();
        for (EntityType entityType : mr.getEntityTypes()) {
            schemaItems.put(entityType.entityName, new CsdlItemEntityType().generateFrom(version, nameSpace, settings, entityType));
        }
        for (Map.Entry<String, PropertyType> entry : mr.getPropertyTypes().entrySet()) {
            String name = entry.getKey();
            PropertyType value = entry.getValue();
            if (value instanceof TypeComplex) {
                TypeComplex tc = (TypeComplex) value;
                schemaItems.put(name, new CsdlItemComplexType().generateFrom(nameSpace, settings, tc));
            } else if (value instanceof TypeSimpleCustom) {
                TypeSimpleCustom tc = (TypeSimpleCustom) value;
                schemaItems.put(name, new CsdlItemTypeDefinition().generateFrom(nameSpace, settings, tc));
            } else if (value instanceof TypeEnumeration) {
                TypeEnumeration te = (TypeEnumeration) value;
                schemaItems.put(name, new CsdlItemEnumType().generateFrom(nameSpace, settings, te));
            } else {
                LOGGER.debug("Unknown PropertyType {}", value);
            }
        }
        schemaItems.put("FrostService", new CsdlItemEntityContainer().generateFrom(nameSpace, settings));

        return this;
    }

    public void writeXml(String nameSpace, Writer writer) throws IOException {
        writer.write("<Schema Namespace=\"" + nameSpace + "\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">");
        for (Map.Entry<String, CsdlSchemaItem> entry : schemaItems.entrySet()) {
            String name = entry.getKey();
            CsdlSchemaItem item = entry.getValue();
            item.writeXml(nameSpace, name, writer);
        }
        writer.write("</Schema>");
    }
}
