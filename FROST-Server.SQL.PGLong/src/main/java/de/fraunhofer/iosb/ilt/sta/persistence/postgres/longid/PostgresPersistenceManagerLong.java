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
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
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

/**
 *
 * @author jab
 * @author scf
 */
public class PostgresPersistenceManagerLong extends PostgresPersistenceManager<NumberPath<Long>, Long> {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/tables.xml";

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
