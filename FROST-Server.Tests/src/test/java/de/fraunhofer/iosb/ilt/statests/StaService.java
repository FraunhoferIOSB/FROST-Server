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
package de.fraunhofer.iosb.ilt.statests;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsMultiDatastreamV11;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsTaskingV11;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author hylke
 */
public class StaService {

    /**
     * A FROST-Client instance that can be used to access the server.
     */
    public final SensorThingsService service;
    public final SensorThingsSensingV11 modelSensing;
    public final SensorThingsMultiDatastreamV11 modelMultiDatastream;
    public final SensorThingsTaskingV11 modelTasking;

    public StaService(URL url) throws MalformedURLException {
        this.modelSensing = new SensorThingsSensingV11();
        this.modelMultiDatastream = new SensorThingsMultiDatastreamV11(modelSensing);
        this.modelTasking = new SensorThingsTaskingV11(modelSensing);
        this.service = new SensorThingsService(modelSensing.getModelRegistry(), url);
    }

    public StaService(SensorThingsService service, SensorThingsSensingV11 modelSensing, SensorThingsTaskingV11 modelTasking, SensorThingsMultiDatastreamV11 modelMds) {
        this.service = service;
        this.modelSensing = modelSensing;
        this.modelMultiDatastream = modelMds;
        this.modelTasking = modelTasking;
    }

}
