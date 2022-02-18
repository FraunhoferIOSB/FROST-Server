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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata.CsdlDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_XML;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class MetaDataGenerator {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MetaDataGenerator.class.getName());
    private final CoreSettings settings;

    public MetaDataGenerator(CoreSettings settings) {
        this.settings = settings;
    }

    public ServiceResponse generateMetaData(ServiceRequest request, ServiceResponse response) {
        final Version version = request.getVersion();
        try {
            final CsdlDocument doc = new CsdlDocument().generateFrom(version, settings);
            List<String> formats = request.getParameterMap().get("$format");
            String format = "";
            if (formats != null && !formats.isEmpty()) {
                format = formats.get(0);
            }
            int idxXml = format.indexOf(CONTENT_TYPE_APPLICATION_XML);
            int idxJson = format.indexOf(CONTENT_TYPE_APPLICATION_JSON);
            if (idxXml == -1) {
                idxXml = Integer.MAX_VALUE;
            }
            if (idxJson == -1) {
                idxJson = Integer.MAX_VALUE;
            }
            if (idxJson < idxXml || "json".equalsIgnoreCase(format)) {
                response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
                SimpleJsonMapper.getSimpleObjectMapper().writeValue(response.getWriter(), doc);
                response.setCode(200);
            } else {
                response.setContentType(CONTENT_TYPE_APPLICATION_XML);
                doc.writeXml(version, response.getWriter());
                response.setCode(200);
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to generate metadata document", ex);
        }
        return response;
    }
}
