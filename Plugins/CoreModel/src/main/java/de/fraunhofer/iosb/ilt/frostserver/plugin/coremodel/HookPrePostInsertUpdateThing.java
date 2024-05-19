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

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostUpdate;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreUpdate;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.Map;
import net.time4j.Moment;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class HookPrePostInsertUpdateThing implements HookPreInsert, HookPostInsert, HookPreUpdate, HookPostUpdate {

    private static final Logger LOGGER = LoggerFactory.getLogger(HookPrePostInsertUpdateThing.class);

    @ConfigurableField(editor = EditorString.class,
            label = "Things Locations LinkTable")
    @EditorString.EdOptsString(dflt = "THINGS_LOCATIONS")
    private String ttlName = "THINGS_LOCATIONS";

    @ConfigurableField(editor = EditorString.class,
            label = "LinkTable ThingId")
    @EditorString.EdOptsString(dflt = "THING_ID")
    private String ttlThingIdName = "THING_ID";

    @ConfigurableField(editor = EditorString.class,
            label = "LinkTable LocationId")
    @EditorString.EdOptsString(dflt = "LOCATION_ID")
    private String ttlLocationIdName = "LOCATION_ID";

    @ConfigurableField(editor = EditorString.class,
            label = "Locations-HistLocations LinkTable")
    @EditorString.EdOptsString(dflt = "LOCATIONS_HIST_LOCATIONS")
    private String tlhlName = "LOCATIONS_HIST_LOCATIONS";

    @ConfigurableField(editor = EditorString.class,
            label = "LinkTable HistLocationId")
    @EditorString.EdOptsString(dflt = "HIST_LOCATION_ID")
    private String tlhlHistLocationIdName = "HIST_LOCATION_ID";

    @ConfigurableField(editor = EditorString.class,
            label = "Time Field")
    @EditorString.EdOptsString(dflt = "TIME")
    private String thlTimeName = "TIME";

    @Override
    public boolean preInsertIntoDatabase(Phase fase, JooqPersistenceManager pm, Entity entity, Map<Field, Object> insertFields) throws NoSuchEntityException, IncompleteEntityException {
        return true;
    }

    @Override
    public boolean postInsertIntoDatabase(JooqPersistenceManager pm, Entity entity, Map<Field, Object> insertFields) throws NoSuchEntityException, IncompleteEntityException {
        PkValue entityId = entity.getPrimaryKeyValues();
        createHistLocationLinkLocations(pm, entity, entityId);
        return true;
    }

    @Override
    public void preUpdateInDatabase(JooqPersistenceManager pm, Entity entity, PkValue entityId, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException {
        TableCollection tables = pm.getTableCollection();
        EntityType et = entity.getEntityType();
        NavigationPropertyEntitySet npThingLocations = et.getNavigationPropertyEntitySet("Locations");
        if (npThingLocations == null) {
            LOGGER.error("Given Entity of type {} has no navigationPropertySet Locations", et);
            return;
        }
        EntitySet locations = entity.getProperty(npThingLocations);
        if (locations != null && !locations.isEmpty()) {
            // New locations to be set, delete old locations.
            Object thingId = entityId.get(0);
            DSLContext dslContext = pm.getDslContext();
            StaTable<?> ttl = tables.getTableForName(ttlName);
            // Unlink old Locations from Thing.
            long count = dslContext.delete(ttl).where(((TableField) ttl.field(ttlThingIdName)).eq(thingId)).execute();
            LOGGER.debug(EntityFactories.UNLINKED_L_FROM_T, count, thingId);
        }
    }

    @Override
    public void postUpdateInDatabase(JooqPersistenceManager pm, Entity entity, PkValue entityId, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException {
        createHistLocationLinkLocations(pm, entity, entityId);
    }

    public void createHistLocationLinkLocations(JooqPersistenceManager pm, Entity entity, PkValue entityId) throws DataAccessException {
        TableCollection tables = pm.getTableCollection();
        EntityType et = entity.getEntityType();
        NavigationPropertyEntitySet npThingLocations = et.getNavigationPropertyEntitySet("Locations");
        if (npThingLocations == null) {
            LOGGER.error("EntityType {} has no navigationPropertySet Locations", et);
            return;
        }
        NavigationPropertyEntitySet npThingHistLocs = et.getNavigationPropertyEntitySet("HistoricalLocations");
        if (npThingHistLocs == null) {
            LOGGER.error("EntityType {} has no navigationPropertySet HistoricalLocations", et);
            return;
        }
        EntityType etHistLoc = npThingHistLocs.getEntityType();
        EntitySet locations = entity.getProperty(npThingLocations);
        DSLContext dslContext = pm.getDslContext();
        Object thingId = entityId.get(0);
        // Now link the new locations also to a historicalLocation.
        if (locations != null && !locations.isEmpty()) {
            // Insert a new HL into the DB
            StaMainTable<?> thl = tables.getTableForType(etHistLoc);
            Object histLocationId = dslContext.insertInto(thl)
                    .set((TableField) thl.field(thlTimeName), Moment.nowInSystemTime())
                    .set((TableField) thl.field(ttlThingIdName), thingId)
                    .returningResult(thl.getPkFields().get(0))
                    .fetchOne(0);
            LOGGER.debug(EntityFactories.CREATED_HL, histLocationId);

            // Link the locations to the new HL
            StaTable<?> tlhl = tables.getTableForName(tlhlName);
            for (Entity location : locations) {
                Object locationId = location.getPrimaryKeyValues().get(0);
                if (locationId == null) {
                    LOGGER.error("Location with no ID");
                }
                dslContext.insertInto(tlhl)
                        .set((TableField) tlhl.field(tlhlHistLocationIdName), histLocationId)
                        .set((TableField) tlhl.field(ttlLocationIdName), locationId)
                        .execute();
                LOGGER.debug(EntityFactories.LINKED_L_TO_HL, locationId, histLocationId);
            }

            // Send a message about the creation of a new HL
            Entity newHl = pm.get(etHistLoc, PkValue.of(histLocationId));
            ModelRegistry modelRegistry = pm.getCoreSettings().getModelRegistry();
            newHl.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(newHl.getEntityType()));
            pm.getEntityChangedMessages().add(
                    new EntityChangedMessage()
                            .setEventType(EntityChangedMessage.Type.CREATE)
                            .setEntity(newHl));
        }
    }

    public void setTtlName(String ttlName) {
        this.ttlName = ttlName;
    }

    public void setTtlThingIdName(String ttlThingIdName) {
        this.ttlThingIdName = ttlThingIdName;
    }

    public void setTtlLocationIdName(String ttlLocationIdName) {
        this.ttlLocationIdName = ttlLocationIdName;
    }

    public void setTlhlName(String tlhlName) {
        this.tlhlName = tlhlName;
    }

    public void setTlhlHistLocationIdName(String tlhlHistLocationIdName) {
        this.tlhlHistLocationIdName = tlhlHistLocationIdName;
    }

    public void setThlTimeName(String thlTimeName) {
        this.thlTimeName = thlTimeName;
    }

}
