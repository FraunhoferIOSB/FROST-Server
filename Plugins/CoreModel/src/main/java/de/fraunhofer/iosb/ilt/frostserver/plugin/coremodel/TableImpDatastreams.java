package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurementPartial;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ExpressionFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.GeoHelper;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.geojson.GeoJsonObject;
import org.geolatte.geom.Geometry;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpDatastreams extends StaTableAbstract<TableImpDatastreams> {

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
    private static final String UOM_NO_PATH = "UnitOfMeasurement does not have the path ";

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
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeStart = createField(DSL.name(NAME_COL_PHENOMENONTIMESTART), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeEnd = createField(DSL.name(NAME_COL_PHENOMENONTIMEEND), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeStart = createField(DSL.name(NAME_COL_RESULTTIMESTART), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeEnd = createField(DSL.name(NAME_COL_RESULTTIMEEND), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

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
    public final TableField<Record, Geometry> colObservedArea = createField(DSL.name(NAME_COL_OBSERVEDAREA), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", new PostGisGeometryBinding());

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> colObservedAreaText = DSL.field("ST_AsGeoJSON(?)", String.class, colObservedArea);

    /**
     * The column <code>public.DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name(NAME_COL_PROPERTIES), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

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
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpDatastreams(DataType<?> idType, DataType<?> idTypeOp, DataType<?> idTypeSnsr, DataType<?> idTypeTng, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name(NAME_TABLE), null);
        this.pluginCoreModel = pluginCoreModel;
        colSensorId = createField(DSL.name(NAME_COL_SENSORID), idTypeSnsr.nullable(false));
        colObsPropertyId = createField(DSL.name(NAME_COL_OBSPROPERTYID), idTypeOp.nullable(false));
        colThingId = createField(DSL.name(NAME_COL_THINGID), idTypeTng.nullable(false));
    }

    private TableImpDatastreams(Name alias, TableImpDatastreams aliased, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
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
                .setTargetFieldAccessor(TableImpThings::getId)
        );
        TableImpSensors sensorsTable = tables.getTableForClass(TableImpSensors.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npSensorDatastream, this, sensorsTable)
                .setSourceFieldAccessor(TableImpDatastreams::getSensorId)
                .setTargetFieldAccessor(TableImpSensors::getId)
        );
        TableImpObsProperties obsPropsTable = tables.getTableForClass(TableImpObsProperties.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npObservedPropertyDatastream, this, obsPropsTable)
                .setSourceFieldAccessor(TableImpDatastreams::getObsPropertyId)
                .setTargetFieldAccessor(TableImpObsProperties::getId)
        );
        TableImpObservations observationsTable = tables.getTableForClass(TableImpObservations.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npObservationsDatastream, this, observationsTable)
                .setSourceFieldAccessor(TableImpDatastreams::getId)
                .setTargetFieldAccessor(TableImpObservations::getDatastreamId)
        );
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(TableImpDatastreams::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryString(pluginCoreModel.epObservationType, table -> table.colObservationType);
        pfReg.addEntry(pluginCoreModel.epObservedArea,
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
                new NFP<>("s", table -> table.colObservedAreaText));
        pfReg.addEntryNoSelect(pluginCoreModel.epObservedArea, "g", table -> table.colObservedArea);
        pfReg.addEntry(pluginCoreModel.epPhenomenonTimeDs,
                new ConverterTimeInterval<>(pluginCoreModel.epPhenomenonTimeDs, table -> table.colPhenomenonTimeStart, table -> table.colPhenomenonTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginCoreModel.epResultTimeDs,
                new ConverterTimeInterval<>(pluginCoreModel.epResultTimeDs, table -> table.colResultTimeStart, table -> table.colResultTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colResultTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colResultTimeEnd));
        pfReg.addEntry(pluginCoreModel.epUnitOfMeasurement,
                new ConverterRecordDeflt<>(
                        (table, tuple, entity, dataSize) -> {
                            final UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement(
                                    tuple.get(table.colUnitName),
                                    tuple.get(table.colUnitSymbol),
                                    tuple.get(table.colUnitDefinition));
                            entity.setProperty(pluginCoreModel.epUnitOfMeasurement, unitOfMeasurement);
                        },
                        (table, entity, insertFields) -> {
                            UnitOfMeasurement uom = entity.getProperty(pluginCoreModel.epUnitOfMeasurement);
                            insertFields.put(table.colUnitDefinition, uom.getDefinition());
                            insertFields.put(table.colUnitName, uom.getName());
                            insertFields.put(table.colUnitSymbol, uom.getSymbol());
                        },
                        (table, entity, updateFields, message) -> {
                            UnitOfMeasurement uom = entity.getProperty(pluginCoreModel.epUnitOfMeasurement);
                            updateFields.put(table.colUnitDefinition, uom.getDefinition());
                            updateFields.put(table.colUnitName, uom.getName());
                            updateFields.put(table.colUnitSymbol, uom.getSymbol());
                            message.addField(pluginCoreModel.epUnitOfMeasurement);
                        }),
                new NFP<>("definition", table -> table.colUnitDefinition),
                new NFP<>("name", table -> table.colUnitName),
                new NFP<>("symbol", table -> table.colUnitSymbol)
        );
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
    public PropertyFields<TableImpDatastreams> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final String epName = epCustomSelect.getMainEntityPropertyName();
        final EntityPropertyMain mainEntityProperty = getEntityType().getEntityProperty(epName);
        if (mainEntityProperty == pluginCoreModel.epUnitOfMeasurement) {
            PropertyFields<TableImpDatastreams> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);
            final List<String> subPath = epCustomSelect.getSubPath();
            if (subPath.isEmpty() || subPath.size() > 1) {
                throw new IllegalArgumentException(UOM_NO_PATH + epCustomSelect);
            }
            final ExpressionFactory<TableImpDatastreams> factory = mainPropertyFields.fields.get(subPath.get(0));
            if (factory == null) {
                throw new IllegalArgumentException(UOM_NO_PATH + epCustomSelect);
            }
            final Field field = factory.get(getThis());
            return propertyFieldForUoM(field, epCustomSelect);
        }
        return super.handleEntityPropertyCustomSelect(epCustomSelect);
    }

    @Override
    public TableImpDatastreams getThis() {
        return this;
    }

    protected PropertyFields<TableImpDatastreams> propertyFieldForUoM(final Field field, final EntityPropertyCustomSelect epCustomSelect) {
        PropertyFields<TableImpDatastreams> pfs = new PropertyFields<>(
                epCustomSelect,
                new ConverterRecordDeflt<>(
                        (tbl, tuple, entity, dataSize) -> {
                            final String value = String.valueOf(tuple.get(field));
                            UnitOfMeasurement uom = entity.getProperty(pluginCoreModel.epUnitOfMeasurement);
                            if (uom == null) {
                                uom = new UnitOfMeasurementPartial();
                                entity.setProperty(pluginCoreModel.epUnitOfMeasurement, uom);
                            }
                            switch (epCustomSelect.getSubPath().get(0)) {
                                case "name":
                                    uom.setName(value);
                                    break;
                                case "symbol":
                                    uom.setSymbol(value);
                                    break;
                                case "definition":
                                    uom.setSymbol(value);
                                    break;
                                default:
                                // Do nothing.
                            }
                        },
                        null,
                        null));
        pfs.addField("1", t -> field);
        return pfs;
    }
}
