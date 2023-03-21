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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.StringWriter;
import org.slf4j.Logger;

/**
 *
 * @author scf
 */
public class LiquibaseUtils {

    private LiquibaseUtils() {
        // Utility class, not to be instantiated.
    }

    /**
     * Tries to upgrade the database of the given LiquibaseUser. returns true if
     * the upgrade should be tried again later, and false if the upgrade
     * succeeds or fails permanently.
     *
     * @param logger The logger to log messages to.
     * @param user The liquibase user to try to upgrade.
     * @return true if the upgrade should be tried again.
     */
    public static boolean maybeUpdateDatabase(Logger logger, LiquibaseUser user) {
        boolean retry = false;
        StringWriter updateLog = new StringWriter();
        try {
            logger.info("Running database update for {}", user.getClass().getName());
            boolean success = user.doUpgrades(updateLog);
            retry = !success;
            if (success) {
                logger.info("Database-update successful.");
            } else {
                logger.info("Database-update not successful, trying again later.");
            }
        } catch (UpgradeFailedException ex) {
            logger.error("Database upgrade failed.", ex);
            retry = false;
        } catch (IOException ex) {
            // Should not happen, StringWriter does not throw IOExceptions.
            logger.error("Database upgrade failed.", ex);
        }
        String logString = updateLog.toString();
        if (!logString.isEmpty()) {
            logger.info("Database-update-log:\n{}", logString);
        }
        return retry;
    }

}
