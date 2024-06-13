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
package de.fraunhofer.iosb.ilt.frostserver;

import de.fraunhofer.iosb.ilt.frostserver.http.common.AbstractContextListener;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBusFactory;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * @author jab, scf
 */
@WebListener
public class ContextListener extends AbstractContextListener {

    MqttManager mqttManager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        super.contextInitialized(sce);

        if (sce != null && sce.getServletContext() != null) {
            final CoreSettings coreSettings = getCoreSettings();
            mqttManager = new MqttManager(coreSettings);
            MessageBusFactory.createMessageBus(coreSettings);
            coreSettings.getMessageBus().addMessageListener(mqttManager);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (mqttManager != null) {
            mqttManager.shutdown();
        }
        super.contextDestroyed(sce);
    }

}
