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

import static de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation.DocType.JSON;

import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class CsdlAnnotation {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsdlAnnotation.class.getName());

    private String name;
    private String namespace;
    private Object value;

    public CsdlAnnotation generateFrom(CsdlDocument doc, Annotation annotation) {
        this.name = annotation.getName();
        this.namespace = annotation.getNameSpace();
        this.value = annotation.getValue();
        doc.registerAnnotation(annotation.getSourceUrl(JSON), this);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getQualifiedName() {
        return namespace + "." + name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void writeXml(Writer writer) throws IOException {
        if (value instanceof Boolean boolValue) {
            if (boolValue) {
                writer.write("<Annotation Term=\"" + getQualifiedName() + "\" />");
            }
            return;
        }
        writer.write("<Annotation Term=\"" + getQualifiedName() + "\"");
        if (value instanceof String string) {
            writer.write(" String=\"" + StringEscapeUtils.escapeXml11(string) + "\" />");
            return;
        }
        LOGGER.error("Unknown annotation value type: {}", value.getClass().getName());
    }
}
