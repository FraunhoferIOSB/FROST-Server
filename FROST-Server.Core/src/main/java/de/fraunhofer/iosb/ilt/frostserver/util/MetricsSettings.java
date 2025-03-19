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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigProvider;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;

/**
 * Settings for the metrics system.
 */
public class MetricsSettings extends ConfigProvider<MetricsSettings> {

    public static final String PREFIX_METRICS = "metrics.";

    @DefaultValueBoolean(false)
    public static final String TAG_USE_SERVLET = "useServlet";

    @DefaultValueBoolean(true)
    public static final String TAG_USE_INTERNAL = "useInternalHttpServer";

    @DefaultValueInt(9400)
    public static final String TAG_ENDPOINT_PORT = "endpointPort";

    public MetricsSettings(CoreSettings coreSettings) {
        setSettings(coreSettings.getHttpSettings().getSubSettings(PREFIX_METRICS));
    }

    @Override
    public MetricsSettings getThis() {
        return this;
    }

}
