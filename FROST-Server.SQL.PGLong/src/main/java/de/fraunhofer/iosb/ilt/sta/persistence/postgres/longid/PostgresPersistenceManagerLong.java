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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLDeleteClause;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManagerlong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.IdGenerationHandler;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PropertyResolver;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QDatastreamsLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QFeaturesLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QHistLocationsLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QLocationsHistLocationsLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QLocationsLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QMultiDatastreamsLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QMultiDatastreamsObsPropertiesLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QObsPropertiesLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QObservationsLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QSensorsLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QThingsLocationsLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths.QThingsLong;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 * @author scf
 */
public class PostgresPersistenceManagerLong extends PostgresPersistenceManager<NumberPath<Long>, Long> {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/tables.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresPersistenceManagerLong.class);

    private static final IdManagerlong ID_MANAGER = new IdManagerlong();
    private static EntityFactories<NumberPath<Long>, Long> entityFactories;
    private static PropertyResolver<NumberPath<Long>, Long> propertyResolver;

    @Override
    public IdManager getIdManager() {
        return ID_MANAGER;
    }

    @Override
    public void init(CoreSettings settings) {
        super.init(settings);
        IdGenerationHandlerLong.setIdGenerationMode(settings.getPersistenceSettings().getIdGenerationMode());
        if (entityFactories == null) {
            QCollection qCollection = new QCollection(
                    QDatastreamsLong.DATASTREAMS,
                    QFeaturesLong.FEATURES,
                    QHistLocationsLong.HISTLOCATIONS,
                    QLocationsLong.LOCATIONS,
                    QMultiDatastreamsLong.MULTIDATASTREAMS,
                    QObsPropertiesLong.OBSPROPERTIES,
                    QObservationsLong.OBSERVATIONS,
                    QSensorsLong.SENSORS,
                    QThingsLong.THINGS,
                    QLocationsHistLocationsLong.LOCATIONSHISTLOCATIONS,
                    QMultiDatastreamsObsPropertiesLong.MULTIDATASTREAMSOBSPROPERTIES,
                    QThingsLocationsLong.THINGSLOCATIONS);
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
    public PropertyResolver<NumberPath<Long>, Long> getPropertyResolver() {
        return propertyResolver;
    }

    @Override
    public boolean doDelete(EntityPathElement pathElement) throws NoSuchEntityException {
        SQLQueryFactory qf = createQueryFactory();
        long id = (long) pathElement.getId().getValue();
        SQLDeleteClause delete;
        EntityType type = pathElement.getEntityType();
        switch (type) {
            case DATASTREAM:
                delete = qf.delete(QDatastreamsLong.DATASTREAMS).where(QDatastreamsLong.DATASTREAMS.getId().eq(id));
                break;

            case MULTIDATASTREAM:
                delete = qf.delete(QMultiDatastreamsLong.MULTIDATASTREAMS).where(QMultiDatastreamsLong.MULTIDATASTREAMS.id.eq(id));
                break;

            case FEATUREOFINTEREST:
                delete = qf.delete(QFeaturesLong.FEATURES).where(QFeaturesLong.FEATURES.id.eq(id));
                break;

            case HISTORICALLOCATION:
                delete = qf.delete(QHistLocationsLong.HISTLOCATIONS).where(QHistLocationsLong.HISTLOCATIONS.id.eq(id));
                break;

            case LOCATION:
                deleteLocation(qf, id, type);
                return true;

            case OBSERVATION:
                delete = qf.delete(QObservationsLong.OBSERVATIONS).where(QObservationsLong.OBSERVATIONS.id.eq(id));
                break;

            case OBSERVEDPROPERTY:
                deleteObservedProperty(qf, id, type);
                return true;

            case SENSOR:
                delete = qf.delete(QSensorsLong.SENSORS).where(QSensorsLong.SENSORS.id.eq(id));
                break;

            case THING:
                delete = qf.delete(QThingsLong.THINGS).where(QThingsLong.THINGS.id.eq(id));
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

    private void deleteObservedProperty(SQLQueryFactory qf, long id, EntityType type) throws NoSuchEntityException {
        SQLDeleteClause delete;
        // First delete all MultiDatastreams that link to this ObservedProperty.
        QMultiDatastreamsLong qMd = QMultiDatastreamsLong.MULTIDATASTREAMS;
        QMultiDatastreamsObsPropertiesLong qMdOp = QMultiDatastreamsObsPropertiesLong.MULTIDATASTREAMSOBSPROPERTIES;
        delete = qf.delete(qMd).where(qMd.id.in(
                SQLExpressions.select(qMdOp.multiDatastreamId).from(qMdOp).where(qMdOp.obsPropertyId.eq(id))
        ));
        long count = delete.execute();
        LOGGER.debug("Deleted {} MultiDatastreams.", count);
        delete = qf.delete(QObsPropertiesLong.OBSPROPERTIES).where(QObsPropertiesLong.OBSPROPERTIES.id.eq(id));
        count = delete.execute();
        if (count == 0) {
            throw new NoSuchEntityException("No " + type + " with id " + id);
        }
        LOGGER.debug("Deleted {} ObservedProperties", count);
    }

    private void deleteLocation(SQLQueryFactory qf, long id, EntityType type) throws NoSuchEntityException {
        SQLDeleteClause delete;
        delete = qf.delete(QLocationsLong.LOCATIONS).where(QLocationsLong.LOCATIONS.id.eq(id));
        long count = delete.execute();
        if (count == 0) {
            throw new NoSuchEntityException("No " + type + " with id " + id);
        }
        LOGGER.debug("Deleted {} Locations", count);
        // Also delete all historicalLocations that no longer reference any location
        QHistLocationsLong qhl = QHistLocationsLong.HISTLOCATIONS;
        QLocationsHistLocationsLong qlhl = QLocationsHistLocationsLong.LOCATIONSHISTLOCATIONS;
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
    public EntityFactories<NumberPath<Long>, Long> getEntityFactories() {
        return entityFactories;
    }

    @Override
    public IdGenerationHandler createIdGenerationHanlder(Entity e) {
        return new IdGenerationHandlerLong(e);
    }

}
