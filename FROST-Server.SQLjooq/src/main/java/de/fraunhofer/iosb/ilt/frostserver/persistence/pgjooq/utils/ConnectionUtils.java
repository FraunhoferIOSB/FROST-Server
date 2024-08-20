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

import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class ConnectionUtils implements ConfigDefaults {

    @DefaultValue("public")
    public static final String TAG_DB_SCHEMA_PRIORITY = "db.schemaPriority";

    @DefaultValue("")
    public static final String TAG_DATA_SOURCE = "db.jndi.datasource";
    @DefaultValue("")
    public static final String TAG_DB_DRIVER = "db.driver";
    @DefaultValue("")
    public static final String TAG_DB_URL = "db.url";
    @DefaultValue("")
    public static final String TAG_DB_USERNAME = "db.username";
    @DefaultValue("")
    public static final String TAG_DB_PASSWRD = "db.password";

    // Default values supplied by driver, not here.
    public static final String TAG_DB_MAXCONN = "db.conn.max";
    public static final String TAG_DB_MAXIDLE = "db.conn.idle.max";
    public static final String TAG_DB_MINIDLE = "db.conn.idle.min";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionUtils.class);

    private static final Map<String, DataSource> EXISTING_POOLS = new HashMap<>();

    private ConnectionUtils() {
        // Utility class, should not be instantiated.
    }

    public static Connection getConnection(String name, Settings settings) throws SQLException {
        Connection connection = getPoolingConnection(name, settings);
        connection.setAutoCommit(false);
        return connection;
    }

    /**
     * Creates a connection, setting up a new pool if needed.
     *
     * @param name The name to use for the source
     * @param settings The settings, must contain the options for db driver, db
     * url and username/password.
     * @return A pooled database connection.
     * @throws SQLException when there is a problem.
     */
    public static Connection getPoolingConnection(String name, Settings settings) throws SQLException {
        DataSource source = EXISTING_POOLS.get(name);
        if (source == null) {
            source = createPoolingConnection(name, settings);
        }
        return source.getConnection();
    }

    private static DataSource createPoolingConnection(String name, Settings settings) {
        synchronized (EXISTING_POOLS) {
            DataSource source = EXISTING_POOLS.get(name);
            if (source == null) {
                if (!settings.get(TAG_DB_URL, ConnectionUtils.class, false).isEmpty()) {
                    source = setupBasicDataSource(settings);
                } else {
                    source = setupDataSource(settings);
                }
                EXISTING_POOLS.put(name, source);
            }
            return source;
        }
    }

    private static DataSource setupBasicDataSource(Settings settings) {
        LOGGER.info("Setting up BasicDataSource for database connections.");
        String driver = settings.get(TAG_DB_DRIVER, ConnectionUtils.class);
        if (driver.isEmpty()) {
            throw new IllegalArgumentException("Property '" + TAG_DB_DRIVER + "' must be non-empty");
        }
        try {
            Class.forName(driver);
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl(settings.get(TAG_DB_URL, ConnectionUtils.class, false));
            ds.setUsername(settings.get(TAG_DB_USERNAME, ConnectionUtils.class));
            ds.setPassword(settings.get(TAG_DB_PASSWRD, ConnectionUtils.class, false));
            ds.setMaxIdle(settings.getInt(TAG_DB_MAXIDLE, ds.getMaxIdle()));
            ds.setMaxTotal(settings.getInt(TAG_DB_MAXCONN, ds.getMaxTotal()));
            ds.setMinIdle(settings.getInt(TAG_DB_MINIDLE, ds.getMinIdle()));
            return ds;
        } catch (ClassNotFoundException exc) {
            throw new IllegalArgumentException(exc);
        }
    }

    private static DataSource setupDataSource(Settings settings) {
        LOGGER.info("Setting up DataSource for database connections.");
        try {
            String dataSourceName = settings.get(TAG_DATA_SOURCE, ConnectionUtils.class);
            if (dataSourceName.isEmpty()) {
                throw new IllegalArgumentException("Setting " + TAG_DATA_SOURCE + " must not be empty.");
            }
            InitialContext cxt = new InitialContext();
            DataSource ds = (DataSource) cxt.lookup("java:/comp/env/" + dataSourceName);
            if (ds == null) {
                throw new IllegalStateException("Data source not found!");
            }
            return ds;
        } catch (NamingException exc) {
            throw new IllegalArgumentException("Failed to load context.", exc);
        }
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

    public static class ConnectionWrapper implements Supplier<Connection>, AutoCloseable {

        private final Settings settings;
        private final String connectionName;
        private Connection connection;

        public ConnectionWrapper(Settings settings, String connectionName) {
            this.settings = settings;
            this.connectionName = connectionName;
        }

        @Override
        public Connection get() {
            if (connection == null) {
                try {
                    connection = ConnectionUtils.getConnection(connectionName, settings);
                } catch (SQLException ex) {
                    LOGGER.error("Could not inizialize {}", getClass().getName(), ex);
                }
            }
            return connection;
        }

        public boolean commit() {
            if (connection == null) {
                return true;
            }
            try {
                if (!connection.isClosed()) {
                    connection.commit();
                    return true;
                }
            } catch (SQLException ex) {
                LOGGER.error("Exception rolling back.", ex);
            }
            return false;
        }

        public boolean rollback() {
            if (connection == null) {
                return true;
            }
            try {
                if (!connection.isClosed()) {
                    LOGGER.debug("Rolling back changes.");
                    connection.rollback();
                    return true;
                }
            } catch (SQLException ex) {
                LOGGER.error("Exception rolling back.", ex);
            }
            return false;
        }

        @Override
        public void close() throws SQLException {
            if (connection == null) {
                return;
            }
            try {
                connection.close();
            } finally {
                clear();
            }
        }

        private void clear() {
            connection = null;
        }

    }

}
