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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.SearchPathResourceAccessor;
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

    public static String checkForUpgrades(Connection connection, String liquibaseChangelogFilename, Map<String, Object> params) {
        StringWriter out = new StringWriter();
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            runLiquibaseCheck(liquibaseChangelogFilename, params, database, out);
        } catch (DatabaseException ex) {
            outputError(ex, out, "Failed to initialise database");
        }
        return out.toString();
    }

    public static boolean doUpgrades(Connection connection, String liquibaseChangelogFilename, Map<String, Object> params, Writer out) throws UpgradeFailedException, IOException {
        try {
            runLiquibaseUpdate(liquibaseChangelogFilename, params, connection, out);
        } catch (DatabaseException ex) {
            outputError(ex, out, "Failed to initialise database");
            return false;
        }
        return true;
    }

    private static void runLiquibaseCheck(String liquibaseChangelogFilename, Map<String, Object> params, Database database, StringWriter out) {
        final String searchPath = Objects.toString(params.get("searchPath"), null);
        final ResourceAccessor resourceAccessor = new SearchPathResourceAccessor(searchPath)
                .addResourceAccessor(new ClassLoaderResourceAccessor());
        try (Liquibase liquibase = new Liquibase(liquibaseChangelogFilename, resourceAccessor, database)) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }
            liquibase.update(new Contexts(), out);
        } catch (LiquibaseException ex) {
            outputError(ex, out, "Failed to upgrade database");
        } catch (Exception ex) {
            LOGGER.warn("Exception happened when closing liquibase.", ex);
        }
    }

    private static void runLiquibaseUpdate(String liquibaseChangelogFilename, Map<String, Object> params, Connection connection, Writer out) throws UpgradeFailedException, IOException, DatabaseException {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        final String liquibaseSearchPath = Objects.toString(params.get("searchPath"), null);
        final ResourceAccessor resourceAccessor = new SearchPathResourceAccessor(liquibaseSearchPath)
                .addResourceAccessor(new ClassLoaderResourceAccessor());
        try (Liquibase liquibase = new Liquibase(liquibaseChangelogFilename, resourceAccessor, database)) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }
            liquibase.update(new Contexts());
        } catch (LiquibaseException ex) {
            outputError(ex, out, "Failed to upgrade database");
            throw new UpgradeFailedException(ex);
        } catch (Exception ex) {
            LOGGER.warn("Exception happened when closing liquibase.", ex);
        }
    }

    public static String getSearchPath(Connection connection) {
        try (PreparedStatement call = connection.prepareStatement("show search_path")) {
            call.execute();
            try (ResultSet resultSet = call.getResultSet()) {
                if (resultSet.next()) {
                    final String searchPath = resultSet.getString(1);
                    LOGGER.debug("Found search_path: {}", searchPath);
                    return searchPath;
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("Failed to fetch search_path: ", ex);
        }
        return "";
    }

    public static void setSearchPath(Connection connection, String wantedSearchPath) {
        String searchPath = getSearchPath(connection);
        if (wantedSearchPath.equals(searchPath)) {
            return;
        }
        LOGGER.info("Liquibase changed the search_path from '{}' to '{}'. Changing it back.", wantedSearchPath, searchPath);
        try (PreparedStatement call = connection.prepareStatement("select set_config('search_path', ?, false)")) {
            call.setString(1, wantedSearchPath);
            call.execute();
            connection.commit();
        } catch (SQLException ex) {
            LOGGER.error("Failed to set search_path: ", ex);
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
        LOGGER.error(message, exception.getMessage());
        out.append(message + ":\n");
        out.append(exception.getLocalizedMessage());
        out.append("\n");
    }

}
