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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager.LINK_TABLE;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
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
 * Hook to update Thing-Location relations when the a new HistoricalLocation is
 * added to a Thing.
 */
class HookPostInsertHistLoc implements HookPostInsert {

    private static final Logger LOGGER = LoggerFactory.getLogger(HookPostInsertHistLoc.class.getName());

    @ConfigurableField(editor = EditorString.class,
            label = "HistoricalLocations Table")
    @EditorString.EdOptsString(dflt = TableImpHistLocations.NAME_TABLE)
    private String nameTblHistLoc = TableImpHistLocations.NAME_TABLE;

    @ConfigurableField(editor = EditorString.class,
            label = "Thing-ID Field in HistoricalLocations Table")
    @EditorString.EdOptsString(dflt = "THING_ID")
    private String nameFldHistLocThingId = TableImpHistLocations.NAME_COL_THINGID;

    @ConfigurableField(editor = EditorString.class,
            label = "Time Field in HistoricalLocations Table")
    @EditorString.EdOptsString(dflt = "TIME")
    private String nameFldHistLocTime = TableImpHistLocations.NAME_COL_TIME;

    @ConfigurableField(editor = EditorString.class,
            label = "Things-Locations LinkTable")
    @EditorString.EdOptsString(dflt = TableImpThingsLocations.NAME_TABLE)
    private String nameTblThingsLocs = TableImpThingsLocations.NAME_TABLE;

    @ConfigurableField(editor = EditorString.class,
            label = "Thing-ID Field in Things-Locations Table")
    @EditorString.EdOptsString(dflt = TableImpThingsLocations.NAME_COL_TL_THINGID)
    private String nameFldThingsLocsThingId = TableImpThingsLocations.NAME_COL_TL_THINGID;

    @ConfigurableField(editor = EditorString.class,
            label = "Location-ID Field in Things-Locations Table")
    @EditorString.EdOptsString(dflt = TableImpThingsLocations.NAME_COL_TL_LOCATIONID)
    private String nameFldThingsLocsLocId = TableImpThingsLocations.NAME_COL_TL_LOCATIONID;

    private StaTable<?> thl;
    private TableField thlThingId;
    private TableField thlTime;
    private StaTable<?> ttl;
    private TableField ttlThingId;
    private TableField ttlLocId;

    @Override
    public boolean postInsertIntoDatabase(JooqPersistenceManager pm, Entity histLoc, Map<Field, Object> insertFields) throws NoSuchEntityException, IncompleteEntityException {
        final EntityFactories ef = pm.getEntityFactories();
        final TableCollection tc = pm.getTableCollection();
        final EntityType etHistLoc = histLoc.getType();
        final NavigationPropertyEntity npThing = (NavigationPropertyEntity) etHistLoc.getNavigationProperty("Thing");
        final NavigationPropertyEntitySet npLocations = (NavigationPropertyEntitySet) etHistLoc.getNavigationProperty("Locations");
        final EntityPropertyMain<TimeInstant> epTime = etHistLoc.getEntityProperty("time");
        Entity thing = histLoc.getProperty(npThing);
        Object thingId = thing.getPrimaryKeyValues().get(0);
        DSLContext dslContext = pm.getDslContext();
        if (ttlLocId == null) {
            thl = tc.getTableForName(nameTblHistLoc);
            thlThingId = (TableField) thl.field(nameFldHistLocThingId);
            thlTime = (TableField) thl.field(nameFldHistLocTime);
            ttl = tc.getTableForName(nameTblThingsLocs);
            ttlThingId = (TableField) ttl.field(nameFldThingsLocsThingId);
            ttlLocId = (TableField) ttl.field(nameFldThingsLocsLocId);
        }
        final TimeInstant hlTime = histLoc.getProperty(epTime);
        Moment newTime = hlTime.getDateTime();
        // https://github.com/opengeospatial/sensorthings/issues/30
        // Check the time of the latest HistoricalLocation of our thing.
        // If this time is earlier than our time, set the Locations of our Thing to our Locations.
        Record lastHistLocation = pm.timeFetchOne(
                dslContext.select(Collections.emptyList())
                        .from(thl)
                        .where(thlThingId.eq(thingId)
                                .and(thlTime.gt(newTime)))
                        .orderBy(thlTime.desc())
                        .limit(1),
                histLoc.getType().entityName);
        if (lastHistLocation == null) {
            // We are the newest.
            // Unlink old Locations from Thing.
            long count = pm.timeExecute(
                    dslContext.delete(ttl).where(ttlThingId.eq(thingId)),
                    LINK_TABLE);
            LOGGER.debug(EntityFactories.UNLINKED_L_FROM_T, count, thingId);
            // Link new locations to Thing.
            for (Entity l : histLoc.getProperty(npLocations)) {
                if (!l.getPrimaryKeyValues().isFullySet() || !ef.entityExists(pm, l, true)) {
                    throw new NoSuchEntityException("Location with no id.");
                }
                Object locationId = l.getPrimaryKeyValues().get(0);
                pm.timeExecute(
                        dslContext.insertInto(ttl).set(ttlThingId, thingId).set(ttlLocId, locationId),
                        LINK_TABLE);
                LOGGER.debug(EntityFactories.LINKED_L_TO_T, locationId, thingId);
            }
        }
        return true;
    }

    public void setNameTblHistLoc(String nameTblHistLoc) {
        this.nameTblHistLoc = nameTblHistLoc;
    }

    public void setNameFldHistLocThingId(String nameFldHistLocThingId) {
        this.nameFldHistLocThingId = nameFldHistLocThingId;
    }

    public void setNameFldHistLocTime(String nameFldHistLocTime) {
        this.nameFldHistLocTime = nameFldHistLocTime;
    }

    public void setNameTblThingsLocs(String nameTblThingsLocs) {
        this.nameTblThingsLocs = nameTblThingsLocs;
    }

    public void setNameFldThingsLocsThingId(String nameFldThingsLocsThingId) {
        this.nameFldThingsLocsThingId = nameFldThingsLocsThingId;
    }

    public void setNameFldThingsLocsLocId(String nameFldThingsLocsLocId) {
        this.nameFldThingsLocsLocId = nameFldThingsLocsLocId;
    }

}
