/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;

/**
 * An interface for components that want to be called when it is time to upgrade
 * the database.
 *
 * @author scf
 */
public interface LiquibaseUser {

    /**
     * Initialise the LiquibaseUser so it can check for, or do upgrades.
     *
     * @param coreSettings The settings to use for initialisation.
     */
    public void init(CoreSettings coreSettings);

    /**
     * Give a summary of any upgrades that need to be done to the storage
     * backend.
     *
     * @return A human readable text summarising the upgrades that need to be
     * done to the storage backend.
     */
    public String checkForUpgrades();

    /**
     * Upgrade the storage backend.
     *
     * @param out The Writer to append logging messages to.
     * @return true if the upgrade was successful, false if upgrade should be
     * tried again later.
     * @throws de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException when
     * upgrading fails and should not be attempted again at a later stage.
     * @throws java.io.IOException when the Writer throws this exception.
     */
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException;

}
