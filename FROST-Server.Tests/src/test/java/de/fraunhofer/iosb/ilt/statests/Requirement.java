/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
    iot_sensing_1_1_datamodel_entity_control_information_common_control_information("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/entity-control-information/common-control-information"),
    iot_sensing_1_1_datamodel_thing_properties("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/thing/properties"),
    iot_sensing_1_1_datamodel_thing_relations("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/thing/relations"),
    iot_sensing_1_1_datamodel_location_properties("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/location/properties"),
    iot_sensing_1_1_datamodel_location_relations("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/location/relations"),
    iot_sensing_1_1_datamodel_historical_location_properties("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/historical-location/properties"),
    iot_sensing_1_1_datamodel_historical_location_relations("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/historical-location/relations"),
    iot_sensing_1_1_create_update_delete_historical_location_auto_creation("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/historical-location-auto-creation"),
    iot_sensing_1_1_datamodel_datastream_properties("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/datastream/properties"),
    iot_sensing_1_1_datamodel_datastream_relations("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/datastream/relations"),
    iot_sensing_1_1_datamodel_sensor_properties("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/sensor/properties"),
    iot_sensing_1_1_datamodel_sensor_relations("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/sensor/relations"),
    iot_sensing_1_1_datamodel_observed_property_properties("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/observed-property/properties"),
    iot_sensing_1_1_datamodel_observed_property_relations("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/observed-property/relations"),
    iot_sensing_1_1_datamodel_observation_properties("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/observation/properties"),
    iot_sensing_1_1_datamodel_observation_relations("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/observation/relations"),
    iot_sensing_1_1_datamodel_feature_of_interest_properties("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/feature-of-interest/properties"),
    iot_sensing_1_1_datamodel_feature_of_interest_relations("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel/feature-of-interest/relations"),
    iot_sensing_1_1_resource_path_resource_path_to_entities("http://www.opengis.net/spec/iot_sensing/1.1/req/resource-path/resource-path-to-entities"),
    iot_sensing_1_1_request_data_status_code("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/status-code"),
    iot_sensing_1_1_request_data_query_status_code("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/query-status-code"),
    iot_sensing_1_1_request_data_order("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/order"),
    iot_sensing_1_1_request_data_expand("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/expand"),
    iot_sensing_1_1_request_data_select("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/select"),
    iot_sensing_1_1_request_data_orderby("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/orderby"),
    iot_sensing_1_1_request_data_top("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/top"),
    iot_sensing_1_1_request_data_skip("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/skip"),
    iot_sensing_1_1_request_data_count("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/count"),
    iot_sensing_1_1_request_data_filter("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/filter"),
    iot_sensing_1_1_request_data_built_in_filter_operations("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/built-in-filter-operations"),
    iot_sensing_1_1_request_data_built_in_query_functions("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/built-in-query-functions"),
    iot_sensing_1_1_request_data_pagination("http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/pagination"),
    iot_sensing_1_1_create_update_delete_create_entity("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/create-entity"),
    iot_sensing_1_1_create_update_delete_link_to_existing_entities("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/link-to-existing-entities"),
    iot_sensing_1_1_create_update_delete_deep_insert("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/deep-insert"),
    iot_sensing_1_1_create_update_delete_deep_insert_status_code("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/deep-insert-status-code"),
    iot_sensing_1_1_create_update_delete_update_entity("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/update-entity"),
    iot_sensing_1_1_create_update_delete_delete_entity("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/delete-entity"),
    iot_sensing_1_1_batch_request_batch_request("http://www.opengis.net/spec/iot_sensing/1.1/req/batch-request/batch-request"),
    iot_sensing_1_1_multi_datastream_properties("http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream/properties"),
    iot_sensing_1_1_multi_datastream_relations("http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream/relations"),
    iot_sensing_1_1_multi_datastream_constraints("http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream/constraints"),
    iot_sensing_1_1_data_array_data_array("http://www.opengis.net/spec/iot_sensing/1.1/req/data-array/data-array"),
    iot_sensing_1_1_create_observations_via_mqtt_observations_creation("http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation"),
    iot_sensing_1_1_receive_updates_via_mqtt_receive_updates("http://www.opengis.net/spec/iot_sensing/1.1/req/receive-updates-via-mqtt/receive-updates"),
    iot_sensing_1_1_create_update_delete_historical_location_manual_creation("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/historical-location-manual-creation"),
    iot_sensing_1_1_create_update_delete_update_entity_put("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/update-entity-put"),
    iot_sensing_1_1_create_update_delete_update_entity_jsonpatch("http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete/update-entity-jsonpatch"),
    iot_tasking_1_0_tasking_capability_properties("http://www.opengis.net/spec/iot_tasking/1.0/req/tasking-capability/properties"),
    iot_tasking_1_0_tasking_capability_relations("http://www.opengis.net/spec/iot_tasking/1.0/req/tasking-capability/relations"),
    iot_tasking_1_0_task_properties("http://www.opengis.net/spec/iot_tasking/1.0/req/task/properties"),
    iot_tasking_1_0_task_relations("http://www.opengis.net/spec/iot_tasking/1.0/req/task/relations"),
    iot_tasking_1_0_actuator_properties("http://www.opengis.net/spec/iot_tasking/1.0/req/actuator/properties"),
    iot_tasking_1_0_actuator_relations("http://www.opengis.net/spec/iot_tasking/1.0/req/actuator/relations"),
    iot_tasking_1_0_create_tasks_task_creation("http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks/task-creation"),
    iot_tasking_1_0_create_tasks_via_mqtt_task_creation("http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks-via-mqtt/task-creation"),
    iot_tasking_1_0_receive_updates_via_mqtt_receive_updated("http://www.opengis.net/spec/iot_tasking/1.0/req/receive-updates-via-mqtt/receive-updated");

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
