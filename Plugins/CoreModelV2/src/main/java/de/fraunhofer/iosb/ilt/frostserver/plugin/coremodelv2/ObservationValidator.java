/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueBoolean;
import java.util.Map;
import org.jooq.Field;

/**
 * Validates Observations against the resultType and resultEncoding of the
 * Datastream.
 */
public class ObservationValidator implements HookPreInsert, ConfigDefaults {

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE = "coreModelV2.resultValidator.enable";

    private boolean initialised;
    private boolean enabled = true;
    private EntityType etObservation;
    private EntityPropertyMain<Object> epResult;
    private NavigationPropertyEntity npDatastream;

    private boolean initialise(JooqPersistenceManager pm, Entity entity) {
        if (initialised) {
            return enabled;
        }
        enabled = pm.getCoreSettings().getSettings().getBoolean(TAG_ENABLE, getClass());
        ModelRegistry mr = pm.getCoreSettings().getModelRegistry();
        etObservation = mr.getEntityTypeForName("Observation", true);
        epResult = etObservation.getEntityProperty("result");
        initialised = true;
        return enabled;
    }

    @Override
    public boolean preInsertIntoDatabase(Phase fase, JooqPersistenceManager pm, Entity entity, Map<Field, Object> insertFields) throws NoSuchEntityException, IncompleteEntityException {
        if (fase != Phase.POST_RELATIONS || !initialise(pm, entity)) {
            return true;
        }
        Object result = entity.getProperty(epResult);
        if (result == null) {
            // NULL result is always allowed.
            return true;
        }

        return true;
    }

}
