package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurementPartial;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
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

public class TableImpDatastreams<J extends Comparable> extends StaTableAbstract<J, TableImpDatastreams<J>> {

    private static final long serialVersionUID = -1460005950;

    private static TableImpDatastreams INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> TableImpDatastreams<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new TableImpDatastreams(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new TableImpDatastreams<>(idType);
    }

    /**
     * The column <code>public.DATASTREAMS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.DATASTREAMS.OBSERVATION_TYPE</code>.
     */
    public final TableField<Record, String> colObservationType = createField(DSL.name("OBSERVATION_TYPE"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeStart = createField(DSL.name("PHENOMENON_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeEnd = createField(DSL.name("PHENOMENON_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeStart = createField(DSL.name("RESULT_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeEnd = createField(DSL.name("RESULT_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_NAME</code>.
     */
    public final TableField<Record, String> colUnitName = createField(DSL.name("UNIT_NAME"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_SYMBOL</code>.
     */
    public final TableField<Record, String> colUnitSymbol = createField(DSL.name("UNIT_SYMBOL"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_DEFINITION</code>.
     */
    public final TableField<Record, String> colUnitDefinition = createField(DSL.name("UNIT_DEFINITION"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.DATASTREAMS.OBSERVED_AREA</code>.
     */
    public final TableField<Record, Geometry> colObservedArea = createField(DSL.name("OBSERVED_AREA"), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", new PostGisGeometryBinding());

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> colObservedAreaText = DSL.field("ST_AsGeoJSON(?)", String.class, colObservedArea);

    /**
     * The column <code>public.DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.DATASTREAMS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType().nullable(false), this);

    /**
     * The column <code>public.DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, J> colSensorId = createField(DSL.name("SENSOR_ID"), getIdType().nullable(false), this);

    /**
     * The column <code>public.DATASTREAMS.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, J> colObsPropertyId = createField(DSL.name("OBS_PROPERTY_ID"), getIdType().nullable(false), this);

    /**
     * The column <code>public.DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, J> colThingId = createField(DSL.name("THING_ID"), getIdType().nullable(false), this);

    /**
     * Create a <code>public.DATASTREAMS</code> table reference
     */
    private TableImpDatastreams(DataType<J> idType) {
        super(idType, DSL.name("DATASTREAMS"), null);
    }

    private TableImpDatastreams(Name alias, TableImpDatastreams<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(new RelationOneToMany<>(this, TableImpThings.getInstance(getIdType()), EntityType.THING)
                        .setSourceFieldAccessor(TableImpDatastreams::getThingId)
                        .setTargetFieldAccessor(TableImpThings::getId)
        );

        registerRelation(new RelationOneToMany<>(this, TableImpSensors.getInstance(getIdType()), EntityType.SENSOR)
                        .setSourceFieldAccessor(TableImpDatastreams::getSensorId)
                        .setTargetFieldAccessor(TableImpSensors::getId)
        );

        registerRelation(new RelationOneToMany<>(this, TableImpObsProperties.getInstance(getIdType()), EntityType.OBSERVED_PROPERTY)
                        .setSourceFieldAccessor(TableImpDatastreams::getObsPropertyId)
                        .setTargetFieldAccessor(TableImpObsProperties::getId)
        );

        registerRelation(new RelationOneToMany<>(this, TableImpObservations.getInstance(getIdType()), EntityType.OBSERVATION, true)
                        .setSourceFieldAccessor(TableImpDatastreams::getId)
                        .setTargetFieldAccessor(TableImpObservations::getDatastreamId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        pfReg.addEntryId(idManager, TableImpDatastreams::getId);
        pfReg.addEntryString(EntityPropertyMain.NAME, table -> table.colName);
        pfReg.addEntryString(EntityPropertyMain.DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryString(EntityPropertyMain.OBSERVATIONTYPE, table -> table.colObservationType);
        pfReg.addEntry(EntityPropertyMain.OBSERVEDAREA,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (table, tuple, entity, dataSize) -> {
                            String observedArea = tuple.get(table.colObservedAreaText);
                            if (observedArea != null) {
                                try {
                                    GeoJsonObject area = GeoHelper.parseGeoJson(observedArea);
                                    entity.setProperty(EntityPropertyMain.OBSERVEDAREA, area);
                                } catch (IOException e) {
                                    // It's not a polygon, probably a point or a line.
                                }
                            }
                        }, null, null),
                new NFP<>("s", table -> table.colObservedAreaText));
        pfReg.addEntryNoSelect(EntityPropertyMain.OBSERVEDAREA, "g", table -> table.colObservedArea);
        pfReg.addEntry(EntityPropertyMain.PHENOMENONTIME_DS,
                new ConverterTimeInterval<>(EntityPropertyMain.PHENOMENONTIME_DS, table -> table.colPhenomenonTimeStart, table -> table.colPhenomenonTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));
        pfReg.addEntryMap(EntityPropertyMain.PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(EntityPropertyMain.RESULTTIME_DS,
                new ConverterTimeInterval<>(EntityPropertyMain.PHENOMENONTIME_DS, table -> table.colResultTimeStart, table -> table.colResultTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colResultTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colResultTimeEnd));
        pfReg.addEntry(EntityPropertyMain.UNITOFMEASUREMENT,
                new ConverterRecordDeflt<>(
                        (table, tuple, entity, dataSize) -> {
                            final UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement(
                                    tuple.get(table.colUnitName),
                                    tuple.get(table.colUnitSymbol),
                                    tuple.get(table.colUnitDefinition));
                            entity.setProperty(EntityPropertyMain.UNITOFMEASUREMENT, unitOfMeasurement);
                        },
                        (table, entity, insertFields) -> {
                            UnitOfMeasurement uom = entity.getProperty(EntityPropertyMain.UNITOFMEASUREMENT);
                            insertFields.put(table.colUnitDefinition, uom.getDefinition());
                            insertFields.put(table.colUnitName, uom.getName());
                            insertFields.put(table.colUnitSymbol, uom.getSymbol());
                        },
                        (table, entity, updateFields, message) -> {
                            UnitOfMeasurement uom = entity.getProperty(EntityPropertyMain.UNITOFMEASUREMENT);
                            updateFields.put(table.colUnitDefinition, uom.getDefinition());
                            updateFields.put(table.colUnitName, uom.getName());
                            updateFields.put(table.colUnitSymbol, uom.getSymbol());
                            message.addField(EntityPropertyMain.UNITOFMEASUREMENT);
                        }),
                new NFP<>("definition", table -> table.colUnitDefinition),
                new NFP<>("name", table -> table.colUnitName),
                new NFP<>("symbol", table -> table.colUnitSymbol)
        );
        pfReg.addEntry(NavigationPropertyMain.SENSOR, TableImpDatastreams::getSensorId, idManager);
        pfReg.addEntry(NavigationPropertyMain.OBSERVEDPROPERTY, TableImpDatastreams::getObsPropertyId, idManager);
        pfReg.addEntry(NavigationPropertyMain.THING, TableImpDatastreams::getThingId, idManager);
        pfReg.addEntry(NavigationPropertyMain.OBSERVATIONS, TableImpDatastreams::getId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DATASTREAM;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    public TableField<Record, J> getSensorId() {
        return colSensorId;
    }

    public TableField<Record, J> getObsPropertyId() {
        return colObsPropertyId;
    }

    public TableField<Record, J> getThingId() {
        return colThingId;
    }

    @Override
    public TableImpDatastreams<J> as(String alias) {
        return new TableImpDatastreams<>(DSL.name(alias), this);
    }

    @Override
    public TableImpDatastreams<J> as(Name alias) {
        return new TableImpDatastreams<>(alias, this);
    }

    @Override
    public PropertyFields<TableImpDatastreams<J>> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == EntityPropertyMain.UNITOFMEASUREMENT) {
            PropertyFields<TableImpDatastreams<J>> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);
            final List<String> subPath = epCustomSelect.getSubPath();
            if (subPath.size() > 1) {
                throw new IllegalArgumentException("UnitOfMeasurement does not have the path " + epCustomSelect);
            }
            final Field field = mainPropertyFields.fields.get(subPath.get(0)).get(getThis());
            if (field == null) {
                throw new IllegalArgumentException("UnitOfMeasurement does not have the path " + epCustomSelect);
            }
            return propertyFieldForUoM(field, epCustomSelect);
        }
        return super.handleEntityPropertyCustomSelect(epCustomSelect);
    }

    @Override
    public TableImpDatastreams<J> getThis() {
        return this;
    }

    protected PropertyFields<TableImpDatastreams<J>> propertyFieldForUoM(final Field field, final EntityPropertyCustomSelect epCustomSelect) {
        PropertyFields<TableImpDatastreams<J>> pfs = new PropertyFields<>(
                epCustomSelect,
                new ConverterRecordDeflt<>(
                        (tbl, tuple, entity, dataSize) -> {
                            final String value = String.valueOf(tuple.get(field));
                            UnitOfMeasurement uom = entity.getProperty(EntityPropertyMain.UNITOFMEASUREMENT);
                            if (uom == null) {
                                uom = new UnitOfMeasurementPartial();
                                entity.setProperty(EntityPropertyMain.UNITOFMEASUREMENT, uom);
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
                            }
                        },
                        null,
                        null));
        pfs.addField("1", t -> field);
        return pfs;
    }
}
