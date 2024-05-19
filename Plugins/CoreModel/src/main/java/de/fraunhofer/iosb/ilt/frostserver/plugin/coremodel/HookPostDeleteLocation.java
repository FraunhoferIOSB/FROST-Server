/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostDelete;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
class HookPostDeleteLocation implements HookPostDelete {

    private static final Logger LOGGER = LoggerFactory.getLogger(HookPostDeleteLocation.class.getName());

    @Override
    public void postDelete(JooqPersistenceManager pm, PkValue entityId) throws NoSuchEntityException {
        final TableCollection tables = pm.getTableCollection();
        // Also postDelete all historicalLocations that no longer reference any location
        TableImpHistLocations thl = tables.getTableForClass(TableImpHistLocations.class);
        TableImpLocationsHistLocations tlhl = tables.getTableForClass(TableImpLocationsHistLocations.class);
        int count = pm.getDslContext().delete(thl).where(((TableField) thl.getId()).in(DSL.select(thl.getId()).from(thl).leftJoin(tlhl).on(((TableField) thl.getId()).eq(tlhl.getHistLocationId())).where(tlhl.getLocationId().isNull()))).execute();
        LOGGER.debug("Deleted {} HistoricalLocations", count);
    }

}
