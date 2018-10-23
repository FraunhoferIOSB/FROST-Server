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

import de.fraunhofer.iosb.ilt.frostserver.http.common.AbstractContextListener;
import de.fraunhofer.iosb.ilt.sta.messagebus.MessageBusFactory;
import de.fraunhofer.iosb.ilt.sta.mqtt.MqttManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 *
 * @author jab, scf
 */
@WebListener
public class ContextListener extends AbstractContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);

        if (sce != null && sce.getServletContext() != null) {
            MqttManager.init(getCoreSettings());
            MessageBusFactory.getMessageBus().addMessageListener(MqttManager.getInstance());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MqttManager.shutdown();
        super.contextDestroyed(sce);
    }

}
