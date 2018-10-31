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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLDeleteClause;
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
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityCreator;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFromTupleFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityInserter;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.IdGenerationHandler;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PathSqlBuilder;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PathSqlBuilderImp;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PropertyResolver;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QThings;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QThingsLocations;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.sta.util.UpgradeFailedException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
 *
 * @author jab
 * @author scf
 * @author selimnairb
 */
public class PostgresPersistenceManagerUuid extends PostgresPersistenceManager<ComparablePath<UUID>, UUID> {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/tablesUuid.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresPersistenceManagerUuid.class);

    private static final IdManager ID_MANAGER = new IdManagerUuid();
    private static EntityFactories<ComparablePath<UUID>, UUID> entityFactories;
    private static PropertyResolver<ComparablePath<UUID>, UUID> propertyResolver;

    private CoreSettings settings;

    @Override
    public IdManager getIdManager() {
        return ID_MANAGER;
    }

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        super.init(settings);
        IdGenerationHandlerUuid.setIdGenerationMode(settings.getPersistenceSettings().getIdGenerationMode());
        if (entityFactories == null) {
            QCollection qCollection = new QCollection(
                    QDatastreams.datastreams,
                    QFeatures.features,
                    QHistLocations.histLocations,
                    QLocations.locations,
                    QMultiDatastreams.multiDatastreams,
                    QObsProperties.obsProperties,
                    QObservations.observations,
                    QSensors.sensors,
                    QThings.things,
                    QLocationsHistLocations.locationsHistLocations,
                    QMultiDatastreamsObsProperties.multiDatastreamsObsProperties,
                    QThingsLocations.thingsLocations);
            entityFactories = new EntityFactories(ID_MANAGER, qCollection);
            propertyResolver = new PropertyResolver<>(entityFactories, BasicPersistenceType.INTEGER);
        }
    }

    @Override
    public CoreSettings getCoreSettings() {
        return settings;
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
    public boolean doDelete(EntityPathElement pathElement) throws NoSuchEntityException {
        SQLQueryFactory qf = createQueryFactory();
        UUID id = (UUID) pathElement.getId().getValue();
        SQLDeleteClause delete;
        EntityType type = pathElement.getEntityType();
        switch (type) {
            case DATASTREAM:
                delete = qf.delete(QDatastreams.datastreams).where(QDatastreams.datastreams.getId().eq(id));
                break;

            case MULTIDATASTREAM:
                delete = qf.delete(QMultiDatastreams.multiDatastreams).where(QMultiDatastreams.multiDatastreams.id.eq(id));
                break;

            case FEATUREOFINTEREST:
                delete = qf.delete(QFeatures.features).where(QFeatures.features.id.eq(id));
                break;

            case HISTORICALLOCATION:
                delete = qf.delete(QHistLocations.histLocations).where(QHistLocations.histLocations.id.eq(id));
                break;

            case LOCATION:
                deleteLocation(qf, id, type);
                return true;

            case OBSERVATION:
                delete = qf.delete(QObservations.observations).where(QObservations.observations.id.eq(id));
                break;

            case OBSERVEDPROPERTY:
                deleteObservedProperty(qf, id, type);
                return true;

            case SENSOR:
                delete = qf.delete(QSensors.sensors).where(QSensors.sensors.id.eq(id));
                break;

            case THING:
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

    private void deleteObservedProperty(SQLQueryFactory qf, UUID id, EntityType type) throws NoSuchEntityException {
        SQLDeleteClause delete;
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
    }

    private void deleteLocation(SQLQueryFactory qf, UUID id, EntityType type) throws NoSuchEntityException {
        SQLDeleteClause delete;
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
    }

    @Override
    public void doDelete(ResourcePath path, Query query) {
        query.setSelect(Arrays.asList(EntityProperty.ID));
        SQLQueryFactory qf = createQueryFactory();
        PathSqlBuilderImp psb = new PathSqlBuilderImp(propertyResolver);

        SQLQuery<Tuple> sqlQuery = psb.buildFor(path, query, qf, settings.getPersistenceSettings());
        SQLDeleteClause sqlDelete = psb.createDelete((EntitySetPathElement) path.getLastElement(), qf, sqlQuery);

        long rowCount = sqlDelete.execute();
        LOGGER.debug("Deleted {} rows using query {}", rowCount, sqlDelete);
    }

    @Override
    public EntityChangedMessage doUpdate(EntityPathElement pathElement, Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        EntityInserter ei = new EntityInserter(this);
        entity.setId(pathElement.getId());
        UUID id = (UUID) pathElement.getId().getValue();
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
        PathSqlBuilder psb = new PathSqlBuilderImp(propertyResolver);
        SQLQuery<Tuple> sqlQuery = psb.buildFor(entityType, id, qf, settings.getPersistenceSettings());
        sqlQuery.limit(2);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Generated SQL:\n{}", sqlQuery.getSQL().getSQL());
        }
        List<Tuple> results = sqlQuery.fetch();

        EntityFromTupleFactory<? extends Entity, ComparablePath<UUID>, UUID> factory;
        factory = entityFactories.getFactoryFor(entityType.getImplementingClass());
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
        PathSqlBuilderImp psb = new PathSqlBuilderImp(propertyResolver);
        SQLQuery<Tuple> sqlQuery = psb.buildFor(path, query, qf, settings.getPersistenceSettings());

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
    public long count(ResourcePath path, Query query) {
        SQLQueryFactory qf = createQueryFactory();
        PathSqlBuilderImp psb = new PathSqlBuilderImp(propertyResolver);
        SQLQuery<Tuple> sqlQuery = psb.buildFor(path, query, qf, settings.getPersistenceSettings());
        return sqlQuery.fetchCount();
    }

    @Override
    public String checkForUpgrades() {
        StringWriter out = new StringWriter();
        try {
            Connection connection = getConnection(settings);

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase(LIQUIBASE_CHANGELOG_FILENAME, new ClassLoaderResourceAccessor(), database);
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
            Connection connection = getConnection(settings);

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase(LIQUIBASE_CHANGELOG_FILENAME, new ClassLoaderResourceAccessor(), database);
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

    @Override
    public EntityFactories<ComparablePath<UUID>, UUID> getEntityFactories() {
        return entityFactories;
    }

    @Override
    public IdGenerationHandler createIdGenerationHanlder(Entity e) {
        return new IdGenerationHandlerUuid(e);
    }

}
