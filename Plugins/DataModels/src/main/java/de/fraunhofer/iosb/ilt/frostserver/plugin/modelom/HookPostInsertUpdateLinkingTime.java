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

import static de.fraunhofer.iosb.ilt.frostserver.plugin.modelom.LinkingTimeValidator.NAME_COL_ID;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.modelom.LinkingTimeValidator.NAME_COL_NETWORK_ID;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.modelom.LinkingTimeValidator.NAME_COL_THING_ID;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.modelom.LinkingTimeValidator.NAME_COL_TIME_END;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.modelom.LinkingTimeValidator.NAME_COL_TIME_START;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.modelom.LinkingTimeValidator.NAME_TBL_NETWORK_THINGS;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.modelom.LinkingTimeValidator.NAME_TBL_TN_LINKING_TIME;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostUpdate;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.request.EditFeatures;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hook to update Thing-Location relations when the a new HistoricalLocation is
 * added to a Thing.
 */
class HookPostInsertUpdateLinkingTime implements HookPostInsert, HookPostUpdate {

    private static final Logger LOGGER = LoggerFactory.getLogger(HookPostInsertUpdateLinkingTime.class.getName());

    @ConfigurableField(editor = EditorString.class,
            label = "Things Locations LinkTable")
    @EditorString.EdOptsString(dflt = NAME_TBL_TN_LINKING_TIME)
    private String tltName = NAME_TBL_TN_LINKING_TIME;

    @Override
    public boolean postInsertIntoDatabase(JooqPersistenceManager pm, Entity lt, Map<Field, Object> insertFields) throws NoSuchEntityException, IncompleteEntityException {
        checkLinks(pm, lt);
        return true;
    }

    private void checkLinks(JooqPersistenceManager pm, Entity lt) throws DataAccessException {
        final TableCollection tc = pm.getTableCollection();
        final EntityType etLinkingTime = lt.getType();
        final NavigationPropertyEntity npThing = (NavigationPropertyEntity) etLinkingTime.getNavigationProperty("Thing");
        final NavigationPropertyEntity npNetwork = (NavigationPropertyEntity) etLinkingTime.getNavigationProperty("MonitoringNetwork");

        Entity ltFull = pm.get(etLinkingTime, lt.getPrimaryKeyValues());
        Entity thing = ltFull.getProperty(npThing);
        Entity network = ltFull.getProperty(npNetwork);
        LOGGER.debug("Handling LinkingTime {} between {} and {}.", ltFull, thing, network);
        Object thingId = thing.getPrimaryKeyValues().get(0);
        Object networkId = network.getPrimaryKeyValues().get(0);

        StaTable<?> tlt = tc.getTableForName(tltName);
        final Field tltId = tlt.field(NAME_COL_ID);
        final Field tltThingId = tlt.field(NAME_COL_THING_ID);
        final Field tltNetworkId = tlt.field(NAME_COL_NETWORK_ID);
        final Field tltTimeStart = tlt.field(NAME_COL_TIME_START);
        final Field tltTimeEnd = tlt.field(NAME_COL_TIME_END);
        DSLContext dslContext = pm.getDslContext();
        Object openLtId = dslContext.select(tltId)
                .from(tlt)
                .where(tltThingId.eq(thingId))
                .and(tltNetworkId.eq(networkId))
                .and(tltTimeEnd.eq(tltTimeStart).or(tltTimeEnd.isNull()))
                .fetchOne(0);
        boolean existsOpenLt = openLtId != null;

        StaTable<?> ttn = tc.getTableForName(NAME_TBL_NETWORK_THINGS);
        final Field ttnThingId = ttn.field("thing_id");
        final Field ttnNetworkId = ttn.field("network_id");
        boolean existsLink = dslContext.selectOne()
                .from(ttn)
                .where(ttnThingId.eq(thingId))
                .and(ttnNetworkId.eq(networkId))
                .fetchOne(0) != null;

        if (existsOpenLt && !existsLink) {
            LOGGER.debug("An open LinkingTime exists between {} and {}, but no direct link. Creating link.", thing, network);
            int count = dslContext.insertInto(ttn)
                    .set(ttnThingId, thingId)
                    .set(ttnNetworkId, networkId)
                    .execute();
            if (count != 1) {
                LOGGER.warn("Failed to create link betweeen {} and {}", thing, network);
            }
        }
        if (!existsOpenLt && existsLink) {
            LOGGER.debug("No open LinkingTime exists between {} and {}, but there is a direct link. Removing link.", thing, network);
            dslContext.deleteFrom(ttn)
                    .where(ttnThingId.eq(thingId))
                    .and(ttnNetworkId.eq(networkId))
                    .execute();
        }
    }

    @Override
    public void postUpdateInDatabase(JooqPersistenceManager pm, Entity lt, PkValue entityId, EditFeatures updateMode) throws NoSuchEntityException, IncompleteEntityException {
        checkLinks(pm, lt);
    }

}
