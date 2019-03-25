/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.json.serialize;

import de.fraunhofer.iosb.ilt.frostserver.model.mixin.FeatureMixIn;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.custom.CustomSerializer;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;

/**
 *
 * @author jab
 */
public class GeoJsonSerializer implements CustomSerializer {

    private static ObjectMapper mapper;

    private static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .addMixIn(Feature.class, FeatureMixIn.class);
        }
        return mapper;
    }

    @Override
    public String serialize(Object object) throws JsonProcessingException {
        if (object == null || !GeoJsonObject.class.isAssignableFrom(object.getClass())) {
            return null;
        }
        GeoJsonObject geoJson = (GeoJsonObject) object;
        return getMapper().writeValueAsString(geoJson);
    }

}
