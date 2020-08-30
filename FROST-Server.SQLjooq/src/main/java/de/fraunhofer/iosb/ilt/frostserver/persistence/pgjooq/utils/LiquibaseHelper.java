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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper functions for Liquibase.
 *
 * @author scf
 */
public class LiquibaseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseHelper.class.getName());

    private LiquibaseHelper() {
        // Utility class, should not be instantiated.
    }

    public static String checkForUpgrades(Connection connection, String liquibaseChangelogFilename) {
        StringWriter out = new StringWriter();
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            runLiquibaseCheck(liquibaseChangelogFilename, database, out);
        } catch (DatabaseException ex) {
            outputError(ex, out, "Failed to initialise database");
        }
        return out.toString();
    }

    public static boolean doUpgrades(Connection connection, String liquibaseChangelogFilename, Writer out) throws UpgradeFailedException, IOException {
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            runLiquibaseUpdate(liquibaseChangelogFilename, database, out);
        } catch (DatabaseException ex) {
            outputError(ex, out, "Failed to initialise database");
            return false;
        }
        return true;
    }

    private static void runLiquibaseCheck(String liquibaseChangelogFilename, Database database, StringWriter out) {
        try (Liquibase liquibase = new Liquibase(liquibaseChangelogFilename, new ClassLoaderResourceAccessor(), database)) {
            liquibase.update(new Contexts(), out);
        } catch (LiquibaseException ex) {
            outputError(ex, out, "Failed to upgrade database");
        } catch (Exception ex) {
            LOGGER.warn("Exception happened when closing liquibase.", ex);
        }
    }

    private static void runLiquibaseUpdate(String liquibaseChangelogFilename, Database database, Writer out) throws UpgradeFailedException, IOException {
        try (Liquibase liquibase = new Liquibase(liquibaseChangelogFilename, new ClassLoaderResourceAccessor(), database)) {
            liquibase.update(new Contexts());
        } catch (LiquibaseException ex) {
            outputError(ex, out, "Failed to upgrade database");
            throw new UpgradeFailedException(ex);
        } catch (Exception ex) {
            LOGGER.warn("Exception happened when closing liquibase.", ex);
        }
    }

    private static void outputError(final Exception exception, final StringWriter out, final String message) {
        try {
            outputError(exception, (Writer) out, message);
        } catch (IOException exc) {
            LOGGER.error("Error writing output.", exc);
            // Never happens.
        }
    }

    private static void outputError(final Exception exception, final Writer out, final String message) throws IOException {
        LOGGER.error(message, exception);
        out.append(message + ":\n");
        out.append(exception.getLocalizedMessage());
        out.append("\n");
    }

}
