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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_XML;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.HEADER_ACCEPT;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.REQUEST_PARAM_FORMAT;

import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata.CsdlDocument;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata.MxGraphGenerator;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

/**
 * A generator for OData CSDL documents.
 */
public class MetaDataGenerator {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MetaDataGenerator.class.getName());
    private final CoreSettings settings;
    private boolean jsonDefault = false;
    private CsdlDocument.ODataVersion version = CsdlDocument.ODataVersion.V4_01;

    public MetaDataGenerator(CoreSettings settings) {
        this.settings = settings;
    }

    public MetaDataGenerator setJsonDefault(boolean jsonDefault) {
        this.jsonDefault = jsonDefault;
        return this;
    }

    public MetaDataGenerator setVersion(CsdlDocument.ODataVersion version) {
        this.version = version;
        return this;
    }

    public ServiceResponse generateMetaData(ServiceRequest request, ServiceResponse response) {
        try {
            final CsdlDocument doc = new CsdlDocument().generateFrom(version, settings.getModelRegistry());
            String format = request.getParameter(REQUEST_PARAM_FORMAT, "");
            String accept = request.getParameter(HEADER_ACCEPT, "");
            int idxXml = accept.indexOf(CONTENT_TYPE_APPLICATION_XML);
            int idxJson = accept.indexOf(CONTENT_TYPE_APPLICATION_JSON);
            if (idxXml == -1) {
                idxXml = Integer.MAX_VALUE;
            }
            if (idxJson == -1) {
                idxJson = Integer.MAX_VALUE;
            }
            if ("drawio".equalsIgnoreCase(format)) {
                response.setContentType(CONTENT_TYPE_APPLICATION_XML);
                new MxGraphGenerator().generate(
                        response.getWriter(),
                        settings.getModelRegistry(),
                        PrincipalExtended.getLocalPrincipal().isAdmin());
                response.setCode(200);
            } else if ("json".equalsIgnoreCase(format)) {
                generateJson(response, doc);
            } else if ("xml".equalsIgnoreCase(format)) {
                generateXml(response, doc);
            } else if (idxJson < idxXml) {
                generateJson(response, doc);
            } else {
                if (jsonDefault) {
                    generateJson(response, doc);
                } else {
                    generateXml(response, doc);
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to generate metadata document", ex);
        }
        return response;
    }

    private void generateXml(ServiceResponse response, final CsdlDocument doc) throws IOException {
        response.setContentType(CONTENT_TYPE_APPLICATION_XML);
        doc.writeXml(response.getWriter());
        response.setCode(200);
    }

    private void generateJson(ServiceResponse response, final CsdlDocument doc) throws JacksonException {
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        SimpleJsonMapper.getSimpleObjectMapper().writeValue(response.getWriter(), doc);
        response.setCode(200);
    }
}
