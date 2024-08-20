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
package de.fraunhofer.iosb.ilt.frostserver.messagebus;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;

/**
 *
 * @author scf
 */
public interface MessageBus {

    public void init(CoreSettings settings);

    /**
     * Stop the bus and clean up any worker threads.
     */
    public void stop();

    /**
     * Offer an event message to be sent over the bus. This should return
     * quickly.
     *
     * @param message the message to send.
     */
    public void sendMessage(EntityChangedMessage message);

    public void addMessageListener(MessageListener listener);

    public void removeMessageListener(MessageListener listener);
}
