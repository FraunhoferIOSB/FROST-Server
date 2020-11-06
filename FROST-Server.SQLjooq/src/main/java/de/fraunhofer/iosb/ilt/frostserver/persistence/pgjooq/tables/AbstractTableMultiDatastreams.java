package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
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

public class AbstractTableMultiDatastreams<J extends Comparable> extends StaTableAbstract<J, AbstractTableMultiDatastreams<J>> {

    private static final long serialVersionUID = 560943996;

    private static AbstractTableMultiDatastreams INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> AbstractTableMultiDatastreams<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new AbstractTableMultiDatastreams(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new AbstractTableMultiDatastreams<>(idType);
    }

    /**
     * The column <code>public.MULTI_DATASTREAMS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVATION_TYPES</code>.
     */
    public final TableField<Record, JsonValue> colObservationTypes = createField(DSL.name("OBSERVATION_TYPES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeStart = createField(DSL.name("PHENOMENON_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeEnd = createField(DSL.name("PHENOMENON_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeStart = createField(DSL.name("RESULT_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeEnd = createField(DSL.name("RESULT_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.UNIT_OF_MEASUREMENTS</code>.
     */
    public final TableField<Record, JsonValue> colUnitOfMeasurements = createField(DSL.name("UNIT_OF_MEASUREMENTS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());
    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVED_AREA</code>.
     */
    public final TableField<Record, Geometry> colObservedArea = createField(DSL.name("OBSERVED_AREA"), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", new PostGisGeometryBinding());

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> colObservedAreaText = DSL.field("ST_AsGeoJSON(?)", String.class, colObservedArea);

    /**
     * The column <code>public.MULTI_DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.MULTI_DATASTREAMS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, J> colSensorId = createField(DSL.name("SENSOR_ID"), getIdType(), this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, J> colThingId = createField(DSL.name("THING_ID"), getIdType(), this);

    /**
     * Create a <code>public.MULTI_DATASTREAMS</code> table reference
     */
    private AbstractTableMultiDatastreams(DataType<J> idType) {
        super(idType, DSL.name("MULTI_DATASTREAMS"), null);
    }

    private AbstractTableMultiDatastreams(Name alias, AbstractTableMultiDatastreams<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, AbstractTableThings.getInstance(getIdType()), EntityType.THING)
                        .setSourceFieldAccessor(AbstractTableMultiDatastreams::getThingId)
                        .setTargetFieldAccessor(AbstractTableThings::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, AbstractTableSensors.getInstance(getIdType()), EntityType.SENSOR)
                        .setSourceFieldAccessor(AbstractTableMultiDatastreams::getSensorId)
                        .setTargetFieldAccessor(AbstractTableSensors::getId)
        );

        registerRelation(
                new RelationManyToManyOrdered<>(this, AbstractTableMultiDatastreamsObsProperties.getInstance(getIdType()), AbstractTableObsProperties.getInstance(getIdType()), EntityType.OBSERVED_PROPERTY)
                        .setOrderFieldAcc((AbstractTableMultiDatastreamsObsProperties<J> table) -> table.colRank)
                        .setAlwaysDistinct(true)
                        .setSourceFieldAcc(AbstractTableMultiDatastreams::getId)
                        .setSourceLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getMultiDatastreamId)
                        .setTargetLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getObsPropertyId)
                        .setTargetFieldAcc(AbstractTableObsProperties::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, AbstractTableObservations.getInstance(getIdType()), EntityType.OBSERVATION, true)
                        .setSourceFieldAccessor(AbstractTableMultiDatastreams::getId)
                        .setTargetFieldAccessor(AbstractTableObservations::getMultiDatastreamId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        pfReg.addEntryId(idManager, AbstractTableMultiDatastreams::getId);
        pfReg.addEntryString(EntityPropertyMain.NAME, table -> table.colName);
        pfReg.addEntryString(EntityPropertyMain.DESCRIPTION, table -> table.colDescription);
        pfReg.addEntry(EntityPropertyMain.OBSERVATIONTYPE, null,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (AbstractTableMultiDatastreams<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            entity.setProperty(EntityPropertyMain.OBSERVATIONTYPE, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation");
                        }, null, null));
        pfReg.addEntry(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, table -> table.colObservationTypes,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (AbstractTableMultiDatastreams<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            final JsonValue fieldJsonValue = Utils.getFieldJsonValue(tuple, table.colObservationTypes);
                            List<String> observationTypes = fieldJsonValue.getValue(Utils.TYPE_LIST_STRING);
                            dataSize.increase(fieldJsonValue.getStringLength());
                            entity.setProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, observationTypes);
                        },
                        (table, entity, insertFields) -> {
                            insertFields.put(table.colObservationTypes, new JsonValue(entity.getProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES)));
                        },
                        (table, entity, updateFields, message) -> {
                            updateFields.put(table.colObservationTypes, new JsonValue(entity.getProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES)));
                            message.addField(EntityPropertyMain.MULTIOBSERVATIONDATATYPES);
                        }));
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
                new PropertyFieldRegistry.ConverterTimeInterval<>(EntityPropertyMain.PHENOMENONTIME_DS, table -> table.colPhenomenonTimeStart, table -> table.colPhenomenonTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));
        pfReg.addEntryMap(EntityPropertyMain.PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(EntityPropertyMain.RESULTTIME_DS,
                new PropertyFieldRegistry.ConverterTimeInterval<>(EntityPropertyMain.PHENOMENONTIME_DS, table -> table.colResultTimeStart, table -> table.colResultTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colResultTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colResultTimeEnd));
        pfReg.addEntry(EntityPropertyMain.UNITOFMEASUREMENTS, table -> table.colUnitOfMeasurements,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (AbstractTableMultiDatastreams<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            final JsonValue fieldJsonValue = Utils.getFieldJsonValue(tuple, table.colUnitOfMeasurements);
                            dataSize.increase(fieldJsonValue.getStringLength());
                            List<UnitOfMeasurement> units = fieldJsonValue.getValue(Utils.TYPE_LIST_UOM);
                            entity.setProperty(EntityPropertyMain.UNITOFMEASUREMENTS, units);
                        },
                        (table, entity, insertFields) -> {
                            insertFields.put(table.colUnitOfMeasurements, new JsonValue(entity.getProperty(EntityPropertyMain.UNITOFMEASUREMENTS)));
                        },
                        (table, entity, updateFields, message) -> {
                            updateFields.put(table.colUnitOfMeasurements, new JsonValue(entity.getProperty(EntityPropertyMain.UNITOFMEASUREMENTS)));
                            message.addField(EntityPropertyMain.UNITOFMEASUREMENTS);
                        }));
        pfReg.addEntry(NavigationPropertyMain.SENSOR, AbstractTableMultiDatastreams::getSensorId, idManager);
        pfReg.addEntry(NavigationPropertyMain.THING, AbstractTableMultiDatastreams::getThingId, idManager);
        pfReg.addEntry(NavigationPropertyMain.OBSERVEDPROPERTIES, AbstractTableMultiDatastreams::getId, idManager);
        pfReg.addEntry(NavigationPropertyMain.OBSERVATIONS, AbstractTableMultiDatastreams::getId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MULTI_DATASTREAM;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    public TableField<Record, J> getSensorId() {
        return colSensorId;
    }

    public TableField<Record, J> getThingId() {
        return colThingId;
    }

    @Override
    public AbstractTableMultiDatastreams<J> as(Name alias) {
        return new AbstractTableMultiDatastreams<>(alias, this);
    }

    @Override
    public AbstractTableMultiDatastreams<J> as(String alias) {
        return new AbstractTableMultiDatastreams<>(DSL.name(alias), this);
    }

    @Override
    public AbstractTableMultiDatastreams<J> getThis() {
        return this;
    }

}
