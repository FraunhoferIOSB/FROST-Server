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

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;

/**
 * An interface for classes that can configure a Filter on a ServletContext.
 */
public interface AuthProvider extends LiquibaseUser {

    public UserCaches getUserCaches();

    /**
     * Configure a new filter on the given context, using the settings from the
     * given CoreSettings.
     *
     * @param context The context to add a filter to. Must be a ServletContext.
     * @param coreSettings The settings to use.
     */
    public void addFilter(Object context, CoreSettings coreSettings);

    /**
     * Get the class name of the Filter class.
     *
     * @return the class name of the Filter class.
     */
    public String getFilterClassName();

    /**
     * Check if a user exists that has the given username and the given
     * password.
     *
     * @param clientId The clientId used in MQTT.
     * @param username The username of the user to check.
     * @param password The password of the user to check.
     * @return True if a user with the given username and password exists, false
     * otherwise.
     */
    public boolean isValidUser(String clientId, String username, String password);

}
