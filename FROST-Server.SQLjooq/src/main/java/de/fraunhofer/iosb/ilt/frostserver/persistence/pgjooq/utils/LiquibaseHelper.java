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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSetStatus;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.SearchPathResourceAccessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper functions for Liquibase.
 *
 * @author scf
 */
public class LiquibaseHelper {

    public static final String CHANGE_SET_NAME = "changeSetName";

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
        try (SearchPathResourceAccessor resourceAccessor = new SearchPathResourceAccessor(searchPath)) {
            resourceAccessor.addResourceAccessor(new ClassLoaderResourceAccessor());
            runLiquibaseCheck(liquibaseChangelogFilename, resourceAccessor, database, params, out);
        } catch (Exception ex) {
            LOGGER.warn("Failed to close SearchPathResourceAccessor.", ex);
        }
    }

    private static void runLiquibaseCheck(String liquibaseChangelogFilename, final SearchPathResourceAccessor resourceAccessor, Database database, Map<String, Object> params, StringWriter out) {
        String changeSetName = Objects.toString(params.get(CHANGE_SET_NAME), "Unnamed Changeset");
        try (Liquibase liquibase = new Liquibase(liquibaseChangelogFilename, resourceAccessor, database)) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }
            final List<ChangeSetStatus> changeSetStatuses = liquibase.getChangeSetStatuses(new Contexts(), new LabelExpression(liquibaseChangelogFilename));
            int toRunCount = 0;
            for (ChangeSetStatus status : changeSetStatuses) {
                if (status.getWillRun()) {
                    toRunCount++;
                    String[] actions = StringUtils.stripAll(StringUtils.split(status.getDescription(), ';'));
                    out.append(status.getChangeSet().getId()).append('\n');
                    for (String action : actions) {
                        out.append('\t').append(action).append('\n');
                    }
                    out.append('\n');
                }
            }
            if (toRunCount == 0) {
                out.append("Up to date, no changes to apply: " + changeSetName + ".\n");
            }
        } catch (LiquibaseException ex) {
            outputError(ex, out, "Failed to upgrade database: " + changeSetName + ".");
        } catch (Exception ex) {
            LOGGER.warn("Exception happened when closing liquibase.", ex);
        }
    }

    private static void runLiquibaseUpdate(String liquibaseChangelogFilename, Map<String, Object> params, Connection connection, Writer out) throws UpgradeFailedException, IOException, DatabaseException {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        final String liquibaseSearchPath = Objects.toString(params.get("searchPath"), null);
        try (SearchPathResourceAccessor resourceAccessor = new SearchPathResourceAccessor(liquibaseSearchPath)) {
            resourceAccessor.addResourceAccessor(new ClassLoaderResourceAccessor());
            runLiquibaseUpdate(liquibaseChangelogFilename, resourceAccessor, database, params, out);
        } catch (Exception ex) {
            LOGGER.warn("Failed to close SearchPathResourceAccessor.", ex);
        }
    }

    private static void runLiquibaseUpdate(String liquibaseChangelogFilename, final SearchPathResourceAccessor resourceAccessor, Database database, Map<String, Object> params, Writer out) throws UpgradeFailedException, IOException {
        String changeSetName = Objects.toString(params.get(CHANGE_SET_NAME), "Unnamed Changeset");
        try (Liquibase liquibase = new Liquibase(liquibaseChangelogFilename, resourceAccessor, database)) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }
            liquibase.update(new Contexts(), new LabelExpression(liquibaseChangelogFilename));
            out.append("Update Completed: " + changeSetName + ".");
        } catch (LiquibaseException ex) {
            outputError(ex, out, "Failed to upgrade database: " + changeSetName + ".");
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
