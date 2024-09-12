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
package de.fraunhofer.iosb.ilt.frostserver.http.common;

import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigProvider;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for setting up the tomcat Remote-IP-Filter.
 *
 * See
 * https://tomcat.apache.org/tomcat-9.0-doc/config/filter.html#Remote_IP_Filter
 */
public class IpFilterHelper extends ConfigProvider<IpFilterHelper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpFilterHelper.class.getName());

    public static final String PREFIX_REMOTE_IP_FILTER = "remoteIpFilter.";
    public static final String NAME_REMOTE_IP_FILTER = "RemoteIpFilter";
    public static final String NAME_CLASS_REMOTE_IP_FILTER = "org.apache.catalina.filters.RemoteIpFilter";

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE = "enable";

    public IpFilterHelper(Settings settings) {
        super(settings);
    }

    public static void setupRemoteIpFilter(ServletContext servletContext, CoreSettings coreSettings) {
        Settings httpSettings = coreSettings.getHttpSettings();
        new IpFilterHelper(httpSettings.getSubSettings(PREFIX_REMOTE_IP_FILTER))
                .setupFilter(servletContext);

    }

    private void setupFilter(ServletContext servletContext) {
        if (!getBoolean(TAG_ENABLE)) {
            return;
        }
        try {
            FilterRegistration.Dynamic ripFilter = servletContext.addFilter(NAME_REMOTE_IP_FILTER, NAME_CLASS_REMOTE_IP_FILTER);
            ripFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/*");
            setInitParameter(ripFilter, "enableLookups");
            setInitParameter(ripFilter, "remoteIpHeader");
            setInitParameter(ripFilter, "internalProxies");
            setInitParameter(ripFilter, "proxiesHeader");
            setInitParameter(ripFilter, "requestAttributesEnabled");
            setInitParameter(ripFilter, "trustedProxies");
            setInitParameter(ripFilter, "protocolHeader");
            setInitParameter(ripFilter, "hostHeader");
            setInitParameter(ripFilter, "hostHeader");
            setInitParameter(ripFilter, "protocolHeaderHttpsValue");
            setInitParameter(ripFilter, "httpServerPort");
            setInitParameter(ripFilter, "httpsServerPort");
            setInitParameter(ripFilter, "changeLocalName");
            setInitParameter(ripFilter, "changeLocalPort");
        } catch (Exception exc) {
            LOGGER.error("Failed to initialise RemoteIpFilter.", exc);
        }
    }

    private void setInitParameter(FilterRegistration.Dynamic filter, String pramName) {
        String value = getSettings().get(pramName, "");
        if (!StringHelper.isNullOrEmpty(value)) {
            filter.setInitParameter(pramName, value);
        }
    }

    @Override
    public IpFilterHelper getThis() {
        return this;
    }

}
