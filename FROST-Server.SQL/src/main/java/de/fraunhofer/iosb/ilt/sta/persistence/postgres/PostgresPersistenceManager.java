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

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.spatial.PostGISTemplates;
import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.persistence.AbstractPersistenceManager;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager.TAG_DATA_SOURCE;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.settings.Settings;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.sta.util.UpgradeFailedException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Provider;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;
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
 * @author scf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class PostgresPersistenceManager<I extends SimpleExpression<J> & Path<J>, J> extends AbstractPersistenceManager {

    public static final String TAG_DATA_SOURCE = "db.jndi.datasource";
    public static final String TAG_DB_DRIVER = "db.driver";
    public static final String TAG_DB_URL = "db.url";
    public static final String TAG_DB_USERNAME = "db.username";
    public static final String TAG_DB_PASSWRD = "db.password";
    public static final String TAG_DB_MAXCONN = "db.conn.max";
    public static final String TAG_DB_MAXIDLE = "db.conn.idle.max";
    public static final String TAG_DB_MINIDLE = "db.conn.idle.min";

    public static final DateTime DATETIME_MAX = DateTime.parse("9999-12-31T23:59:59.999Z");
    public static final DateTime DATETIME_MIN = DateTime.parse("-4000-01-01T00:00:00.000Z");

    /**
     * The logger for this class.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(PostgresPersistenceManager.class);

    static Map<String, ConnectionSource> existingPools = new HashMap<>();

    private static class MyConnectionWrapper implements Provider<Connection> {

        private final CoreSettings settings;
        private Connection connection;

        public MyConnectionWrapper(CoreSettings settings) {
            this.settings = settings;
        }

        @Override
        public Connection get() {
            if (connection == null) {
                try {
                    connection = getConnection(settings);
                } catch (SQLException ex) {
                    LOGGER.error("Could not inizialize " + getClass().getName(), ex);
                }
            }
            return connection;
        }

        protected boolean doCommit() {
            if (connection == null) {
                return true;
            }
            try {
                if (!get().isClosed()) {
                    get().commit();
                    return true;
                }
            } catch (SQLException ex) {
                LOGGER.error("Exception rolling back.", ex);
            }
            return false;
        }

        protected boolean doRollback() {
            if (connection == null) {
                return true;
            }
            try {
                if (!get().isClosed()) {
                    LOGGER.debug("Rolling back changes.");
                    get().rollback();
                    return true;
                }
            } catch (SQLException ex) {
                LOGGER.error("Exception rolling back.", ex);
            }
            return false;
        }

        protected boolean doClose() {
            if (connection == null) {
                return true;
            }
            try {
                get().close();
                return true;
            } catch (SQLException ex) {
                LOGGER.error("Exception closing.", ex);
            } finally {
                clear();
            }
            return false;
        }

        public void clear() {
            connection = null;
        }

    }

    private CoreSettings settings;
    private MyConnectionWrapper connectionProvider;
    private SQLQueryFactory queryFactory;

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        connectionProvider = new MyConnectionWrapper(settings);
    }

    @Override
    public CoreSettings getCoreSettings() {
        return settings;
    }

    public abstract EntityFactories<I, J> getEntityFactories();

    public abstract IdGenerationHandler createIdGenerationHanlder(Entity e);

    public SQLQueryFactory createQueryFactory() {
        if (queryFactory == null) {
            SQLTemplates templates = PostGISTemplates.builder().quote().build();
            queryFactory = new SQLQueryFactory(templates, connectionProvider);
        }
        return queryFactory;
    }

    public abstract PropertyResolver<I, J> getPropertyResolver();

    public abstract String getLiquibaseChangelogFilename();

    public long count(ResourcePath path, Query query) {
        SQLQueryFactory qf = createQueryFactory();
        PathSqlBuilderImp psb = new PathSqlBuilderImp(getPropertyResolver());
        SQLQuery<Tuple> sqlQuery = psb.buildFor(path, query, qf, getCoreSettings().getPersistenceSettings());
        return sqlQuery.fetchCount();
    }

    @Override
    public boolean validatePath(ResourcePath path) {
        ResourcePathElement element = path.getIdentifiedElement();
        if (element == null) {
            return true;
        }
        ResourcePath tempPath = new ResourcePath();
        List<ResourcePathElement> elements = tempPath.getPathElements();
        while (element != null) {
            elements.add(0, element);
            element = element.getParent();
        }
        return new EntityInserter(this).entityExists(tempPath);
    }

    @Override
    public Entity get(EntityType entityType, Id id) {
        SQLQueryFactory qf = createQueryFactory();
        PathSqlBuilder psb = new PathSqlBuilderImp(getPropertyResolver());
        SQLQuery<Tuple> sqlQuery = psb.buildFor(entityType, id, qf, getCoreSettings().getPersistenceSettings());
        sqlQuery.limit(2);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Generated SQL:\n{}", sqlQuery.getSQL().getSQL());
        }
        List<Tuple> results = sqlQuery.fetch();

        EntityFromTupleFactory<? extends Entity, I, J> factory;
        factory = getEntityFactories().getFactoryFor(entityType.getImplementingClass());
        return factory.create(results.get(0), null, new DataSize());
    }

    @Override
    public Object get(ResourcePath path, Query query) {
        ResourcePathElement lastElement = path.getLastElement();
        if (!(lastElement instanceof EntityPathElement) && !(lastElement instanceof EntitySetPathElement)) {
            if (!query.getExpand().isEmpty()) {
                LOGGER.warn("Expand only allowed on Entities or EntitySets. Not on {}!", lastElement.getClass());
                query.getExpand().clear();
            }
            if (!query.getSelect().isEmpty()) {
                LOGGER.warn("Select only allowed on Entities or EntitySets. Not on {}!", lastElement.getClass());
                query.getSelect().clear();
            }
        }

        SQLQueryFactory qf = createQueryFactory();
        PathSqlBuilderImp psb = new PathSqlBuilderImp(getPropertyResolver());
        SQLQuery<Tuple> sqlQuery = psb.buildFor(path, query, qf, getCoreSettings().getPersistenceSettings());

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Generated SQL:\n{}", sqlQuery.getSQL().getSQL());
        }

        EntityCreator entityCreator = new EntityCreator(this, path, query, sqlQuery);
        lastElement.visit(entityCreator);
        Object entity = entityCreator.getEntity();

        if (path.isValue() && entity instanceof Map) {
            Map map = (Map) entity;
            entity = map.get(entityCreator.getEntityName());
        }

        return entity;
    }

    @Override
    public boolean doInsert(Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        EntityInserter ei = new EntityInserter(this);
        switch (entity.getEntityType()) {
            case DATASTREAM:
                ei.insertDatastream((Datastream) entity);
                break;

            case MULTIDATASTREAM:
                ei.insertMultiDatastream((MultiDatastream) entity);
                break;

            case FEATUREOFINTEREST:
                ei.insertFeatureOfInterest((FeatureOfInterest) entity);
                break;

            case HISTORICALLOCATION:
                ei.insertHistoricalLocation((HistoricalLocation) entity);
                break;

            case LOCATION:
                ei.insertLocation((Location) entity);
                break;

            case OBSERVATION:
                ei.insertObservation((Observation) entity);
                break;

            case OBSERVEDPROPERTY:
                ei.insertObservedProperty((ObservedProperty) entity);
                break;

            case SENSOR:
                ei.insertSensor((Sensor) entity);
                break;

            case THING:
                ei.insertThing((Thing) entity);
                break;

            default:
                throw new IllegalStateException("Unknown entity type: " + entity.getEntityType().name());

        }
        return true;
    }

    @Override
    public EntityChangedMessage doUpdate(EntityPathElement pathElement, Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        EntityInserter ei = new EntityInserter(this);
        entity.setId(pathElement.getId());
        J id = (J) pathElement.getId().getValue();
        if (!ei.entityExists(entity)) {
            throw new NoSuchEntityException("No entity of type " + pathElement.getEntityType() + " with id " + id);
        }
        EntityType type = pathElement.getEntityType();
        EntityChangedMessage message;
        switch (type) {
            case DATASTREAM:
                message = ei.updateDatastream((Datastream) entity, id);
                break;

            case MULTIDATASTREAM:
                message = ei.updateMultiDatastream((MultiDatastream) entity, id);
                break;

            case FEATUREOFINTEREST:
                message = ei.updateFeatureOfInterest((FeatureOfInterest) entity, id);
                break;

            case HISTORICALLOCATION:
                message = ei.updateHistoricalLocation((HistoricalLocation) entity, id);
                break;

            case LOCATION:
                message = ei.updateLocation((Location) entity, id);
                break;

            case OBSERVATION:
                message = ei.updateObservation((Observation) entity, id);
                break;

            case OBSERVEDPROPERTY:
                message = ei.updateObservedProperty((ObservedProperty) entity, id);
                break;

            case SENSOR:
                message = ei.updateSensor((Sensor) entity, id);
                break;

            case THING:
                message = ei.updateThing((Thing) entity, id);
                break;

            default:
                throw new AssertionError(type.name());

        }

        return message;
    }

    @Override
    public void doDelete(ResourcePath path, Query query) {
        query.setSelect(Arrays.asList(EntityProperty.ID));
        SQLQueryFactory qf = createQueryFactory();
        PathSqlBuilderImp psb = new PathSqlBuilderImp(getPropertyResolver());

        SQLQuery<Tuple> sqlQuery = psb.buildFor(path, query, qf, getCoreSettings().getPersistenceSettings());
        SQLDeleteClause sqlDelete = psb.createDelete((EntitySetPathElement) path.getLastElement(), qf, sqlQuery);

        long rowCount = sqlDelete.execute();
        LOGGER.debug("Deleted {} rows using query {}", rowCount, sqlDelete);
    }

    @Override
    protected boolean doCommit() {
        return connectionProvider.doCommit();
    }

    @Override
    protected boolean doRollback() {
        return connectionProvider.doRollback();
    }

    @Override
    protected boolean doClose() {
        return connectionProvider.doClose();
    }

    @Override
    public String checkForUpgrades() {
        StringWriter out = new StringWriter();
        try {
            Connection connection = getConnection(getCoreSettings());

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase(getLiquibaseChangelogFilename(), new ClassLoaderResourceAccessor(), database);
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

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        try {
            Connection connection = getConnection(getCoreSettings());

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase(getLiquibaseChangelogFilename(), new ClassLoaderResourceAccessor(), database);
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

    public static Connection getConnection(CoreSettings settings) throws SQLException {
        Settings customSettings = settings.getPersistenceSettings().getCustomSettings();
        Connection connection = PostgresPersistenceManager.getPoolingConnection("FROST-Source", customSettings);
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
     * @throws SQLException
     */
    public static Connection getPoolingConnection(String name, Settings settings) throws SQLException {
        ConnectionSource source = existingPools.get(name);
        if (source == null) {
            source = createPoolingConnection(name, settings);
        }
        return source.getConnection();
    }

    static ConnectionSource createPoolingConnection(String name, Settings settings) {
        synchronized (existingPools) {
            ConnectionSource source = existingPools.get(name);
            if (source == null) {
                if (settings.containsName(TAG_DB_URL) && !settings.get(TAG_DB_URL).isEmpty()) {
                    source = setupBasicDataSource(settings);
                } else {
                    source = setupDataSource(settings);
                }
                existingPools.put(name, source);
            }
            return source;
        }
    }

    static ConnectionSource setupBasicDataSource(Settings settings) {
        LOGGER.info("Setting up BasicDataSource for database connections.");
        String driver = settings.getWithDefault(TAG_DB_DRIVER, "", String.class);
        if (driver.isEmpty()) {
            throw new IllegalArgumentException("Property '" + TAG_DB_DRIVER + "' must be non-empty");
        }
        try {
            Class.forName(settings.get(TAG_DB_DRIVER));
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl(settings.get(TAG_DB_URL));
            ds.setUsername(settings.get(TAG_DB_USERNAME));
            ds.setPassword(settings.get(TAG_DB_PASSWRD));
            ds.setMaxIdle(settings.getInt(TAG_DB_MAXIDLE, ds.getMaxIdle()));
            ds.setMaxTotal(settings.getInt(TAG_DB_MAXCONN, ds.getMaxTotal()));
            ds.setMinIdle(settings.getInt(TAG_DB_MINIDLE, ds.getMinIdle()));
            return new ConnectionSourceBasicDataSource(ds);
        } catch (ClassNotFoundException exc) {
            LOGGER.error("Failed to set up a Connection pool for the database.", exc);
            throw new IllegalArgumentException(exc);
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
                    settings.get(TAG_DB_PASSWRD));
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

    static class ConnectionSourceBasicDataSource implements ConnectionSource {

        private final BasicDataSource dataSource;

        public ConnectionSourceBasicDataSource(BasicDataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return dataSource.getConnection();
        }

    }

}
