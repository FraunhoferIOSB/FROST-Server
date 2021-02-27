/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.extensions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author scf
 */
public enum Extension {
    CORE(
            "http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel",
            "http://www.opengis.net/spec/iot_sensing/1.1/req/resource-path/resource-path-to-entities",
            "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data",
            "http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete",
            "https://fraunhoferiosb.github.io/FROST-Server/extensions/DeepSelect.html",
            "https://fraunhoferiosb.github.io/FROST-Server/extensions/SelectDistinct.html"
    ),
    ACTUATION(
            "http://www.opengis.net/spec/iot_tasking/1.0/req/tasking-capability",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/task",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/actuator",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks-via-mqtt",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/receive-updates-via-mqtt"
    ),
    MULTI_DATASTREAM(
            "http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream"
    ),
    MQTT(
            "http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation",
            "http://www.opengis.net/spec/iot_sensing/1.1/req/receive-updates-via-mqtt/receive-updates"
    ),
    ENTITY_LINKING(
            "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#NavigationLinks",
            "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#Expand",
            "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#Filter"
    );

    /**
     * Flag indicating the server feature should be exposed on the index page.
     */
    private final boolean exposedFeature;
    /**
     * The requirements implemented by this extension.
     */
    private final Set<String> requirements;

    private Extension() {
        this.exposedFeature = false;
        this.requirements = Collections.emptySet();
    }

    private Extension(String... requirements) {
        this.exposedFeature = true;
        Set<String> reqs = new HashSet<>(Arrays.asList(requirements));
        this.requirements = Collections.unmodifiableSet(reqs);
    }

    public boolean isExposedFeature() {
        return exposedFeature;
    }

    public Set<String> getRequirements() {
        return requirements;
    }

}
