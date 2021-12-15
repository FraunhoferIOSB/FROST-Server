/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBusFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthProvider;
import de.fraunhofer.iosb.ilt.frostserver.util.GitVersionInfo;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jab, scf
 */
public abstract class AbstractContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContextListener.class);

    private CoreSettings coreSettings;

    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    private synchronized void initCoreSettings(ServletContext context) {
        if (coreSettings != null) {
            return;
        }
        final Properties properties = new Properties();
        final Enumeration<String> names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final String targetName = name.replace("_", ".");
            final String value = context.getInitParameter(name);
            if (value == null) {
                LOGGER.error("NULL value for {}", name);
            } else {
                properties.put(targetName, value);
            }
        }
        if (!properties.containsKey(CoreSettings.TAG_TEMP_PATH)) {
            properties.setProperty(CoreSettings.TAG_TEMP_PATH, String.valueOf(context.getAttribute(ServletContext.TEMPDIR)));
        }
        coreSettings = new CoreSettings(properties);

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        GitVersionInfo.logGitInfo();

        if (sce != null && sce.getServletContext() != null) {
            LOGGER.info("Context initialised, loading settings.");
            ServletContext context = sce.getServletContext();

            initCoreSettings(context);
            context.setAttribute(TAG_CORE_SETTINGS, coreSettings);

            setUpCorsFilter(context, coreSettings);

            PersistenceManagerFactory.init(coreSettings);
            PersistenceManagerFactory.getInstance(coreSettings);
            MessageBusFactory.createMessageBus(coreSettings);

            setupAuthFilter(context, coreSettings);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Context destroyed, shutting down threads...");
        if (coreSettings == null) {
            return;
        }
        final MessageBus messageBus = coreSettings.getMessageBus();
        if (messageBus != null) {
            messageBus.stop();
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ex) {
                LOGGER.debug("Rude wakeup?", ex);
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Context destroyed, done shutting down threads.");
    }

    private void setUpCorsFilter(ServletContext servletContext, CoreSettings coreSettings) {
        Settings httpSettings = coreSettings.getHttpSettings();
        boolean corsEnable = httpSettings.getBoolean(CoreSettings.TAG_CORS_ENABLE, CoreSettings.class);
        if (corsEnable) {
            try {
                String filterName = "CorsFilter";

                FilterRegistration.Dynamic corsFilter = servletContext.addFilter(filterName, "org.apache.catalina.filters.CorsFilter");
                corsFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/*");

                String allowedOrigins = httpSettings.get(CoreSettings.TAG_CORS_ALLOWED_ORIGINS, CoreSettings.class);
                corsFilter.setInitParameter("cors.allowed.origins", allowedOrigins);

                String allowedMethods = httpSettings.get(CoreSettings.TAG_CORS_ALLOWED_METHODS, CoreSettings.class);
                corsFilter.setInitParameter("cors.allowed.methods", allowedMethods);

                String exposedHeaders = httpSettings.get(CoreSettings.TAG_CORS_EXPOSED_HEADERS, CoreSettings.class);
                corsFilter.setInitParameter("cors.exposed.headers", exposedHeaders);

                String allowedHeaders = httpSettings.get(CoreSettings.TAG_CORS_ALLOWED_HEADERS, CoreSettings.class);
                corsFilter.setInitParameter("cors.allowed.headers", allowedHeaders);

                String supportCreds = httpSettings.get(CoreSettings.TAG_CORS_SUPPORT_CREDENTIALS, CoreSettings.class);
                corsFilter.setInitParameter("cors.support.credentials", supportCreds);

                String preflightMaxage = httpSettings.get(CoreSettings.TAG_CORS_PREFLIGHT_MAXAGE, CoreSettings.class);
                corsFilter.setInitParameter("cors.preflight.maxage", preflightMaxage);

                String requestDecorate = httpSettings.get(CoreSettings.TAG_CORS_REQUEST_DECORATE, CoreSettings.class);
                corsFilter.setInitParameter("cors.request.decorate", requestDecorate);
            } catch (Exception exc) {
                LOGGER.error("Failed to initialise CORS filter.", exc);
            }
        }
    }

    private void setupAuthFilter(ServletContext servletContext, CoreSettings coreSettings) {
        Settings authSettings = coreSettings.getAuthSettings();
        String authProviderClassName = authSettings.get(CoreSettings.TAG_AUTH_PROVIDER, CoreSettings.class);
        if (!StringHelper.isNullOrEmpty(authProviderClassName)) {
            LOGGER.info("Turning on authentication.");
            try {
                Class<?> authConfigClass = ClassUtils.getClass(authProviderClassName);
                if (AuthProvider.class.isAssignableFrom(authConfigClass)) {
                    Class<AuthProvider> filterConfigClass = (Class<AuthProvider>) authConfigClass;
                    AuthProvider filterConfigurator = filterConfigClass.getDeclaredConstructor().newInstance();
                    filterConfigurator.init(coreSettings);
                    filterConfigurator.addFilter(servletContext, coreSettings);

                    // If all went well, register the filter so it can upgrade its database.
                    coreSettings.addLiquibaseUser(filterConfigurator);
                } else {
                    throw new IllegalArgumentException("Configured class does not implement AuthProvider.");
                }
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                throw new IllegalArgumentException("Could not find or load auth class: " + authProviderClassName, ex);
            }
        }

    }

}
