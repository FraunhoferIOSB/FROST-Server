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

import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLDeleteClause;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.IdGenerationHandler;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PropertyResolver;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QDatastreamsUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QFeaturesUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QHistLocationsUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QLocationsHistLocationsUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QLocationsUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QMultiDatastreamsObsPropertiesUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QMultiDatastreamsUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QObsPropertiesUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QObservationsUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QSensorsUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QThingsLocationsUuid;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths.QThingsUuid;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.util.UUID;
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

    @Override
    public IdManager getIdManager() {
        return ID_MANAGER;
    }

    @Override
    public void init(CoreSettings settings) {
        super.init(settings);
        IdGenerationHandlerUuid.setIdGenerationMode(settings.getPersistenceSettings().getIdGenerationMode());
        if (entityFactories == null) {
            QCollection qCollection = new QCollection(
                    QDatastreamsUuid.DATASTREAMS,
                    QFeaturesUuid.FEATURES,
                    QHistLocationsUuid.HISTLOCATIONS,
                    QLocationsUuid.LOCATIONS,
                    QMultiDatastreamsUuid.MULTIDATASTREAMS,
                    QObsPropertiesUuid.OBSPROPERTIES,
                    QObservationsUuid.OBSERVATIONS,
                    QSensorsUuid.SENSORS,
                    QThingsUuid.THINGS,
                    QLocationsHistLocationsUuid.LOCATIONSHISTLOCATIONS,
                    QMultiDatastreamsObsPropertiesUuid.MULTIDATASTREAMSOBSPROPERTIES,
                    QThingsLocationsUuid.THINGSLOCATIONS);
            init(qCollection);
        }
    }

    private static synchronized void init(QCollection qCollection) {
        if (entityFactories == null) {
            entityFactories = new EntityFactories(ID_MANAGER, qCollection);
            propertyResolver = new PropertyResolver<>(entityFactories, BasicPersistenceType.INTEGER);
        }
    }

    @Override
    public PropertyResolver<ComparablePath<UUID>, UUID> getPropertyResolver() {
        return propertyResolver;
    }

    @Override
    public boolean doDelete(EntityPathElement pathElement) throws NoSuchEntityException {
        SQLQueryFactory qf = createQueryFactory();
        UUID id = (UUID) pathElement.getId().getValue();
        SQLDeleteClause delete;
        EntityType type = pathElement.getEntityType();
        switch (type) {
            case DATASTREAM:
                delete = qf.delete(QDatastreamsUuid.DATASTREAMS).where(QDatastreamsUuid.DATASTREAMS.getId().eq(id));
                break;

            case MULTIDATASTREAM:
                delete = qf.delete(QMultiDatastreamsUuid.MULTIDATASTREAMS).where(QMultiDatastreamsUuid.MULTIDATASTREAMS.id.eq(id));
                break;

            case FEATUREOFINTEREST:
                delete = qf.delete(QFeaturesUuid.FEATURES).where(QFeaturesUuid.FEATURES.id.eq(id));
                break;

            case HISTORICALLOCATION:
                delete = qf.delete(QHistLocationsUuid.HISTLOCATIONS).where(QHistLocationsUuid.HISTLOCATIONS.id.eq(id));
                break;

            case LOCATION:
                deleteLocation(qf, id, type);
                return true;

            case OBSERVATION:
                delete = qf.delete(QObservationsUuid.OBSERVATIONS).where(QObservationsUuid.OBSERVATIONS.id.eq(id));
                break;

            case OBSERVEDPROPERTY:
                deleteObservedProperty(qf, id, type);
                return true;

            case SENSOR:
                delete = qf.delete(QSensorsUuid.SENSORS).where(QSensorsUuid.SENSORS.id.eq(id));
                break;

            case THING:
                delete = qf.delete(QThingsUuid.THINGS).where(QThingsUuid.THINGS.id.eq(id));
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
        QMultiDatastreamsUuid qMd = QMultiDatastreamsUuid.MULTIDATASTREAMS;
        QMultiDatastreamsObsPropertiesUuid qMdOp = QMultiDatastreamsObsPropertiesUuid.MULTIDATASTREAMSOBSPROPERTIES;
        delete = qf.delete(qMd).where(qMd.id.in(
                SQLExpressions.select(qMdOp.multiDatastreamId).from(qMdOp).where(qMdOp.obsPropertyId.eq(id))
        ));
        long count = delete.execute();
        LOGGER.debug("Deleted {} MultiDatastreams.", count);
        delete = qf.delete(QObsPropertiesUuid.OBSPROPERTIES).where(QObsPropertiesUuid.OBSPROPERTIES.id.eq(id));
        count = delete.execute();
        if (count == 0) {
            throw new NoSuchEntityException("No " + type + " with id " + id);
        }
        LOGGER.debug("Deleted {} ObservedProperties", count);
    }

    private void deleteLocation(SQLQueryFactory qf, UUID id, EntityType type) throws NoSuchEntityException {
        SQLDeleteClause delete;
        delete = qf.delete(QLocationsUuid.LOCATIONS).where(QLocationsUuid.LOCATIONS.id.eq(id));
        long count = delete.execute();
        if (count == 0) {
            throw new NoSuchEntityException("No " + type + " with id " + id);
        }
        LOGGER.debug("Deleted {} Locations", count);
        // Also delete all historicalLocations that no longer reference any location
        QHistLocationsUuid qhl = QHistLocationsUuid.HISTLOCATIONS;
        QLocationsHistLocationsUuid qlhl = QLocationsHistLocationsUuid.LOCATIONSHISTLOCATIONS;
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
    public String getLiquibaseChangelogFilename() {
        return LIQUIBASE_CHANGELOG_FILENAME;
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
