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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.time4j.Moment;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpHistLocations extends StaTableAbstract<TableImpHistLocations> {

    public static final String NAME_TABLE = "HIST_LOCATIONS";
    public static final String NAME_COL_ID = "ID";
    public static final String NAME_COL_THINGID = "THING_ID";
    public static final String NAME_COL_TIME = "TIME";

    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpHistLocations.class.getName());
    private static final long serialVersionUID = -1457801967;

    /**
     * The column <code>public.HIST_LOCATIONS.TIME</code>.
     */
    public final TableField<Record, Moment> time = createField(DSL.name(NAME_COL_TIME), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.HIST_LOCATIONS.ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name(NAME_COL_ID), getIdType(), this);

    /**
     * The column <code>public.HIST_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, ?> colThingId;

    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.HIST_LOCATIONS</code> table reference.
     *
     * @param idType The (SQL)DataType of the ID column used in the database.
     * @param idTypeThing The (SQL)DataType of the THING_ID column used in the
     * database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpHistLocations(DataType<?> idType, DataType<?> idTypeThing, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name(NAME_TABLE), null, null);
        this.pluginCoreModel = pluginCoreModel;
        colThingId = createField(DSL.name(NAME_COL_THINGID), idTypeThing);
    }

    private TableImpHistLocations(Name alias, TableImpHistLocations aliased, PluginCoreModel pluginCoreModel) {
        this(alias, aliased, aliased, pluginCoreModel);
    }

    private TableImpHistLocations(Name alias, TableImpHistLocations aliased, Table updatedSql, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        this.pluginCoreModel = pluginCoreModel;
        colThingId = createField(DSL.name(NAME_COL_THINGID), aliased.colThingId.getDataType());
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        TableImpThings tableThings = tables.getTableForClass(TableImpThings.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npThingHistLoc, this, tableThings)
                .setSourceFieldAccessor(TableImpHistLocations::getThingId)
                .setTargetFieldAccessor(TableImpThings::getId));
        final TableImpLocationsHistLocations tableLocHistLoc = tables.getTableForClass(TableImpLocationsHistLocations.class);
        final TableImpLocations tableLocations = tables.getTableForClass(TableImpLocations.class);
        registerRelation(new RelationManyToMany<>(pluginCoreModel.npLocationsHistLoc, this, tableLocHistLoc, tableLocations)
                .setSourceFieldAcc(TableImpHistLocations::getId)
                .setSourceLinkFieldAcc(TableImpLocationsHistLocations::getHistLocationId)
                .setTargetLinkFieldAcc(TableImpLocationsHistLocations::getLocationId)
                .setTargetFieldAcc(TableImpLocations::getId));
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(TableImpHistLocations::getId);
        pfReg.addEntry(pluginCoreModel.epTime, table -> table.time,
                new PropertyFieldRegistry.ConverterTimeInstant<>(pluginCoreModel.epTime, table -> table.time));
        pfReg.addEntry(pluginCoreModel.npThingHistLoc, TableImpHistLocations::getThingId);
        pfReg.addEntry(pluginCoreModel.npLocationsHistLoc, TableImpHistLocations::getId);
    }

    @Override
    public boolean insertIntoDatabase(JooqPersistenceManager pm, Entity histLoc, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException {
        super.insertIntoDatabase(pm, histLoc, updateMode);
        EntityFactories entityFactories = pm.getEntityFactories();
        Entity thing = histLoc.getProperty(pluginCoreModel.npThingHistLoc);
        Object thingId = thing.getPrimaryKeyValues().get(0);
        DSLContext dslContext = pm.getDslContext();
        TableImpHistLocations thl = getTables().getTableForClass(TableImpHistLocations.class);

        final TimeInstant hlTime = histLoc.getProperty(pluginCoreModel.epTime);
        Moment newTime = hlTime.getDateTime();
        // https://github.com/opengeospatial/sensorthings/issues/30
        // Check the time of the latest HistoricalLocation of our thing.
        // If this time is earlier than our time, set the Locations of our Thing to our Locations.
        Record lastHistLocation = dslContext.select(Collections.emptyList())
                .from(thl)
                .where(
                        ((TableField) thl.getThingId()).eq(thingId)
                                .and(thl.time.gt(newTime)))
                .orderBy(thl.time.desc())
                .limit(1)
                .fetchOne();
        if (lastHistLocation == null) {
            // We are the newest.
            // Unlink old Locations from Thing.
            TableImpThingsLocations qtl = getTables().getTableForClass(TableImpThingsLocations.class);
            long count = dslContext
                    .delete(qtl)
                    .where(((TableField) qtl.getThingId()).eq(thingId))
                    .execute();
            LOGGER.debug(EntityFactories.UNLINKED_L_FROM_T, count, thingId);

            // Link new locations to Thing.
            for (Entity l : histLoc.getProperty(pluginCoreModel.npLocationsHistLoc)) {
                if (!l.getPrimaryKeyValues().isFullySet() || !entityFactories.entityExists(pm, l, true)) {
                    throw new NoSuchEntityException("Location with no id.");
                }
                Object locationId = l.getPrimaryKeyValues().get(0);

                dslContext.insertInto(qtl)
                        .set(((TableField) qtl.getThingId()), thingId)
                        .set((qtl.getLocationId()), locationId)
                        .execute();
                LOGGER.debug(EntityFactories.LINKED_L_TO_T, locationId, thingId);
            }
        }

        return true;
    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.etHistoricalLocation;
    }

    @Override
    public List<Field> getPkFields() {
        return Arrays.asList(colId);
    }

    public TableField<Record, ?> getId() {
        return colId;
    }

    public TableField<Record, ?> getThingId() {
        return colThingId;
    }

    @Override
    public TableImpHistLocations as(Name alias) {
        return new TableImpHistLocations(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpHistLocations asSecure(String name, JooqPersistenceManager pm) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null || PrincipalExtended.getLocalPrincipal().isAdmin()) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this, pm);
        return new TableImpHistLocations(DSL.name(name), this, wrappedTable, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpHistLocations getThis() {
        return this;
    }

}
