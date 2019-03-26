/*
 * Copyright (C) 2019 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import java.util.Set;

/**
 *
 * @author scf
 */
public class ServerSettings {

    /**
     * The root of FROST, without the v1.0.
     */
    public String serviceRootUrl = "";
    /**
     * The root of the sta service, with the v1.0.
     */
    public String serviceUrl = "";

    public String mqttUrl = "";

    public boolean hasMultiDatastream;
    public boolean hasActuation;

    public final Set<Extension> extensions = EnumSet.noneOf(Extension.class);
    public final Set<EntityType> enabledEntityTypes = EnumSet.noneOf(EntityType.class);

    /**
     * The timeout to use when waiting for MQTT messages.
     */
    public long mqttTimeOut = 30000;

    public void setServiceRootUrl(String serviceRootUrl) {
        if (serviceRootUrl.endsWith("/")) {
            this.serviceRootUrl = serviceRootUrl.substring(0, serviceRootUrl.length() - 1);
        } else {
            this.serviceRootUrl = serviceRootUrl;
        }
        serviceUrl = this.serviceRootUrl + "/v1.0";
    }

    public void initExtensionsAndTypes() {
        extensions.add(Extension.CORE);
        if (hasMultiDatastream) {
            extensions.add(Extension.MULTI_DATASTREAM);
        }
        if (hasActuation) {
            extensions.add(Extension.ACTUATION);
        }
        for (EntityType entityType : EntityType.values()) {
            if (!extensions.contains(entityType.getExtension())) {
                continue;
            }
            enabledEntityTypes.add(entityType);
        }
    }

}
