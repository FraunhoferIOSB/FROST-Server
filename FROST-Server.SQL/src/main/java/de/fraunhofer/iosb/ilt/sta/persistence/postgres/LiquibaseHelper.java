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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres;

import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager.LOGGER;
import de.fraunhofer.iosb.ilt.sta.util.UpgradeFailedException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Helper functions for Liquibase.
 *
 * @author scf
 */
public class LiquibaseHelper {

    private LiquibaseHelper() {
        // Utility class, should not be instantiated.
    }

    public static String checkForUpgrades(Connection connection, String liquibaseChangelogFilename) {
        StringWriter out = new StringWriter();
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase(liquibaseChangelogFilename, new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), out);
            database.commit();
            database.close();
            connection.close();

        } catch (SQLException | DatabaseException ex) {
            LOGGER.error("Could not initialise database.", ex);
            out.append("Failed to initialise database:\n");
            out.append(ex.getLocalizedMessage());
            out.append("\n");
        } catch (LiquibaseException ex) {
            LOGGER.error("Could not upgrade database.", ex);
            out.append("Failed to upgrade database:\n");
            out.append(ex.getLocalizedMessage());
            out.append("\n");
        }
        return out.toString();
    }

    public static boolean doUpgrades(Connection connection, String liquibaseChangelogFilename, Writer out) throws UpgradeFailedException, IOException {
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase(liquibaseChangelogFilename, new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts());
            database.commit();
            database.close();
            connection.close();

        } catch (SQLException | DatabaseException ex) {
            LOGGER.error("Could not initialise database.", ex);
            out.append("Failed to initialise database:\n");
            out.append(ex.getLocalizedMessage());
            out.append("\n");
            return false;

        } catch (LiquibaseException ex) {
            out.append("Failed to upgrade database:\n");
            out.append(ex.getLocalizedMessage());
            out.append("\n");
            throw new UpgradeFailedException(ex);
        }
        return true;
    }

}
