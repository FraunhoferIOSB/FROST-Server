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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpThings extends StaTableAbstract<TableImpThings> {

    public static final String NAME_TABLE = "THINGS";
    public static final String NAME_COL_DESCRIPTION = "DESCRIPTION";
    public static final String NAME_COL_ID = "ID";
    public static final String NAME_COL_NAME = "NAME";
    public static final String NAME_COL_PROPERTIES = "PROPERTIES";

    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpThings.class.getName());
    private static final long serialVersionUID = -729589982;

    /**
     * The column <code>public.THINGS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name(NAME_COL_DESCRIPTION), SQLDataType.CLOB, this);

    /**
     * The column <code>public.THINGS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name(NAME_COL_PROPERTIES), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());

    /**
     * The column <code>public.THINGS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name(NAME_COL_NAME), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.THINGS.ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name(NAME_COL_ID), getIdType(), this);

    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.THINGS</code> table reference
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpThings(DataType<?> idType, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name(NAME_TABLE), null, null);
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpThings(Name alias, TableImpThings aliased, PluginCoreModel pluginCoreModel) {
        this(alias, aliased, aliased, pluginCoreModel);
    }

    private TableImpThings(Name alias, TableImpThings aliased, Table updatedSql, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        final TableImpDatastreams tableDs = tables.getTableForClass(TableImpDatastreams.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npDatastreamsThing, this, tableDs)
                .setSourceFieldAccessor(TableImpThings::getId)
                .setTargetFieldAccessor(TableImpDatastreams::getThingId));
        final TableImpHistLocations tableHistLoc = tables.getTableForClass(TableImpHistLocations.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npHistoricalLocationsThing, this, tableHistLoc)
                .setSourceFieldAccessor(TableImpThings::getId)
                .setTargetFieldAccessor(TableImpHistLocations::getThingId));
        final TableImpThingsLocations tableThingsLocs = tables.getTableForClass(TableImpThingsLocations.class);
        final TableImpLocations tableLocs = tables.getTableForClass(TableImpLocations.class);
        registerRelation(new RelationManyToMany<>(pluginCoreModel.npLocationsThing, this, tableThingsLocs, tableLocs)
                .setSourceFieldAcc(TableImpThings::getId)
                .setSourceLinkFieldAcc(TableImpThingsLocations::getThingId)
                .setTargetLinkFieldAcc(TableImpThingsLocations::getLocationId)
                .setTargetFieldAcc(TableImpLocations::getId));
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(TableImpThings::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginCoreModel.npDatastreamsThing, TableImpThings::getId);
        pfReg.addEntry(pluginCoreModel.npHistoricalLocationsThing, TableImpThings::getId);
        pfReg.addEntry(pluginCoreModel.npLocationsThing, TableImpThings::getId);
    }

    @Override
    protected void updateNavigationPropertySet(Entity thing, EntitySet linkedSet, JooqPersistenceManager pm, UpdateMode updateMode) throws IncompleteEntityException, NoSuchEntityException {
        final ModelRegistry modelRegistry = getModelRegistry();
        EntityType linkedEntityType = linkedSet.getEntityType();
        if (linkedEntityType.equals(pluginCoreModel.etLocation)) {
            final TableCollection tables = getTables();
            // We know a Thing has a single-valued PK.
            Object thingId = thing.getPrimaryKeyValues().get(0);
            DSLContext dslContext = pm.getDslContext();
            EntityFactories entityFactories = pm.getEntityFactories();
            TableImpThingsLocations ttl = tables.getTableForClass(TableImpThingsLocations.class);

            // Unlink old Locations from Thing.
            long count = dslContext.delete(ttl).where(((TableField) ttl.getThingId()).eq(thingId)).execute();
            LOGGER.debug(EntityFactories.UNLINKED_L_FROM_T, count, thingId);

            // Maybe Create new Locations and link them to this Thing.
            List<Object> locationIds = new ArrayList<>();
            boolean admin = PrincipalExtended.getLocalPrincipal().isAdmin();
            for (Entity l : linkedSet) {
                if (updateMode.createAndLinkNew) {
                    entityFactories.entityExistsOrCreate(pm, l, updateMode);
                } else if (!entityFactories.entityExists(pm, l, admin)) {
                    throw new NoSuchEntityException("Linked Location with no id.");
                }
                PkValue lPk = l.getPrimaryKeyValues();
                Object lId = lPk.get(0);

                dslContext.insertInto(ttl)
                        .set((TableField) ttl.getThingId(), thingId)
                        .set(ttl.getLocationId(), lId)
                        .execute();
                LOGGER.debug(EntityFactories.LINKED_L_TO_T, lId, thingId);
                locationIds.add(lId);
            }

            // Now link the new locations also to a historicalLocation.
            if (!locationIds.isEmpty()) {
                // Insert a new HL into the DB
                TableImpHistLocations qhl = tables.getTableForClass(TableImpHistLocations.class);
                Object histLocationId = dslContext.insertInto(qhl)
                        .set((TableField) qhl.getThingId(), thingId)
                        .set(qhl.time, Moment.nowInSystemTime())
                        .returningResult(qhl.getId())
                        .fetchOne(0);
                LOGGER.debug(EntityFactories.CREATED_HL, histLocationId);

                // Link the locations to the new HL
                TableImpLocationsHistLocations qlhl = tables.getTableForClass(TableImpLocationsHistLocations.class);
                for (Object locId : locationIds) {
                    dslContext.insertInto(qlhl)
                            .set(((TableField) qlhl.getHistLocationId()), histLocationId)
                            .set((qlhl.getLocationId()), locId)
                            .execute();
                    LOGGER.debug(EntityFactories.LINKED_L_TO_HL, locId, histLocationId);
                }

                // Send a message about the creation of a new HL
                Entity newHl = pm.get(pluginCoreModel.etHistoricalLocation, PkValue.of(histLocationId));
                newHl.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(newHl.getEntityType()));
                pm.getEntityChangedMessages().add(
                        new EntityChangedMessage()
                                .setEventType(EntityChangedMessage.Type.CREATE)
                                .setEntity(newHl));
            }
            return;
        }
        super.updateNavigationPropertySet(thing, linkedSet, pm, updateMode);
    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.etThing;
    }

    @Override
    public List<Field> getPkFields() {
        return Arrays.asList(colId);
    }

    public TableField<Record, ?> getId() {
        return colId;
    }

    @Override
    public TableImpThings as(Name alias) {
        return new TableImpThings(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpThings asSecure(String name, JooqPersistenceManager pm) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null || PrincipalExtended.getLocalPrincipal().isAdmin()) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this, pm);
        return new TableImpThings(DSL.name(name), this, wrappedTable, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpThings getThis() {
        return this;
    }

}
