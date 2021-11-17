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

import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata.CsdlDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
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

    public ServiceResponse generateMetaData(ServiceResponse response) {
        try {
            final CsdlDocument doc = new CsdlDocument().generateFrom(settings);
            SimpleJsonMapper.getSimpleObjectMapper().writeValue(response.getWriter(), doc);
            response.setCode(200);
            response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        } catch (IOException ex) {
            LOGGER.error("Failed to generate metadata document", ex);
        }
        return response;
    }
}
