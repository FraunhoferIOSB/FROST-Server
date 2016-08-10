/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta;

import de.fraunhofer.iosb.ilt.sta.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManagerFactory;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
@WebListener
public class ContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);
    public static final String API_VERSION = "v1.0";
    private static final String USE_ABSOLUTE_NAVIGATION_LINKS_TAG = "useAbsoluteNavigationLinks";
    private static final String MQTT_CONFIG_TAG_IMPLEMENTATION_CLASS = "MqttImplementationClass";
    private static final String MQTT_CONFIG_TAG_ENABLED = "MqttEnabled";
    private static final String MQTT_CONFIG_TAG_QOS = "MqttQos";
    private static final String MQTT_CONFIG_TAG_PORT = "MqttPort";
    private static final String MQTT_CONFIG_TAG_HOST = "MqttHost";
    private static final String MQTT_CONFIG_TAG_WEBSOCKET_PORT = "MqttWebsocketPort";
    public static boolean useAbsoluteNavigationLinks;

    private static Properties dbProperties;

    public static Properties getDbProperties() {
        return dbProperties;
    }

    private static synchronized Properties initDbProperties(ServletContext sc) {
        if (dbProperties == null) {
            dbProperties = new Properties();
            Enumeration<String> names = sc.getInitParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                dbProperties.put(name, sc.getInitParameter(name));
            }
        }
        return dbProperties;
    }

    private MqttSettings getMqttSettings(ServletContext sc) {
        MqttSettings settings = new MqttSettings(sc.getInitParameter(MQTT_CONFIG_TAG_IMPLEMENTATION_CLASS));
        settings.setTempPath(sc.getAttribute(ServletContext.TEMPDIR).toString());
        String enableMqtt = sc.getInitParameter(MQTT_CONFIG_TAG_ENABLED);
        if (enableMqtt != null) {
            settings.setEnableMqtt(Boolean.valueOf(enableMqtt));
        }
        String qos = sc.getInitParameter(MQTT_CONFIG_TAG_QOS);
        if (qos != null) {
            try {
                settings.setQosLevel(Integer.parseInt(qos));
            } catch (NumberFormatException e) {
                LOGGER.error("Could not parse mqtt qos value. Not a number: " + qos, e);
            }
        }
        String port = sc.getInitParameter(MQTT_CONFIG_TAG_PORT);
        if (port != null) {
            try {
                settings.setPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                LOGGER.error("Could not parse mqtt port value. Not a number: " + port, e);
            }
        }
        String host = sc.getInitParameter(MQTT_CONFIG_TAG_HOST);
        if (host != null) {
            settings.setHost(host);
        }
        String websocketPort = sc.getInitParameter(MQTT_CONFIG_TAG_WEBSOCKET_PORT);
        if (websocketPort != null) {
            try {
                settings.setWebsocketPort(Integer.parseInt(websocketPort));
            } catch (NumberFormatException e) {
                LOGGER.error("Could not parse mqtt websocket port value. Not a number: " + websocketPort, e);
            }
        }
        settings.setTopicPrefix(API_VERSION + "/");
        return settings;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        if (sce != null && context != null) {
            initDbProperties(context);
            String serviceRootUrl = context.getInitParameter("serviceRootUrl") + context.getContextPath() + "/" + API_VERSION;
            if (context.getInitParameter(USE_ABSOLUTE_NAVIGATION_LINKS_TAG) != null) {
                useAbsoluteNavigationLinks = Boolean.parseBoolean(context.getInitParameter(USE_ABSOLUTE_NAVIGATION_LINKS_TAG)
                );
            }
            PersistenceManagerFactory.init(getDbProperties());
            MqttManager.init(serviceRootUrl, getMqttSettings(context));
            PersistenceManagerFactory.addEntityChangeListener(MqttManager.getInstance());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MqttManager.shutdown();
    }

}
