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
package de.fraunhofer.iosb.ilt.frostserver.plugin.modelom;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookRelation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.Relation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.HookValidator;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.Exceptions;
import net.time4j.SystemClock;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds hooks to deal with LinkingTime.
 */
public class LinkingTimeValidator implements HookValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkingTimeValidator.class.getName());

    public static final String NAME_MONITORINGNETWORK_THINGS = "Things";
    public static final String NAME_TBL_NETWORK_THINGS = "monitoring_network_thing";
    public static final String NAME_TBL_TN_LINKING_TIME = "tn_linking_time";
    public static final String NAME_COL_ID = "id";
    public static final String NAME_COL_THING_ID = "thing_id";
    public static final String NAME_COL_NETWORK_ID = "network_id";
    public static final String NAME_COL_TIME_START = "time_start";
    public static final String NAME_COL_TIME_END = "time_end";

    @Override
    public void registerHooks(StaMainTable mainTable, JooqPersistenceManager ppm) {
        final String entityName = mainTable.getEntityType().entityName;

        if (null != entityName) {
            switch (entityName) {
                case "MonitoringNetwork" ->
                    registerHooksMonitoringNetwork(mainTable, ppm);
                case "LinkingTime" ->
                    registerHooksMonitoringNetwork(mainTable, ppm);
                default -> {
                    // Nothing to do.
                }
            }
        }
    }

    public void registerHooksMonitoringNetwork(StaMainTable networksTable, JooqPersistenceManager ppm) {
        final Relation relThings = networksTable.findRelation(NAME_MONITORINGNETWORK_THINGS);
        final EntityType etNetworks = networksTable.getEntityType();
        if (relThings == null) {
            LOGGER.error("Count not find relation {} on table of entity type {}", NAME_MONITORINGNETWORK_THINGS, etNetworks);
            return;
        }
        // Here we swap target and source!
        relThings.registerHook(0.0, HookRelation.of(
                (pm, np, source, target) -> directLinkPreCreate(pm, np, target, source),
                (pm, np, source, target) -> directLinkPostDelete(pm, np, target, source)));

        final var npThings = etNetworks.getNavigationPropertyEntitySet(NAME_MONITORINGNETWORK_THINGS);
        Exceptions.unknownPropertyIf(npThings == null, "NavigationProperty {}/{} not found", NAME_MONITORINGNETWORK_THINGS);
        final EntityType thingType = npThings.getEntityType();
        final StaMainTable<?> thingsTable = ppm.getTableCollection().getTableForType(thingType);
        Relation<?> relNetworks = thingsTable.findRelation(npThings.getInverse().getName());
        // Here we do not swap target and source!
        relNetworks.registerHook(0.0, HookRelation.of(
                LinkingTimeValidator::directLinkPreCreate,
                LinkingTimeValidator::directLinkPostDelete));
    }

    private static void directLinkPreCreate(JooqPersistenceManager pm, NavigationProperty np, Entity thing, Entity network) {
        LOGGER.info("Running hook for the creation of {} <-> {} <-> {}", thing, np, network);
        PkValue thingPk = thing.getPrimaryKeyValues();
        PkValue networkPk = network.getPrimaryKeyValues();

        Exceptions.illegalArgumentIf(!thingPk.isFullySet() || !networkPk.isFullySet(),
                "Primary key of Thing ({}) or Network ({}) not fully set!", thingPk, networkPk);

        TableCollection tables = pm.getTableCollection();
        DSLContext dslContext = pm.getDslContext();
        StaTable<?> tlt = tables.getTableForName(NAME_TBL_TN_LINKING_TIME);
        final Field tltThingId = tlt.field(NAME_COL_THING_ID);
        final Field tltNetworkId = tlt.field(NAME_COL_NETWORK_ID);
        final Field tltTimeStart = tlt.field(NAME_COL_TIME_START);
        final Field tltTimeEnd = tlt.field(NAME_COL_TIME_END);
        Object exists = dslContext.selectOne()
                .from(tlt)
                .where(tltThingId.eq(thingPk.get(0)))
                .and(tltNetworkId.eq(networkPk.get(0)))
                .and(tltTimeEnd.eq(tltTimeStart).or(tltTimeEnd.isNull()))
                .fetchOne(0);
        if (exists != null) {
            LOGGER.info("An open LinkingTime already exists between {} and {}", thing, network);
            return;
        }
        dslContext.insertInto(tlt)
                .set(tltThingId, thingPk.get(0))
                .set(tltNetworkId, networkPk.get(0))
                .set(tltTimeStart, SystemClock.currentMoment())
                .execute();
        LOGGER.debug("Created a new LinkingTime between {} and {}", thing, network);
    }

    private static void directLinkPostDelete(JooqPersistenceManager pm, NavigationProperty np, Entity thing, Entity network) {
        LOGGER.info("Running hook for the removal of {} <-> {} <-> {}", thing, np, network);
        PkValue thingPk = thing.getPrimaryKeyValues();
        PkValue networkPk = network.getPrimaryKeyValues();

        Exceptions.illegalArgumentIf(!thingPk.isFullySet() || !networkPk.isFullySet(),
                "Primary key of Thing ({}) or Network ({}) not fully set!", thingPk, networkPk);

        TableCollection tables = pm.getTableCollection();
        DSLContext dslContext = pm.getDslContext();
        StaTable<?> tlt = tables.getTableForName(NAME_TBL_TN_LINKING_TIME);
        final Field tltId = tlt.field(NAME_COL_ID);
        final Field tltThingId = tlt.field(NAME_COL_THING_ID);
        final Field tltNetworkId = tlt.field(NAME_COL_NETWORK_ID);
        final Field tltTimeStart = tlt.field(NAME_COL_TIME_START);
        final Field tltTimeEnd = tlt.field(NAME_COL_TIME_END);
        Record rcrd = dslContext.select(tltId)
                .from(tlt)
                .where(tltThingId.eq(thingPk.get(0)))
                .and(tltNetworkId.eq(networkPk.get(0)))
                .and(tltTimeEnd.eq(tltTimeStart).or(tltTimeEnd.isNull()))
                .fetchAny();
        if (rcrd == null) {
            LOGGER.info("No open LinkingTime exists between {} and {}", thing, network);
            return;
        }
        Object existingId = rcrd.get(0);
        dslContext.update(tlt)
                .set(tltTimeEnd, SystemClock.currentMoment())
                .where(tltId.eq(existingId))
                .execute();
        LOGGER.debug("Closed LinkingTime {} between {} and {}", existingId, thing, network);
    }
}
