/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.spatial.PostGISTemplates;
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
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.persistence.AbstractPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.QDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.QFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.QHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.QLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.QLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.QMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.QMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.QObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.QObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.QSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.QThings;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.settings.Settings;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.inject.Provider;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class PostgresPersistenceManager extends AbstractPersistenceManager {

    /**
     * Custom Settings | Tags
     */
    private static final String TAG_DATA_SOURCE = "db_jndi_datasource";
    private static final String TAG_DB_DRIVER = "db_driver";
    private static final String TAG_DB_URL = "db_url";
    private static final String TAG_DB_USERNAME = "db_username";
    private static final String TAG_DB_PASSWORD = "db_password";

    /**
     * Custom Settings | Default values
     */
    private static final int DEFAULT_WEBSOCKET_PORT = 9876;

    public PostgresPersistenceManager() {

    }

    @Override
    public boolean doInsert(Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        EntityInserter ei = new EntityInserter(this);
        switch (entity.getEntityType()) {
            case Datastream:
                ei.insertDatastream((Datastream) entity);
                break;

            case MultiDatastream:
                ei.insertMultiDatastream((MultiDatastream) entity);
                break;

            case FeatureOfInterest:
                ei.insertFeatureOfInterest((FeatureOfInterest) entity);
                break;

            case HistoricalLocation:
                ei.insertHistoricalLocation((HistoricalLocation) entity);
                break;

            case Location:
                ei.insertLocation((Location) entity);
                break;

            case Observation:
                ei.insertObservation((Observation) entity);
                break;

            case ObservedProperty:
                ei.insertObservedProperty((ObservedProperty) entity);
                break;

            case Sensor:
                ei.insertSensor((Sensor) entity);
                break;

            case Thing:
                ei.insertThing((Thing) entity);
                break;

            default:
                throw new IllegalStateException("Unknown entity type: " + entity.getEntityType().name());

        }
        return true;
    }

    @Override
    public boolean doDelete(EntityPathElement pathElement) throws NoSuchEntityException {
        SQLQueryFactory qf = createQueryFactory();
        long id = (long) pathElement.getId().getValue();
        SQLDeleteClause delete;
        EntityType type = pathElement.getEntityType();
        switch (type) {
            case Datastream:
                delete = qf.delete(QDatastreams.datastreams).where(QDatastreams.datastreams.id.eq(id));
                break;

            case MultiDatastream:
                delete = qf.delete(QMultiDatastreams.multiDatastreams).where(QMultiDatastreams.multiDatastreams.id.eq(id));
                break;

            case FeatureOfInterest:
                delete = qf.delete(QFeatures.features).where(QFeatures.features.id.eq(id));
                break;

            case HistoricalLocation:
                delete = qf.delete(QHistLocations.histLocations).where(QHistLocations.histLocations.id.eq(id));
                break;

            case Location: {
                delete = qf.delete(QLocations.locations).where(QLocations.locations.id.eq(id));
                long count = delete.execute();
                if (count == 0) {
                    throw new NoSuchEntityException("No " + type + " with id " + id);
                }
                LOGGER.debug("Deleted {} Locations", count);

                // Also delete all historicalLocations that no longer reference any location
                QHistLocations qhl = QHistLocations.histLocations;
                QLocationsHistLocations qlhl = QLocationsHistLocations.locationsHistLocations;
                delete = qf.delete(qhl)
                        .where(qhl.id.in(
                                SQLExpressions.select(qhl.id)
                                .from(qhl)
                                .leftJoin(qlhl).on(qhl.id.eq(qlhl.histLocationId))
                                .where(qlhl.locationId.isNull())
                        ));
                count = delete.execute();
                LOGGER.debug("Deleted {} HistoricalLocations", count);
                return true;
            }

            case Observation:
                delete = qf.delete(QObservations.observations).where(QObservations.observations.id.eq(id));
                break;

            case ObservedProperty: {
                // First delete all MultiDatastreams that link to this ObservedProperty.
                QMultiDatastreams qMd = QMultiDatastreams.multiDatastreams;
                QMultiDatastreamsObsProperties qMdOp = QMultiDatastreamsObsProperties.multiDatastreamsObsProperties;
                delete = qf.delete(qMd).where(qMd.id.in(
                        SQLExpressions.select(qMdOp.multiDatastreamId).from(qMdOp).where(qMdOp.obsPropertyId.eq(id))
                ));
                long count = delete.execute();
                LOGGER.debug("Deleted {} MultiDatastreams.", count);

                delete = qf.delete(QObsProperties.obsProperties).where(QObsProperties.obsProperties.id.eq(id));
                count = delete.execute();
                if (count == 0) {
                    throw new NoSuchEntityException("No " + type + " with id " + id);
                }
                LOGGER.debug("Deleted {} ObservedProperties", count);
                return true;
            }

            case Sensor:
                delete = qf.delete(QSensors.sensors).where(QSensors.sensors.id.eq(id));
                break;

            case Thing:
                delete = qf.delete(QThings.things).where(QThings.things.id.eq(id));
                break;

            default:
                throw new NoSuchEntityException("Unknown entity type: " + pathElement.getEntityType());
        }
        if (delete != null) {
            long count = delete.execute();
            if (count == 0) {
                throw new NoSuchEntityException("No " + type + " with id " + id);
            }
            LOGGER.debug("Deleted {} entries of type {}", count, type);
        }
        return true;
    }

    @Override
    public boolean doUpdate(EntityPathElement pathElement, Entity entity) throws NoSuchEntityException {
        EntityInserter ei = new EntityInserter(this);
        entity.setId(pathElement.getId());
        long id = (long) pathElement.getId().getValue();
        if (!ei.entityExists(entity)) {
            throw new NoSuchEntityException("No entity of type " + pathElement.getEntityType() + " with id " + id);
        }
        EntityType type = pathElement.getEntityType();
        switch (type) {
            case Datastream:
                ei.updateDatastream((Datastream) entity, id);
                break;

            case MultiDatastream:
                ei.updateMultiDatastream((MultiDatastream) entity, id);
                break;

            case FeatureOfInterest:
                ei.updateFeatureOfInterest((FeatureOfInterest) entity, id);
                break;

            case HistoricalLocation:
                ei.updateHistoricalLocation((HistoricalLocation) entity, id);
                break;

            case Location:
                ei.updateLocation((Location) entity, id);
                break;

            case Observation:
                ei.updateObservation((Observation) entity, id);
                break;

            case ObservedProperty:
                ei.updateObservedProperty((ObservedProperty) entity, id);
                break;

            case Sensor:
                ei.updateSensor((Sensor) entity, id);
                break;

            case Thing:
                ei.updateThing((Thing) entity, id);
                break;

            default:
                throw new AssertionError(type.name());

        }

        return true;
    }

    @Override
    protected boolean doCommit() {
        try {
            if (!connectionProvider.get().isClosed()) {
                connectionProvider.get().commit();
                return true;
            }
        } catch (SQLException ex) {
            LOGGER.error("Exception rolling back.", ex);
        }
        return false;
    }

    @Override
    protected boolean doRollback() {
        try {
            if (!connectionProvider.get().isClosed()) {
                LOGGER.info("Rolling back changes.");
                connectionProvider.get().rollback();
                return true;
            }
        } catch (SQLException ex) {
            LOGGER.error("Exception rolling back.", ex);
        }
        return false;
    }

    @Override
    protected boolean doClose() {
        try {
            connectionProvider.get().close();
            return true;
        } catch (SQLException ex) {
            LOGGER.error("Exception closing.", ex);
        }
        return false;
    }

    private static class MyConnectionWrapper implements Provider<Connection> {

        private final Connection connection;

        public MyConnectionWrapper(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Connection get() {
            return connection;
        }

    }
    public static final DateTime DATETIME_MAX = DateTime.parse("9999-12-31T23:59:59.999Z");
    public static final DateTime DATETIME_MIN = DateTime.parse("-4000-01-01T00:00:00.000Z");
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresPersistenceManager.class);
    private Provider<Connection> connectionProvider;
    private SQLQueryFactory queryFactory;

    @Override
    public void init(CoreSettings settings) {
        try {
            Connection connection = getConnection(settings);
            connectionProvider = new MyConnectionWrapper(connection);
        } catch (NamingException | SQLException ex) {
            LOGGER.error("Could not inizialize " + getClass().getName(), ex);
        }
    }

    public static Connection getConnection(CoreSettings settings) throws NamingException, SQLException {
        Settings customSettings = settings.getPersistenceSettings().getCustomSettings();
        if (customSettings.contains(TAG_DATA_SOURCE)) {
            String dataSourceName = customSettings.getString(TAG_DATA_SOURCE);
            if (dataSourceName != null && !dataSourceName.isEmpty()) {
                InitialContext cxt = new InitialContext();
                if (cxt == null) {
                    throw new IllegalStateException("No context!");
                }

                DataSource ds = (DataSource) cxt.lookup("java:/comp/env/" + dataSourceName);
                if (ds == null) {
                    throw new IllegalStateException("Data source not found!");
                }
                Connection connection = ds.getConnection();
                connection.setAutoCommit(false);
                return connection;
            }
        }
        if (!customSettings.contains(TAG_DB_DRIVER) || customSettings.getString(TAG_DB_DRIVER).isEmpty()) {
            throw new IllegalArgumentException("Property '" + TAG_DB_DRIVER + "' must be non-empty");
        }
        try {
            Class.forName(customSettings.getString(TAG_DB_DRIVER));
        } catch (ClassNotFoundException ex) {
            LOGGER.error("Could not initialise database.", ex);
            throw new IllegalArgumentException(ex);
        }

        Connection connection = DriverManager.getConnection(
                customSettings.getString(TAG_DB_URL),
                customSettings.getString(TAG_DB_USERNAME),
                customSettings.getString(TAG_DB_PASSWORD));

        connection.setAutoCommit(false);
        return connection;
    }

    public SQLQueryFactory createQueryFactory() {
        if (queryFactory == null) {
            SQLTemplates templates = PostGISTemplates.builder().quote().build();
            queryFactory = new SQLQueryFactory(templates, connectionProvider);
        }
        return queryFactory;
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
        PathSqlBuilder psb = new PathSqlBuilder();
        SQLQuery<Tuple> sqlQuery = psb.buildFor(path, query, qf);

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

    public long count(ResourcePath path, Query query) {
        SQLQueryFactory qf = createQueryFactory();
        PathSqlBuilder psb = new PathSqlBuilder();
        SQLQuery<Tuple> sqlQuery = psb.buildFor(path, query, qf);
        return sqlQuery.fetchCount();
    }

}
