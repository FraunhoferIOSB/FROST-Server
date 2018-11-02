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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid;

import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLDeleteClause;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManagerString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.IdGenerationHandler;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PropertyResolver;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QDatastreamsString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QFeaturesString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QHistLocationsString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QLocationsHistLocationsString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QLocationsString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QMultiDatastreamsObsPropertiesString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QMultiDatastreamsString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QObsPropertiesString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QObservationsString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QSensorsString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QThingsLocationsString;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths.QThingsString;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 * @author scf
 */
public class PostgresPersistenceManagerString extends PostgresPersistenceManager<StringPath, String> {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/tablesString.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresPersistenceManagerString.class);

    private static final IdManagerString ID_MANAGER = new IdManagerString();
    private static EntityFactories<StringPath, String> entityFactories;
    private static PropertyResolver<StringPath, String> propertyResolver;

    @Override
    public IdManager getIdManager() {
        return ID_MANAGER;
    }

    @Override
    public void init(CoreSettings settings) {
        super.init(settings);
        IdGenerationHandlerString.setIdGenerationMode(settings.getPersistenceSettings().getIdGenerationMode());
        if (entityFactories == null) {
            QCollection qCollection = new QCollection(
                    QDatastreamsString.DATASTREAMS,
                    QFeaturesString.FEATURES,
                    QHistLocationsString.HISTLOCATIONS,
                    QLocationsString.LOCATIONS,
                    QMultiDatastreamsString.MULTIDATASTREAMS,
                    QObsPropertiesString.OBSPROPERTIES,
                    QObservationsString.OBSERVATIONS,
                    QSensorsString.SENSORS,
                    QThingsString.THINGS,
                    QLocationsHistLocationsString.LOCATIONSHISTLOCATIONS,
                    QMultiDatastreamsObsPropertiesString.MULTIDATASTREAMSOBSPROPERTIES,
                    QThingsLocationsString.THINGSLOCATIONS);
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
    public PropertyResolver<StringPath, String> getPropertyResolver() {
        return propertyResolver;
    }

    @Override
    public boolean doDelete(EntityPathElement pathElement) throws NoSuchEntityException {
        SQLQueryFactory qf = createQueryFactory();
        String id = (String) pathElement.getId().getValue();
        SQLDeleteClause delete;
        EntityType type = pathElement.getEntityType();
        switch (type) {
            case DATASTREAM:
                delete = qf.delete(QDatastreamsString.DATASTREAMS).where(QDatastreamsString.DATASTREAMS.getId().eq(id));
                break;

            case MULTIDATASTREAM:
                delete = qf.delete(QMultiDatastreamsString.MULTIDATASTREAMS).where(QMultiDatastreamsString.MULTIDATASTREAMS.id.eq(id));
                break;

            case FEATUREOFINTEREST:
                delete = qf.delete(QFeaturesString.FEATURES).where(QFeaturesString.FEATURES.id.eq(id));
                break;

            case HISTORICALLOCATION:
                delete = qf.delete(QHistLocationsString.HISTLOCATIONS).where(QHistLocationsString.HISTLOCATIONS.id.eq(id));
                break;

            case LOCATION:
                deleteLocation(qf, id, type);
                return true;

            case OBSERVATION:
                delete = qf.delete(QObservationsString.OBSERVATIONS).where(QObservationsString.OBSERVATIONS.id.eq(id));
                break;

            case OBSERVEDPROPERTY:
                deleteObservedProperty(qf, id, type);
                return true;

            case SENSOR:
                delete = qf.delete(QSensorsString.SENSORS).where(QSensorsString.SENSORS.id.eq(id));
                break;

            case THING:
                delete = qf.delete(QThingsString.THINGS).where(QThingsString.THINGS.id.eq(id));
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

    private void deleteObservedProperty(SQLQueryFactory qf, String id, EntityType type) throws NoSuchEntityException {
        SQLDeleteClause delete;
        // First delete all MultiDatastreams that link to this ObservedProperty.
        QMultiDatastreamsString qMd = QMultiDatastreamsString.MULTIDATASTREAMS;
        QMultiDatastreamsObsPropertiesString qMdOp = QMultiDatastreamsObsPropertiesString.MULTIDATASTREAMSOBSPROPERTIES;
        delete = qf.delete(qMd).where(qMd.id.in(
                SQLExpressions.select(qMdOp.multiDatastreamId).from(qMdOp).where(qMdOp.obsPropertyId.eq(id))
        ));
        long count = delete.execute();
        LOGGER.debug("Deleted {} MultiDatastreams.", count);
        delete = qf.delete(QObsPropertiesString.OBSPROPERTIES).where(QObsPropertiesString.OBSPROPERTIES.id.eq(id));
        count = delete.execute();
        if (count == 0) {
            throw new NoSuchEntityException("No " + type + " with id " + id);
        }
        LOGGER.debug("Deleted {} ObservedProperties", count);
    }

    private void deleteLocation(SQLQueryFactory qf, String id, EntityType type) throws NoSuchEntityException {
        SQLDeleteClause delete;
        delete = qf.delete(QLocationsString.LOCATIONS).where(QLocationsString.LOCATIONS.id.eq(id));
        long count = delete.execute();
        if (count == 0) {
            throw new NoSuchEntityException("No " + type + " with id " + id);
        }
        LOGGER.debug("Deleted {} Locations", count);
        // Also delete all historicalLocations that no longer reference any location
        QHistLocationsString qhl = QHistLocationsString.HISTLOCATIONS;
        QLocationsHistLocationsString qlhl = QLocationsHistLocationsString.LOCATIONSHISTLOCATIONS;
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
    public EntityFactories<StringPath, String> getEntityFactories() {
        return entityFactories;
    }

    @Override
    public IdGenerationHandler createIdGenerationHanlder(Entity e) {
        return new IdGenerationHandlerString(e);
    }

}
