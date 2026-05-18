/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeEnumeration;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimpleCustom;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsdlSchema {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsdlSchema.class.getName());

    @JsonIgnore
    private String namespace;

    @JsonAnyGetter
    @JsonAnySetter
    public Map<String, CsdlSchemaItem> schemaItems = new LinkedHashMap<>();

    public CsdlSchema generateFrom(CsdlDocument doc, CsdlDocument.ODataVersion version, String namespace, ModelRegistry mr) {
        this.namespace = namespace;
        for (EntityType entityType : mr.getEntityTypes(PrincipalExtended.getLocalPrincipal().isAdmin())) {
            if (namespace.equals(entityType.getNamespace())) {
                schemaItems.put(entityType.entityName, new CsdlItemEntityType().generateFrom(doc, version, namespace, entityType));
            }
        }
        for (Entry<String, PropertyType> entry : mr.getPropertyTypes().entrySet()) {
            PropertyType value = entry.getValue();
            if (!namespace.equals(value.getNamespace())) {
                continue;
            }
            String name = value.getName();
            if (value instanceof TypeComplex tc) {
                schemaItems.put(name, new CsdlItemComplexType().generateFrom(doc, namespace, tc));
            } else if (value instanceof TypeSimpleCustom tc) {
                schemaItems.put(name, new CsdlItemTypeDefinition().generateFrom(tc));
            } else if (value instanceof TypeEnumeration te) {
                schemaItems.put(name, new CsdlItemEnumType().generateFrom(te));
            } else {
                LOGGER.debug("Unknown PropertyType {}", value);
            }
        }

        return this;
    }

    public void addSchemaItem(String name, CsdlSchemaItem item) {
        schemaItems.put(name, item);
    }

    public void writeXml(Writer writer) throws IOException {
        writer.write("<Schema Namespace=\"" + namespace + "\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">");
        for (Map.Entry<String, CsdlSchemaItem> entry : schemaItems.entrySet()) {
            String name = entry.getKey();
            CsdlSchemaItem item = entry.getValue();
            item.writeXml(name, writer);
        }
        writer.write("</Schema>");
    }
}
