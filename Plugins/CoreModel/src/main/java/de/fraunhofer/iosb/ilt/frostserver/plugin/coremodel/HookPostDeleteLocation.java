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

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostDelete;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hook that cleans up HistoricalLocations that no longer point to any Location.
 */
class HookPostDeleteLocation implements HookPostDelete {

    private static final Logger LOGGER = LoggerFactory.getLogger(HookPostDeleteLocation.class.getName());

    @ConfigurableField(editor = EditorString.class,
            label = "HistoricalLocations Table")
    @EditorString.EdOptsString(dflt = TableImpHistLocations.NAME_TABLE)
    private String nameTblHistLoc = TableImpHistLocations.NAME_TABLE;

    @ConfigurableField(editor = EditorString.class,
            label = "ID Field in HistoricalLocations Table")
    @EditorString.EdOptsString(dflt = TableImpHistLocations.NAME_COL_ID)
    private String nameFldHistLocId = TableImpHistLocations.NAME_COL_ID;

    @ConfigurableField(editor = EditorString.class,
            label = "Locations-HistoricalLocations LinkTable")
    @EditorString.EdOptsString(dflt = TableImpLocationsHistLocations.NAME_TABLE)
    private String nameTblLocHistLoc = TableImpLocationsHistLocations.NAME_TABLE;

    @ConfigurableField(editor = EditorString.class,
            label = "HistLocID Field in Locations-HistoricalLocations Table")
    @EditorString.EdOptsString(dflt = TableImpLocationsHistLocations.NAME_COL_HISTLOCATIONID)
    private String nameFldLocHistLocHistLocId = TableImpLocationsHistLocations.NAME_COL_HISTLOCATIONID;

    @ConfigurableField(editor = EditorString.class,
            label = "LocID Field in Locations-HistoricalLocations Table")
    @EditorString.EdOptsString(dflt = TableImpLocationsHistLocations.NAME_COL_LOCATIONID)
    private String nameFldLocHistLocLocId = TableImpLocationsHistLocations.NAME_COL_LOCATIONID;

    private StaTable<?> thl;
    private TableField thlId;
    private StaTable<?> tlhl;
    private TableField tlhlHistLocId;
    private TableField tlhlLocId;

    @Override
    public void postDelete(JooqPersistenceManager pm, PkValue entityId) throws NoSuchEntityException {
        final TableCollection tables = pm.getTableCollection();
        // Also postDelete all historicalLocations that no longer reference any location
        if (tlhlLocId == null) {
            thl = tables.getTableForName(nameTblHistLoc);
            tlhl = tables.getTableForName(nameTblLocHistLoc);
            thlId = (TableField) thl.field(nameFldHistLocId);
            tlhlHistLocId = (TableField) tlhl.field(nameFldLocHistLocHistLocId);
            tlhlLocId = (TableField) tlhl.field(nameFldLocHistLocLocId);
        }
        int count = pm.timeExecute(
                pm.getDslContext()
                        .delete(thl)
                        .where((thlId).in(
                                DSL.select(thlId)
                                        .from(thl)
                                        .leftJoin(tlhl)
                                        .on(thlId.eq(tlhlHistLocId))
                                        .where(tlhlLocId.isNull()))),
                "HistoricalLocation");
        LOGGER.debug("Deleted {} HistoricalLocations", count);
    }

    public void setNameTblHistLoc(String nameTblHistLoc) {
        this.nameTblHistLoc = nameTblHistLoc;
    }

    public void setNameFldHistLocId(String nameFldHistLocId) {
        this.nameFldHistLocId = nameFldHistLocId;
    }

    public void setNameTblLocHistLoc(String nameTblLocHistLoc) {
        this.nameTblLocHistLoc = nameTblLocHistLoc;
    }

    public void setNameFldLocHistLocHistLocId(String nameFldLocHistLocHistLocId) {
        this.nameFldLocHistLocHistLocId = nameFldLocHistLocHistLocId;
    }

    public void setNameFldLocHistLocLocId(String nameFldLocHistLocLocId) {
        this.nameFldLocHistLocLocId = nameFldLocHistLocLocId;
    }

}
