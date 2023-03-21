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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.Collections;
import net.time4j.Moment;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
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
    public final TableField<Record, Moment> time = createField(DSL.name(NAME_COL_TIME), SQLDataType.TIMESTAMP, this, "", new MomentBinding());

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
        super(idType, DSL.name(NAME_TABLE), null);
        this.pluginCoreModel = pluginCoreModel;
        colThingId = createField(DSL.name(NAME_COL_THINGID), idTypeThing);
    }

    private TableImpHistLocations(Name alias, TableImpHistLocations aliased, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
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
    public boolean insertIntoDatabase(PostgresPersistenceManager pm, Entity histLoc) throws NoSuchEntityException, IncompleteEntityException {
        super.insertIntoDatabase(pm, histLoc);
        EntityFactories entityFactories = pm.getEntityFactories();
        Entity thing = histLoc.getProperty(pluginCoreModel.npThingHistLoc);
        Object thingId = thing.getId().getValue();
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
                if (l.getId() == null || !entityFactories.entityExists(pm, l)) {
                    throw new NoSuchEntityException("Location with no id.");
                }
                Object locationId = l.getId().getValue();

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
    public TableImpHistLocations getThis() {
        return this;
    }

}
