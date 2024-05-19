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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.Collections;
import java.util.Map;
import net.time4j.Moment;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
class HookPostInsertHistLoc implements HookPostInsert {

    private static final Logger LOGGER = LoggerFactory.getLogger(HookPostInsertHistLoc.class.getName());

    @Override
    public boolean postInsertIntoDatabase(JooqPersistenceManager pm, Entity histLoc, Map<Field, Object> insertFields) throws NoSuchEntityException, IncompleteEntityException {
        final EntityFactories ef = pm.getEntityFactories();
        final TableCollection tc = pm.getTableCollection();
        final EntityType etHistLoc = histLoc.getEntityType();
        final NavigationPropertyEntity npThing = (NavigationPropertyEntity) etHistLoc.getNavigationProperty("Thing");
        final NavigationPropertyEntitySet npLocations = (NavigationPropertyEntitySet) etHistLoc.getNavigationProperty("Locations");
        final EntityPropertyMain<TimeInstant> epTime = etHistLoc.getEntityProperty("time");
        Entity thing = histLoc.getProperty(npThing);
        Object thingId = thing.getPrimaryKeyValues().get(0);
        DSLContext dslContext = pm.getDslContext();
        TableImpHistLocations thl = tc.getTableForClass(TableImpHistLocations.class);
        final TimeInstant hlTime = histLoc.getProperty(epTime);
        Moment newTime = hlTime.getDateTime();
        // https://github.com/opengeospatial/sensorthings/issues/30
        // Check the time of the latest HistoricalLocation of our thing.
        // If this time is earlier than our time, set the Locations of our Thing to our Locations.
        Record lastHistLocation = dslContext.select(Collections.emptyList()).from(thl).where(((TableField) thl.getThingId()).eq(thingId).and(thl.time.gt(newTime))).orderBy(thl.time.desc()).limit(1).fetchOne();
        if (lastHistLocation == null) {
            // We are the newest.
            // Unlink old Locations from Thing.
            TableImpThingsLocations qtl = tc.getTableForClass(TableImpThingsLocations.class);
            long count = dslContext.delete(qtl).where(((TableField) qtl.getThingId()).eq(thingId)).execute();
            LOGGER.debug(EntityFactories.UNLINKED_L_FROM_T, count, thingId);
            // Link new locations to Thing.
            for (Entity l : histLoc.getProperty(npLocations)) {
                if (!l.getPrimaryKeyValues().isFullySet() || !ef.entityExists(pm, l, true)) {
                    throw new NoSuchEntityException("Location with no id.");
                }
                Object locationId = l.getPrimaryKeyValues().get(0);
                dslContext.insertInto(qtl).set((TableField) qtl.getThingId(), thingId).set(qtl.getLocationId(), locationId).execute();
                LOGGER.debug(EntityFactories.LINKED_L_TO_T, locationId, thingId);
            }
        }
        return true;
    }

}
