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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManagerlong;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongThings;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid.TableLongThingsLocations;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;

/**
 *
 * @author jab
 * @author scf
 */
public class PostgresPersistenceManagerLong extends PostgresPersistenceManager<Long> {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/tables.xml";

    private static final IdManagerlong ID_MANAGER = new IdManagerlong();
    private static EntityFactories<Long> entityFactories;
    private static PropertyResolver<Long> propertyResolver;

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
                    TableLongDatastreams.DATASTREAMS,
                    TableLongFeatures.FEATURES,
                    TableLongHistLocations.HIST_LOCATIONS,
                    TableLongLocations.LOCATIONS,
                    TableLongLocationsHistLocations.LOCATIONS_HIST_LOCATIONS,
                    TableLongMultiDatastreams.MULTI_DATASTREAMS,
                    TableLongMultiDatastreamsObsProperties.MULTI_DATASTREAMS_OBS_PROPERTIES,
                    TableLongObservations.OBSERVATIONS,
                    TableLongObsProperties.OBS_PROPERTIES,
                    TableLongSensors.SENSORS,
                    TableLongThings.THINGS,
                    TableLongThingsLocations.THINGS_LOCATIONS);
            init(qCollection);
        }
    }

    private static synchronized void init(QCollection<Long> qCollection) {
        if (entityFactories == null) {
            entityFactories = new EntityFactories(ID_MANAGER, qCollection);
            propertyResolver = new PropertyResolver<>(qCollection, BasicPersistenceType.INTEGER);
        }
    }

    @Override
    public PropertyResolver<Long> getPropertyResolver() {
        return propertyResolver;
    }

    @Override
    public String getLiquibaseChangelogFilename() {
        return LIQUIBASE_CHANGELOG_FILENAME;
    }

    @Override
    public EntityFactories<Long> getEntityFactories() {
        return entityFactories;
    }

    @Override
    public IdGenerationHandler createIdGenerationHanlder(Entity e) {
        return new IdGenerationHandlerLong(e);
    }

}
