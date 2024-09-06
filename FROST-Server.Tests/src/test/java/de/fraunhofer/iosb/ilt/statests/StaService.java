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
package de.fraunhofer.iosb.ilt.statests;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11MultiDatastream;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Tasking;
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
    public final SensorThingsV11Sensing modelSensing;
    public final SensorThingsV11MultiDatastream modelMultiDatastream;
    public final SensorThingsV11Tasking modelTasking;

    public StaService(URL url) throws MalformedURLException {
        this.modelSensing = new SensorThingsV11Sensing();
        this.modelMultiDatastream = new SensorThingsV11MultiDatastream();
        this.modelTasking = new SensorThingsV11Tasking();
        this.service = new SensorThingsService(modelSensing, modelMultiDatastream, modelTasking)
                .setBaseUrl(url)
                .init();
    }

    public StaService(SensorThingsService service, SensorThingsV11Sensing modelSensing, SensorThingsV11Tasking modelTasking, SensorThingsV11MultiDatastream modelMds) {
        this.service = service;
        this.modelSensing = modelSensing;
        this.modelMultiDatastream = modelMds;
        this.modelTasking = modelTasking;
    }

}
