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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData.VERSION_ODATA_40;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;

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

    @JsonProperty("$Reference")
    private final Map<String, ReferencedDoc> referencedDocs = new TreeMap<>();

    private static class ReferencedDoc {

        @JsonProperty("$Include")
        private final List<ReferencedNamespace> includes = new ArrayList<>();

        @JsonIgnore
        private final String url;

        @JsonIgnore
        private final Map<String, ReferencedNamespace> namespaces = new TreeMap<>();

        @JsonIgnore
        private final Set<String> aliases = new TreeSet<>();

        public ReferencedDoc(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public ReferencedDoc addAnnotation(CsdlAnnotation annotation) {
            ReferencedNamespace refNs = namespaces.computeIfAbsent(annotation.getNamespace(), ns -> createReferencedNamespace(ns));
            refNs.addAnnotation(annotation);
            return this;
        }

        public List<ReferencedNamespace> getIncludes() {
            return includes;
        }

        private ReferencedNamespace createReferencedNamespace(String namespace) {
            String[] parts = StringUtils.split(namespace, '.');
            String baseAlias = parts[parts.length - 2];
            int i = 1;
            String alias = baseAlias;
            while (aliases.contains(alias)) {
                alias = baseAlias + (i++);
            }
            final ReferencedNamespace refNs = new ReferencedNamespace(namespace).setAlias(alias);
            aliases.add(alias);
            includes.add(refNs);
            return refNs;
        }

        public void writeXml(Writer writer) throws IOException {
            String xmlUrl = url.replace(".json", ".xml");
            writer.write("<edmx:Reference Uri=\"" + xmlUrl + "\">");
            for (ReferencedNamespace include : includes) {
                include.writeXml(writer);
            }
            writer.write("</edmx:Reference>");
        }
    }

    private static class ReferencedNamespace {

        @JsonProperty("$Namespace")
        private final String namespace;
        @JsonProperty("$Alias")
        private String alias;

        @JsonIgnore
        private final List<CsdlAnnotation> annotations = new ArrayList<>();

        public ReferencedNamespace(String name) {
            this.namespace = name;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getAlias() {
            return alias;
        }

        public ReferencedNamespace setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public List<CsdlAnnotation> getAnnotations() {
            return annotations;
        }

        public ReferencedNamespace addAnnotation(CsdlAnnotation annotation) {
            annotations.add(annotation);
            annotation.setNamespace(alias);
            return this;
        }

        public void writeXml(Writer writer) throws IOException {
            writer.write("<edmx:Include Alias=\"" + alias + "\" Namespace=\"" + namespace + "\" />");
        }
    }

    /**
     * Fill the document using the given Settings, and return itself.
     *
     * @param settings the CoreSettings to use.
     * @return this.
     */
    public CsdlDocument generateFrom(Version version, CoreSettings settings) {
        this.version = version == VERSION_ODATA_40 ? "4.0" : "4.01";
        String nameSpace = "de.FROST";
        nameSpaces.put(nameSpace, new CsdlSchema().generateFrom(this, version, nameSpace, settings));
        return this;
    }

    public void registerAnnotation(String baseUrl, CsdlAnnotation annotation) {
        referencedDocs.computeIfAbsent(baseUrl, ReferencedDoc::new).addAnnotation(annotation);
    }

    public void writeXml(Version version, Writer writer) throws IOException {
        if (version == VERSION_ODATA_40) {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">");
        } else {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><edmx:Edmx Version=\"4.01\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">");
        }
        for (ReferencedDoc refDoc : referencedDocs.values()) {
            refDoc.writeXml(writer);
        }
        writer.write("<edmx:DataServices>");
        for (Entry<String, CsdlSchema> entry : nameSpaces.entrySet()) {
            String name = entry.getKey();
            CsdlSchema schema = entry.getValue();
            schema.writeXml(name, writer);
        }

        writer.write("</edmx:DataServices></edmx:Edmx>");
    }
}
