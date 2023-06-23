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

import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.Extension;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author scf
 */
public class ServerSettings {

    public static final Requirement TASKING_REQ = Requirement.IOT_TASKING_1_0_TASKING_CAPABILITY_PROPERTIES;
    public static final Requirement MULTIDATA_REQ = Requirement.IOT_SENSING_1_1_MULTI_DATASTREAM_CONSTRAINTS;

    /**
     * The root of FROST, without the v1.0.
     */
    private String serviceRootUrl = "";

    private String mqttUrl = "";

    private final Map<ServerVersion, Set<Requirement>> implementedRequirements = new HashMap<>();
    private final Set<ServerVersion> implementedVersions = new LinkedHashSet<>();

    private final Set<Extension> extensions = EnumSet.noneOf(Extension.class);
    private final Set<EntityType> enabledEntityTypes = EnumSet.noneOf(EntityType.class);

    /**
     * The timeout to use when waiting for MQTT messages.
     */
    private final long mqttTimeOut = 30000;

    public void setServiceRootUrl(String serviceRootUrl) {
        if (serviceRootUrl.endsWith("/")) {
            this.serviceRootUrl = serviceRootUrl.substring(0, serviceRootUrl.length() - 1);
        } else {
            this.serviceRootUrl = serviceRootUrl;
        }
    }

    public void initExtensionsAndTypes() {
        getExtensions().add(Extension.CORE);
        if (implementsRequirement(MULTIDATA_REQ) != null) {
            getExtensions().add(Extension.MULTI_DATASTREAM);
        }
        if (implementsRequirement(TASKING_REQ) != null) {
            getExtensions().add(Extension.ACTUATION);
        }
        for (EntityType entityType : EntityType.values()) {
            if (!extensions.contains(entityType.getExtension())) {
                continue;
            }
            getEnabledEntityTypes().add(entityType);
        }
    }

    /**
     * The root of FROST, without the v1.0.
     *
     * @return the serviceRootUrl
     */
    public String getServiceRootUrl() {
        return serviceRootUrl;
    }

    /**
     * The root of the sta service, with the version number.
     *
     * @param version The version to get the service url for.
     * @return the serviceUrl
     */
    public String getServiceUrl(ServerVersion version) {
        return serviceRootUrl + "/" + version.urlPart;
    }

    /**
     * @return the mqttUrl
     */
    public String getMqttUrl() {
        return mqttUrl;
    }

    public void setMqttUrl(String mqttUrl) {
        this.mqttUrl = mqttUrl;
    }

    public boolean hasTasking(ServerVersion version) {
        return implementsRequirement(version, ServerSettings.TASKING_REQ);
    }

    public void addImplementedRequirement(ServerVersion version, Requirement requirement) {
        implementedRequirements
                .computeIfAbsent(version, t -> new HashSet<>())
                .add(requirement);
    }

    public void addImplementedRequirements(ServerVersion version, Set<Requirement> requirements) {
        implementedRequirements
                .computeIfAbsent(version, t -> new HashSet<>())
                .addAll(requirements);
    }

    public boolean implementsRequirement(ServerVersion version, Requirement req) {
        return implementedRequirements
                .computeIfAbsent(version, t -> new HashSet())
                .contains(req);
    }

    /**
     * Returns the first version it finds that implements the given requirement,
     * or null if it is not implemented.
     *
     * @param req
     * @return
     */
    public ServerVersion implementsRequirement(Requirement req) {
        for (Map.Entry<ServerVersion, Set<Requirement>> entry : implementedRequirements.entrySet()) {
            if (entry.getValue().contains(req)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * @return the extensions
     */
    public Set<Extension> getExtensions() {
        return extensions;
    }

    /**
     * @return the enabledEntityTypes
     */
    public Set<EntityType> getEnabledEntityTypes() {
        return enabledEntityTypes;
    }

    public void addImplementedVersion(ServerVersion version) {
        implementedVersions.add(version);
    }

    public Set<ServerVersion> getImplementedVersions() {
        return implementedVersions;
    }

    public boolean implementsVersion(ServerVersion version) {
        return implementedVersions.contains(version);
    }

    /**
     * The timeout to use when waiting for MQTT messages.
     *
     * @return the mqttTimeOut
     */
    public long getMqttTimeOut() {
        return mqttTimeOut;
    }

}
