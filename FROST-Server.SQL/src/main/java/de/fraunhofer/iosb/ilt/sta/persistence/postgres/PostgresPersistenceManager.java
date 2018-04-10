/*
 * Copyright (C) 2017 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager.TAG_DATA_SOURCE;
import de.fraunhofer.iosb.ilt.sta.settings.Settings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public interface PostgresPersistenceManager {

    public static final String TAG_DATA_SOURCE = "db.jndi.datasource";
    public static final String TAG_DB_DRIVER = "db.driver";
    public static final String TAG_DB_URL = "db.url";
    public static final String TAG_DB_USERNAME = "db.username";
    public static final String TAG_DB_PASSWORD = "db.password";

    public static final DateTime DATETIME_MAX = DateTime.parse("9999-12-31T23:59:59.999Z");
    public static final DateTime DATETIME_MIN = DateTime.parse("-4000-01-01T00:00:00.000Z");

    /**
     * The logger for this class.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(PostgresPersistenceManager.class);

    static Map<String, ConnectionSource> existingPools = new HashMap<>();

    /**
     * Creates a connection, setting up a new pool if needed.
     *
     * @param name The name to use for the source
     * @param settings The settings, must contain the options for db driver, db
     * url and username/password.
     * @return A pooled database connection.
     * @throws SQLException
     */
    public static Connection getPoolingConnection(String name, Settings settings) throws SQLException {
        ConnectionSource source = existingPools.get(name);
        if (source == null) {
            source = createPoolingConnection(name, settings);
        }
        return source.getConnection();
    }

    static ConnectionSource createPoolingConnection(String name, Settings settings) throws SQLException {
        synchronized (existingPools) {
            ConnectionSource source = existingPools.get(name);
            if (source == null) {
                if (settings.containsName(TAG_DATA_SOURCE)) {
                    source = setupDataSource(settings);
                } else {
                    source = setupDriverSource(name, settings);
                }
                existingPools.put(name, source);
            }
            return source;
        }
    }

    static ConnectionSource setupDataSource(Settings settings) {
        LOGGER.info("Setting up DataSource for database connections.");
        try {
            String dataSourceName = settings.getWithDefault(TAG_DATA_SOURCE, "", String.class);
            if (dataSourceName.isEmpty()) {
                throw new IllegalArgumentException("Setting " + TAG_DATA_SOURCE + " must not be empty.");
            }
            InitialContext cxt = new InitialContext();
            if (cxt == null) {
                throw new IllegalStateException("No context!");
            }

            DataSource ds = (DataSource) cxt.lookup("java:/comp/env/" + dataSourceName);
            if (ds == null) {
                throw new IllegalStateException("Data source not found!");
            }
            return new ConnectionSourceDataSource(ds);
        } catch (NamingException exc) {
            throw new IllegalArgumentException("Failed to load context.", exc);
        }
    }

    static ConnectionSource setupDriverSource(String name, Settings settings) {
        LOGGER.info("Setting up Driver for database connections.");
        String driver = settings.getWithDefault(TAG_DB_DRIVER, "", String.class);
        if (driver.isEmpty()) {
            throw new IllegalArgumentException("Property '" + TAG_DB_DRIVER + "' must be non-empty");
        }
        try {
            Class.forName(settings.get(TAG_DB_DRIVER));
            setupPoolingDriver(
                    name,
                    settings.get(TAG_DB_URL),
                    settings.get(TAG_DB_USERNAME),
                    settings.get(TAG_DB_PASSWORD));
        } catch (ClassNotFoundException | SQLException exc) {
            LOGGER.error("Failed to set up a Connection pool for the database.", exc);
            throw new IllegalArgumentException(exc);
        }
        return new ConnectionSourceDriverManager("jdbc:apache:commons:dbcp:" + name);
    }

    /**
     * Set up a connection pool. The driver used in the connection URI should
     * already be loaded using
     * Class.forName("org.apache.commons.dbcp2.PoolingDriver"); After calling
     * this you can use "jdbc:apache:commons:dbcp:FROST-Pool" to connect.
     *
     * @param name The name of the pool to create.
     * @param connectURI The URL of the database to connect to.
     * @param username The username to use when connecting to the database.
     * @param password The password to use when connecting to the database.
     * @throws ClassNotFoundException If the PoolingDriver is not on the
     * classpath.
     * @throws SQLException If the dbcp driver could not be loaded.
     */
    public static void setupPoolingDriver(String name, String connectURI, String username, String password) throws ClassNotFoundException, SQLException {
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, username, password);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(connectionPool);
        Class.forName("org.apache.commons.dbcp2.PoolingDriver");
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.registerPool(name, connectionPool);
    }

    static interface ConnectionSource {

        public Connection getConnection() throws SQLException;
    }

    static class ConnectionSourceDataSource implements ConnectionSource {

        private final DataSource ds;

        public ConnectionSourceDataSource(DataSource ds) {
            this.ds = ds;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return ds.getConnection();
        }
    }

    static class ConnectionSourceDriverManager implements ConnectionSource {

        private final String name;

        public ConnectionSourceDriverManager(String name) {
            this.name = name;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(name);
        }

    }
}
