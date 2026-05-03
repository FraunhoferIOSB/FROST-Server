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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.FieldFetcher;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.util.GeoHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.time4j.Moment;
import org.geojson.GeoJsonObject;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpDatastreams extends StaTableAbstract<TableImpDatastreams> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpDatastreams.class.getName());

    public static final String NAME_TABLE = "DATASTREAMS";
    public static final String NAME_COL_DESCRIPTION = "DESCRIPTION";
    public static final String NAME_COL_ID = "ID";
    public static final String NAME_COL_NAME = "NAME";
    public static final String NAME_COL_OBSERVEDAREA = "OBSERVED_AREA";
    public static final String NAME_COL_OBSERVATIONTYPE = "OBSERVATION_TYPE";
    public static final String NAME_COL_OBSPROPERTYID = "OBS_PROPERTY_ID";
    public static final String NAME_COL_PHENOMENONTIMESTART = "PHENOMENON_TIME_START";
    public static final String NAME_COL_PHENOMENONTIMEEND = "PHENOMENON_TIME_END";
    public static final String NAME_COL_PROPERTIES = "PROPERTIES";
    public static final String NAME_COL_RESULTTIMESTART = "RESULT_TIME_START";
    public static final String NAME_COL_RESULTTIMEEND = "RESULT_TIME_END";
    public static final String NAME_COL_SENSORID = "SENSOR_ID";
    public static final String NAME_COL_THINGID = "THING_ID";
    public static final String NAME_COL_UNITDEFINITION = "UNIT_DEFINITION";
    public static final String NAME_COL_UNITNAME = "UNIT_NAME";
    public static final String NAME_COL_UNITSYMBOL = "UNIT_SYMBOL";

    private static final long serialVersionUID = -1460005950;

    /**
     * The column <code>public.DATASTREAMS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name(NAME_COL_DESCRIPTION), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.DATASTREAMS.OBSERVATION_TYPE</code>.
     */
    public final TableField<Record, String> colObservationType = createField(DSL.name(NAME_COL_OBSERVATIONTYPE), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, Moment> colPhenomenonTimeStart = createField(DSL.name(NAME_COL_PHENOMENONTIMESTART), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, Moment> colPhenomenonTimeEnd = createField(DSL.name(NAME_COL_PHENOMENONTIMEEND), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<Record, Moment> colResultTimeStart = createField(DSL.name(NAME_COL_RESULTTIMESTART), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<Record, Moment> colResultTimeEnd = createField(DSL.name(NAME_COL_RESULTTIMEEND), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.DATASTREAMS.UNIT_NAME</code>.
     */
    public final TableField<Record, String> colUnitName = createField(DSL.name(NAME_COL_UNITNAME), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_SYMBOL</code>.
     */
    public final TableField<Record, String> colUnitSymbol = createField(DSL.name(NAME_COL_UNITSYMBOL), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_DEFINITION</code>.
     */
    public final TableField<Record, String> colUnitDefinition = createField(DSL.name(NAME_COL_UNITDEFINITION), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name(NAME_COL_NAME), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.DATASTREAMS.OBSERVED_AREA</code>.
     */
    public final TableField<Record, Geometry> colObservedArea = createField(DSL.name(NAME_COL_OBSERVEDAREA), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", PostGisGeometryBinding.instance());

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> colObservedAreaText = DSL.field("ST_AsGeoJSON(?)", String.class, colObservedArea);

    /**
     * The column <code>public.DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name(NAME_COL_PROPERTIES), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());

    /**
     * The column <code>public.DATASTREAMS.ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name(NAME_COL_ID), getIdType().nullable(false), this);

    /**
     * The column <code>public.DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, ?> colSensorId;

    /**
     * The column <code>public.DATASTREAMS.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, ?> colObsPropertyId;

    /**
     * The column <code>public.DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, ?> colThingId;

    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.DATASTREAMS</code> table reference.
     *
     * @param idType The (SQL)DataType of the ID column used in the database.
     * @param idTypeOp The (SQL)DataType of the OBS_PROPERTY_ID column used in
     * the database.
     * @param idTypeSnsr The (SQL)DataType of the SENSOR_ID column used in the
     * database.
     * @param idTypeTng The (SQL)DataType of the THING_ID column used in the
     * database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpDatastreams(DataType<?> idType, DataType<?> idTypeOp, DataType<?> idTypeSnsr, DataType<?> idTypeTng, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name(NAME_TABLE), null, null);
        this.pluginCoreModel = pluginCoreModel;
        colSensorId = createField(DSL.name(NAME_COL_SENSORID), idTypeSnsr.nullable(false));
        colObsPropertyId = createField(DSL.name(NAME_COL_OBSPROPERTYID), idTypeOp.nullable(false));
        colThingId = createField(DSL.name(NAME_COL_THINGID), idTypeTng.nullable(false));
    }

    private TableImpDatastreams(Name alias, TableImpDatastreams aliased, PluginCoreModel pluginCoreModel) {
        this(alias, aliased, aliased, pluginCoreModel);
    }

    private TableImpDatastreams(Name alias, TableImpDatastreams aliased, Table updatedSql, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        this.pluginCoreModel = pluginCoreModel;
        colSensorId = createField(DSL.name(NAME_COL_SENSORID), aliased.colSensorId.getDataType().nullable(false));
        colObsPropertyId = createField(DSL.name(NAME_COL_OBSPROPERTYID), aliased.colObsPropertyId.getDataType().nullable(false));
        colThingId = createField(DSL.name(NAME_COL_THINGID), aliased.colThingId.getDataType().nullable(false));
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        TableImpThings thingsTable = tables.getTableForClass(TableImpThings.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npThingDatasteam, this, thingsTable)
                .setSourceFieldAccessor(TableImpDatastreams::getThingId)
                .setTargetFieldAccessor(TableImpThings::getId));
        TableImpSensors sensorsTable = tables.getTableForClass(TableImpSensors.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npSensorDatastream, this, sensorsTable)
                .setSourceFieldAccessor(TableImpDatastreams::getSensorId)
                .setTargetFieldAccessor(TableImpSensors::getId));
        TableImpObsProperties obsPropsTable = tables.getTableForClass(TableImpObsProperties.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npObservedPropertyDatastream, this, obsPropsTable)
                .setSourceFieldAccessor(TableImpDatastreams::getObsPropertyId)
                .setTargetFieldAccessor(TableImpObsProperties::getId));
        TableImpObservations observationsTable = tables.getTableForClass(TableImpObservations.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npObservationsDatastream, this, observationsTable)
                .setSourceFieldAccessor(TableImpDatastreams::getId)
                .setTargetFieldAccessor(TableImpObservations::getDatastreamId));
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(TableImpDatastreams::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryString(pluginCoreModel.epObservationType, table -> table.colObservationType);
        pfReg.addEntryMap(StandardProperties.EP_PROPERTIES, table -> table.colProperties);

        pfReg.addEntry(pluginCoreModel.epObservedArea, true,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (table, tuple, entity, dataSize) -> {
                            String observedArea = tuple.get(table.colObservedAreaText);
                            if (observedArea != null) {
                                try {
                                    GeoJsonObject area = GeoHelper.parseGeoJson(observedArea);
                                    entity.setProperty(pluginCoreModel.epObservedArea, area);
                                } catch (IOException e) {
                                    // It's not a polygon, probably a point or a line.
                                }
                            }
                        }, null, null),
                new NFP<>("s", table -> table.colObservedAreaText),
                new NFP<>("j", table -> table.colObservedAreaText),
                new NFP<>("g", table -> table.colObservedArea));

        pfReg.addEntryTimeInterval(
                pluginCoreModel.epPhenomenonTimeDs,
                table -> table.colPhenomenonTimeStart,
                table -> table.colPhenomenonTimeEnd);

        pfReg.addEntryTimeInterval(
                pluginCoreModel.epResultTimeDs,
                table -> table.colResultTimeStart,
                table -> table.colResultTimeEnd);

        final FieldFetcher<TableImpDatastreams> efUomName = table -> table.colUnitName;
        final FieldFetcher<TableImpDatastreams> efUomDefinition = table -> table.colUnitDefinition;
        final FieldFetcher<TableImpDatastreams> efUomSymbol = table -> table.colUnitSymbol;
        pfReg.addEntry(pluginCoreModel.epUnitOfMeasurement, new ConverterRecordDeflt<>())
                .addSubProperty(pfReg.createEntryString(StandardProperties.EP_NAME, efUomName))
                .addSubProperty(pfReg.createEntryString(StandardProperties.EP_DEFINITION, efUomDefinition))
                .addSubProperty(pfReg.createEntryString(StandardProperties.EP_SYMBOL, efUomSymbol));

        pfReg.addEntry(pluginCoreModel.npSensorDatastream, TableImpDatastreams::getSensorId);
        pfReg.addEntry(pluginCoreModel.npObservedPropertyDatastream, TableImpDatastreams::getObsPropertyId);
        pfReg.addEntry(pluginCoreModel.npThingDatasteam, TableImpDatastreams::getThingId);
        pfReg.addEntry(pluginCoreModel.npObservationsDatastream, TableImpDatastreams::getId);
    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.etDatastream;
    }

    @Override
    public List<Field> getPkFields() {
        return Arrays.asList(colId);
    }

    public TableField<Record, ?> getId() {
        return colId;
    }

    public TableField<Record, ?> getSensorId() {
        return colSensorId;
    }

    public TableField<Record, ?> getObsPropertyId() {
        return colObsPropertyId;
    }

    public TableField<Record, ?> getThingId() {
        return colThingId;
    }

    @Override
    public TableImpDatastreams as(Name alias) {
        return new TableImpDatastreams(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpDatastreams asSecure(String name, JooqPersistenceManager pm) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null || PrincipalExtended.getLocalPrincipal().isAdmin()) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this, pm);
        return new TableImpDatastreams(DSL.name(name), this, wrappedTable, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpDatastreams getThis() {
        return this;
    }

}
