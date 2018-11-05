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
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
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
import java.util.UUID;

/**
 *
 * @author jab
 * @author scf
 * @author selimnairb
 */
public class PostgresPersistenceManagerUuid extends PostgresPersistenceManager<ComparablePath<UUID>, UUID> {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/tablesUuid.xml";

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
