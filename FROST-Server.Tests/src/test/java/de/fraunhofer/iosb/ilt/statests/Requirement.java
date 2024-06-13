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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author scf
 */
public enum Requirement {
    IOT_SENSING_1_1_DATAMODEL_ENTITY_CONTROL_INFORMATION_COMMON_CONTROL_INFORMATION("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/entity-control-information/common-control-information"),
    IOT_SENSING_1_1_DATAMODEL_THING_PROPERTIES("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/thing/properties"),
    IOT_SENSING_1_1_DATAMODEL_THING_RELATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/thing/relations"),
    IOT_SENSING_1_1_DATAMODEL_LOCATION_PROPERTIES("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/location/properties"),
    IOT_SENSING_1_1_DATAMODEL_LOCATION_RELATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/location/relations"),
    IOT_SENSING_1_1_DATAMODEL_HISTORICAL_LOCATION_PROPERTIES("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/historical-location/properties"),
    IOT_SENSING_1_1_DATAMODEL_HISTORICAL_LOCATION_RELATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/historical-location/relations"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_HISTORICAL_LOCATION_AUTO_CREATION("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/historical-location-auto-creation"),
    IOT_SENSING_1_1_DATAMODEL_DATASTREAM_PROPERTIES("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/datastream/properties"),
    IOT_SENSING_1_1_DATAMODEL_DATASTREAM_RELATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/datastream/relations"),
    IOT_SENSING_1_1_DATAMODEL_SENSOR_PROPERTIES("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/sensor/properties"),
    IOT_SENSING_1_1_DATAMODEL_SENSOR_RELATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/sensor/relations"),
    IOT_SENSING_1_1_DATAMODEL_OBSERVED_PROPERTY_PROPERTIES("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/observed-property/properties"),
    IOT_SENSING_1_1_DATAMODEL_OBSERVED_PROPERTY_RELATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/observed-property/relations"),
    IOT_SENSING_1_1_DATAMODEL_OBSERVATION_PROPERTIES("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/observation/properties"),
    IOT_SENSING_1_1_DATAMODEL_OBSERVATION_RELATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/observation/relations"),
    IOT_SENSING_1_1_DATAMODEL_FEATURE_OF_INTEREST_PROPERTIES("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/feature-of-interest/properties"),
    IOT_SENSING_1_1_DATAMODEL_FEATURE_OF_INTEREST_RELATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/feature-of-interest/relations"),
    IOT_SENSING_1_1_RESOURCE_PATH_RESOURCE_PATH_TO_ENTITIES("http://www.opengis.net/spec/iot_sensing/1.1/req/resource-path/resource-path-to-entities"),
    IOT_SENSING_1_1_REQUEST_DATA_STATUS_CODE("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/status-code"),
    IOT_SENSING_1_1_REQUEST_DATA_QUERY_STATUS_CODE("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/query-status-code"),
    IOT_SENSING_1_1_REQUEST_DATA_ORDER("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/order"),
    IOT_SENSING_1_1_REQUEST_DATA_EXPAND("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/expand"),
    IOT_SENSING_1_1_REQUEST_DATA_SELECT("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/select"),
    IOT_SENSING_1_1_REQUEST_DATA_ORDERBY("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/orderby"),
    IOT_SENSING_1_1_REQUEST_DATA_TOP("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/top"),
    IOT_SENSING_1_1_REQUEST_DATA_SKIP("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/skip"),
    IOT_SENSING_1_1_REQUEST_DATA_COUNT("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/count"),
    IOT_SENSING_1_1_REQUEST_DATA_FILTER("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/filter"),
    IOT_SENSING_1_1_REQUEST_DATA_BUILT_IN_FILTER_OPERATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/built-in-filter-operations"),
    IOT_SENSING_1_1_REQUEST_DATA_BUILT_IN_QUERY_FUNCTIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/built-in-query-functions"),
    IOT_SENSING_1_1_REQUEST_DATA_PAGINATION("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/pagination"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_CREATE_ENTITY("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/create-entity"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_LINK_TO_EXISTING_ENTITIES("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/link-to-existing-entities"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_DEEP_INSERT("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/deep-insert"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_DEEP_INSERT_STATUS_CODE("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/deep-insert-status-code"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_UPDATE_ENTITY("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/update-entity"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_DELETE_ENTITY("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/delete-entity"),
    IOT_SENSING_1_1_BATCH_REQUEST_BATCH_REQUEST("http://www.opengis.net/spec/iot_sensing/1.1/req/batch-request/batch-request"),
    IOT_SENSING_1_1_MULTI_DATASTREAM_PROPERTIES("http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream/properties"),
    IOT_SENSING_1_1_MULTI_DATASTREAM_RELATIONS("http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream/relations"),
    IOT_SENSING_1_1_MULTI_DATASTREAM_CONSTRAINTS("http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream/constraints"),
    IOT_SENSING_1_1_DATA_ARRAY_DATA_ARRAY("http://www.opengis.net/spec/iot_sensing/1.1/req/data-array/data-array"),
    IOT_SENSING_1_1_CREATE_OBSERVATIONS_VIA_MQTT_OBSERVATIONS_CREATION("http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation"),
    IOT_SENSING_1_1_RECEIVE_UPDATES_VIA_MQTT_RECEIVE_UPDATES("http://www.opengis.net/spec/iot_sensing/1.1/req/receive-updates-via-mqtt/receive-updates"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_HISTORICAL_LOCATION_MANUAL_CREATION("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/historical-location-manual-creation"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_UPDATE_ENTITY_PUT("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/update-entity-put"),
    IOT_SENSING_1_1_CREATE_UPDATE_DELETE_UPDATE_ENTITY_JSONPATCH("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/update-entity-jsonpatch"),
    IOT_TASKING_1_0_TASKING_CAPABILITY_PROPERTIES("http://www.opengis.net/spec/iot_tasking/1.0/req/tasking-capability/properties"),
    IOT_TASKING_1_0_TASKING_CAPABILITY_RELATIONS("http://www.opengis.net/spec/iot_tasking/1.0/req/tasking-capability/relations"),
    IOT_TASKING_1_0_TASK_PROPERTIES("http://www.opengis.net/spec/iot_tasking/1.0/req/task/properties"),
    IOT_TASKING_1_0_TASK_RELATIONS("http://www.opengis.net/spec/iot_tasking/1.0/req/task/relations"),
    IOT_TASKING_1_0_ACTUATOR_PROPERTIES("http://www.opengis.net/spec/iot_tasking/1.0/req/actuator/properties"),
    IOT_TASKING_1_0_ACTUATOR_RELATIONS("http://www.opengis.net/spec/iot_tasking/1.0/req/actuator/relations"),
    IOT_TASKING_1_0_CREATE_TASKS_TASK_CREATION("http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks/task-creation"),
    IOT_TASKING_1_0_CREATE_TASKS_VIA_MQTT_TASK_CREATION("http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks-via-mqtt/task-creation"),
    IOT_TASKING_1_0_RECEIVE_UPDATES_VIA_MQTT_RECEIVE_UPDATED("http://www.opengis.net/spec/iot_tasking/1.0/req/receive-updates-via-mqtt/receive-updated"),
    FROST_BATCH_REQUESTS_JSON("https://fraunhoferiosb.github.io/FROST-Server/extensions/JsonBatchRequest.html"),
    FROST_RESPONSE_METADATA("https://fraunhoferiosb.github.io/FROST-Server/extensions/ResponseMetadata.html"),
    FROST_RESULT_FORMAT_CSV("https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/CSV-ResultFormat/CSV-ResultFormat.md"),
    FROST_RESULT_FORMAT_GEOJSON("https://fraunhoferiosb.github.io/FROST-Server/extensions/GeoJSON-ResultFormat.html"),
    FROST_SELECT_DEEP("https://fraunhoferiosb.github.io/FROST-Server/extensions/DeepSelect.html"),
    FROST_SELECT_DISTINCT("https://fraunhoferiosb.github.io/FROST-Server/extensions/SelectDistinct.html");

    private final String name;

    private Requirement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean matches(String spec) {
        return name.startsWith(spec);
    }

    public static Set<Requirement> getAllMatching(final String spec) {
        return Arrays.asList(values())
                .stream()
                .filter((Requirement t) -> t.matches(spec))
                .collect(Collectors.toSet());
    }
}
