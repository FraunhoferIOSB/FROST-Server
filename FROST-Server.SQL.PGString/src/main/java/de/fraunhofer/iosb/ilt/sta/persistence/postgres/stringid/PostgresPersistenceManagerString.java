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
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
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

/**
 *
 * @author jab
 * @author scf
 */
public class PostgresPersistenceManagerString extends PostgresPersistenceManager<StringPath, String> {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/tablesString.xml";

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
