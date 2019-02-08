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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManagerString;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.IdGenerationHandler;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PropertyResolver;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringActuators;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringTaskingCapabilities;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringTasks;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringThings;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid.TableStringThingsLocations;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;

/**
 *
 * @author jab
 * @author scf
 */
public class PostgresPersistenceManagerString extends PostgresPersistenceManager<String> {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/tablesString.xml";

    private static final IdManagerString ID_MANAGER = new IdManagerString();
    private static EntityFactories<String> entityFactories;
    private static PropertyResolver<String> propertyResolver;

    @Override
    public IdManager getIdManager() {
        return ID_MANAGER;
    }

    @Override
    public void init(CoreSettings settings) {
        super.init(settings);
        IdGenerationHandlerString.setIdGenerationMode(settings.getPersistenceSettings().getIdGenerationMode());
        if (entityFactories == null) {
            TableCollection tableCollection = new TableCollection(
                    TableStringActuators.ACTUATORS,
                    TableStringDatastreams.DATASTREAMS,
                    TableStringFeatures.FEATURES,
                    TableStringHistLocations.HIST_LOCATIONS,
                    TableStringLocations.LOCATIONS,
                    TableStringLocationsHistLocations.LOCATIONS_HIST_LOCATIONS,
                    TableStringMultiDatastreams.MULTI_DATASTREAMS,
                    TableStringMultiDatastreamsObsProperties.MULTI_DATASTREAMS_OBS_PROPERTIES,
                    TableStringObservations.OBSERVATIONS,
                    TableStringObsProperties.OBS_PROPERTIES,
                    TableStringSensors.SENSORS,
                    TableStringTasks.TASKS,
                    TableStringTaskingCapabilities.TASKINGCAPABILITIES,
                    TableStringThings.THINGS,
                    TableStringThingsLocations.THINGS_LOCATIONS);
            init(tableCollection);
        }
    }

    private static synchronized void init(TableCollection<String> tableCollection) {
        if (entityFactories == null) {
            entityFactories = new EntityFactories(ID_MANAGER, tableCollection);
            propertyResolver = new PropertyResolver<>(tableCollection, BasicPersistenceType.STRING);
        }
    }

    @Override
    public PropertyResolver<String> getPropertyResolver() {
        return propertyResolver;
    }

    @Override
    public String getLiquibaseChangelogFilename() {
        return LIQUIBASE_CHANGELOG_FILENAME;
    }

    @Override
    public EntityFactories<String> getEntityFactories() {
        return entityFactories;
    }

    @Override
    public IdGenerationHandler createIdGenerationHanlder(Entity e) {
        return new IdGenerationHandlerString(e);
    }

}
