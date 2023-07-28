/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.service;

import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;

/**
 * The interface that plugins must implement that want to change the data model.
 *
 * @author hylke
 */
public interface PluginModel extends Plugin {

    /**
     * Register bare EntityTypes without properties yet.
     */
    public void registerEntityTypes();

    /**
     * Link entityTypes to their Properties, and to the PersistenceManager.
     *
     * @param pm The PersistenceManager used to store entities.
     * @return true if registration was complete. False if another pass is
     * needed, after all other plugins have had a try.
     */
    public boolean linkEntityTypes(PersistenceManager pm);

    /**
     *
     * @return true if both {@link #registerEntityTypes()} and
     * {@link #linkEntityTypes(PersistenceManager)} have successfully executed.
     */
    public boolean isFullyInitialised();
}
