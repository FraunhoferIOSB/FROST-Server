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

    @DefaultValueBoolean(false)
    public static final String TAG_USE_INTERNAL = "useInternalHttpServer";

    @DefaultValueInt(9400)
    public static final String TAG_ENDPOINT_PORT = "endpointPort";

    private final boolean enabled;
    private final boolean servlet;
    private final boolean internal;

    /**
     * Initialise the metrics settings with the default prefex "metrics.".
     *
     * @param coreSettings The core settings to use.
     */
    public MetricsSettings(CoreSettings coreSettings) {
        setSettings(coreSettings.getSettings().getSubSettings(PREFIX_METRICS));
        internal = getBoolean(MetricsSettings.TAG_USE_INTERNAL);
        servlet = getBoolean(MetricsSettings.TAG_USE_SERVLET);
        enabled = internal || servlet;
    }

    @Override
    public MetricsSettings getThis() {
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isServlet() {
        return servlet;
    }

    public boolean isInternal() {
        return internal;
    }

}
