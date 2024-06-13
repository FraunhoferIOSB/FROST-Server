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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.Arrays;
import java.util.List;
import org.geolatte.geom.Geometry;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpLocations extends StaTableAbstract<TableImpLocations> {

    public static final String NAME_TABLE = "LOCATIONS";
    public static final String NAME_COL_DESCRIPTION = "DESCRIPTION";
    public static final String NAME_COL_ENCODING_TYPE = "ENCODING_TYPE";
    public static final String NAME_COL_GEN_FOI_ID = "GEN_FOI_ID";
    public static final String NAME_COL_GEOM = "GEOM";
    public static final String NAME_COL_ID = "ID";
    public static final String NAME_COL_LOCATION = "LOCATION";
    public static final String NAME_COL_NAME = "NAME";
    public static final String NAME_COL_PROPERTIES = "PROPERTIES";

    private static final long serialVersionUID = -806078255;

    /**
     * The column <code>public.LOCATIONS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name(NAME_COL_DESCRIPTION), SQLDataType.CLOB, this);

    /**
     * The column <code>public.LOCATIONS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name(NAME_COL_ENCODING_TYPE), SQLDataType.CLOB, this);

    /**
     * The column <code>public.LOCATIONS.LOCATION</code>.
     */
    public final TableField<Record, String> colLocation = createField(DSL.name(NAME_COL_LOCATION), SQLDataType.CLOB, this);

    /**
     * The column <code>public.LOCATIONS.GEOM</code>.
     */
    public final TableField<Record, Geometry> colGeom = createField(DSL.name(NAME_COL_GEOM), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", PostGisGeometryBinding.instance());

    /**
     * The column <code>public.LOCATIONS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name(NAME_COL_NAME), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.LOCATIONS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name(NAME_COL_PROPERTIES), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());

    /**
     * The column <code>public.LOCATIONS.ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name(NAME_COL_ID), getIdType(), this);

    /**
     * The column <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final TableField<Record, ?> colGenFoiId;

    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.LOCATIONS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param idTypeFeature The (SQL)DataType of the FEATURE_ID column used in
     * the database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpLocations(DataType<?> idType, DataType<?> idTypeFeature, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name(NAME_TABLE), null, null);
        colGenFoiId = createField(DSL.name(NAME_COL_GEN_FOI_ID), idTypeFeature);
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpLocations(Name alias, TableImpLocations aliased, PluginCoreModel pluginCoreModel) {
        this(alias, aliased, aliased, pluginCoreModel);
    }

    private TableImpLocations(Name alias, TableImpLocations aliased, Table updatedSql, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        colGenFoiId = createField(DSL.name(NAME_COL_GEN_FOI_ID), aliased.colGenFoiId.getDataType());
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        final TableImpThingsLocations tableThingsLoc = tables.getTableForClass(TableImpThingsLocations.class);
        final TableImpThings tableThings = tables.getTableForClass(TableImpThings.class);
        registerRelation(new RelationManyToMany<>(pluginCoreModel.npThingsLocation, this, tableThingsLoc, tableThings)
                .setSourceFieldAcc(TableImpLocations::getId)
                .setSourceLinkFieldAcc(TableImpThingsLocations::getLocationId)
                .setTargetLinkFieldAcc(TableImpThingsLocations::getThingId)
                .setTargetFieldAcc(TableImpThings::getId));
        final TableImpLocationsHistLocations tableLocHistLoc = tables.getTableForClass(TableImpLocationsHistLocations.class);
        final TableImpHistLocations tableHistLoc = tables.getTableForClass(TableImpHistLocations.class);
        registerRelation(new RelationManyToMany<>(pluginCoreModel.npHistoricalLocationsLocation, this, tableLocHistLoc, tableHistLoc)
                .setSourceFieldAcc(TableImpLocations::getId)
                .setSourceLinkFieldAcc(TableImpLocationsHistLocations::getLocationId)
                .setTargetLinkFieldAcc(TableImpLocationsHistLocations::getHistLocationId)
                .setTargetFieldAcc(TableImpHistLocations::getId));
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(TableImpLocations::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryString(ModelRegistry.EP_ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntry(pluginCoreModel.epLocation,
                true,
                new ConverterRecordDeflt<>(
                        (TableImpLocations table, Record tuple, Entity entity, DataSize dataSize) -> {
                            String locationString = tuple.get(table.colLocation);
                            dataSize.increase(locationString == null ? 0 : locationString.length());
                            entity.setProperty(pluginCoreModel.epLocation, Utils.jsonToTreeOrString(locationString));
                        },
                        (table, entity, insertFields) -> {
                            Object feature = entity.getProperty(pluginCoreModel.epLocation);
                            String encodingType = entity.getProperty(ModelRegistry.EP_ENCODINGTYPE);
                            EntityFactories.insertGeometry(insertFields, table.colLocation, table.colGeom, encodingType, feature);
                        },
                        (table, entity, updateFields, message) -> {
                            Object feature = entity.getProperty(pluginCoreModel.epLocation);
                            String encodingType = entity.getProperty(ModelRegistry.EP_ENCODINGTYPE);
                            EntityFactories.insertGeometry(updateFields, table.colLocation, table.colGeom, encodingType, feature);
                            message.addField(pluginCoreModel.epLocation);
                        }),
                new NFP<>("j", table -> table.colLocation));
        pfReg.addEntryNoSelect(pluginCoreModel.epLocation, "g", table -> table.colGeom);
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginCoreModel.npThingsLocation, TableImpLocations::getId);
        pfReg.addEntry(pluginCoreModel.npHistoricalLocationsLocation, TableImpLocations::getId);
        registerHookPostDelete(0, new HookPostDeleteLocation());
        HookPrePostInsertUpdateLocation hook = new HookPrePostInsertUpdateLocation();
        registerHookPreInsert(0, hook);
        registerHookPostInsert(0, hook);
        registerHookPreUpdate(0, hook);
        registerHookPostUpdate(0, hook);

    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.etLocation;
    }

    @Override
    public List<Field> getPkFields() {
        return Arrays.asList(colId);
    }

    public TableField<Record, ?> getId() {
        return colId;
    }

    public TableField<Record, ?> getGenFoiId() {
        return colGenFoiId;
    }

    @Override
    public TableImpLocations as(Name alias) {
        return new TableImpLocations(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpLocations asSecure(String name, JooqPersistenceManager pm) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null || PrincipalExtended.getLocalPrincipal().isAdmin()) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this, pm);
        return new TableImpLocations(DSL.name(name), this, wrappedTable, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpLocations getThis() {
        return this;
    }

}
