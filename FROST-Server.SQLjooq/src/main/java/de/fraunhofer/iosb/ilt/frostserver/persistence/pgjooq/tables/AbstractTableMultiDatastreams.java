package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
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
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableMultiDatastreams<J extends Comparable> extends StaTableAbstract<J, MultiDatastream, AbstractTableMultiDatastreams<J>> {

    private static final long serialVersionUID = 560943996;

    /**
     * The column <code>public.MULTI_DATASTREAMS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVATION_TYPES</code>.
     */
    public final TableField<Record, JsonValue> colObservationTypes = createField(DSL.name("OBSERVATION_TYPES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeStart = createField(DSL.name("PHENOMENON_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeEnd = createField(DSL.name("PHENOMENON_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeStart = createField(DSL.name("RESULT_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeEnd = createField(DSL.name("RESULT_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

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
     * Create a <code>public.MULTI_DATASTREAMS</code> table reference
     */
    protected AbstractTableMultiDatastreams() {
        this(DSL.name("MULTI_DATASTREAMS"), null);
    }

    protected AbstractTableMultiDatastreams(Name alias, AbstractTableMultiDatastreams<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableMultiDatastreams(Name alias, AbstractTableMultiDatastreams<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.getTableThings(), EntityType.THING)
                        .setSourceFieldAccessor(AbstractTableMultiDatastreams::getThingId)
                        .setTargetFieldAccessor(AbstractTableThings::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableSensors(), EntityType.SENSOR)
                        .setSourceFieldAccessor(AbstractTableMultiDatastreams::getSensorId)
                        .setTargetFieldAccessor(AbstractTableSensors::getId)
        );

        registerRelation(
                new RelationManyToMany<>(this, tables.getTableMultiDatastreamsObsProperties(), tables.getTableObsProperties(), EntityType.OBSERVEDPROPERTY)
                        .setSourceFieldAcc(AbstractTableMultiDatastreams::getId)
                        .setSourceLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getMultiDatastreamId)
                        .setTargetLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getObsPropertyId)
                        .setTargetFieldAcc(AbstractTableObsProperties::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableObservations(), EntityType.OBSERVATION, true)
                        .setSourceFieldAccessor(AbstractTableMultiDatastreams::getId)
                        .setTargetFieldAccessor(AbstractTableObservations::getMultiDatastreamId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        final PropertyFieldRegistry.PropertySetter<AbstractTableMultiDatastreams<J>, MultiDatastream> setterId = (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
            entity.setId(idManager.fromObject(tuple.get(table.getId())));
        };
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableMultiDatastreams::getId, setterId);
        pfReg.addEntry(EntityPropertyMain.SELFLINK, AbstractTableMultiDatastreams::getId, setterId);
        pfReg.addEntry(EntityPropertyMain.NAME, table -> table.colName,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    entity.setName(tuple.get(table.colName));
                });
        pfReg.addEntry(EntityPropertyMain.DESCRIPTION, table -> table.colDescription,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    entity.setDescription(tuple.get(table.colDescription));
                });
        pfReg.addEntry(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, table -> table.colObservationTypes,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    final JsonValue fieldJsonValue = Utils.getFieldJsonValue(tuple, table.colObservationTypes);
                    List<String> observationTypes = fieldJsonValue.getValue(Utils.TYPE_LIST_STRING);
                    dataSize.increase(fieldJsonValue.getStringLength());
                    entity.setMultiObservationDataTypes(observationTypes);
                });
        pfReg.addEntry(EntityPropertyMain.OBSERVEDAREA,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    String observedArea = tuple.get(table.colObservedAreaText);
                    if (observedArea != null) {
                        try {
                            GeoJsonObject area = GeoHelper.parseGeoJson(observedArea);
                            entity.setObservedArea(area);
                        } catch (IOException e) {
                            // It's not a polygon, probably a point or a line.
                        }
                    }
                },
                new NFP<>("s", table -> table.colObservedAreaText));
        pfReg.addEntryNoSelect(EntityPropertyMain.OBSERVEDAREA, "g", table -> table.colObservedArea);
        pfReg.addEntry(EntityPropertyMain.PHENOMENONTIME,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    entity.setPhenomenonTime(Utils.intervalFromTimes(
                            tuple.get(table.colPhenomenonTimeStart),
                            tuple.get(table.colPhenomenonTimeEnd)));
                },
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));
        pfReg.addEntry(EntityPropertyMain.PROPERTIES, table -> table.colProperties,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    JsonValue props = Utils.getFieldJsonValue(tuple, table.colProperties);
                    dataSize.increase(props.getStringLength());
                    entity.setProperties(props.getMapValue());
                });
        pfReg.addEntry(EntityPropertyMain.RESULTTIME,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    entity.setResultTime(Utils.intervalFromTimes(
                            tuple.get(table.colResultTimeStart),
                            tuple.get(table.colResultTimeEnd)));
                },
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colResultTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colResultTimeEnd));
        pfReg.addEntry(EntityPropertyMain.UNITOFMEASUREMENTS, table -> table.colUnitOfMeasurements,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    final JsonValue fieldJsonValue = Utils.getFieldJsonValue(tuple, table.colUnitOfMeasurements);
                    dataSize.increase(fieldJsonValue.getStringLength());
                    List<UnitOfMeasurement> units = fieldJsonValue.getValue(Utils.TYPE_LIST_UOM);
                    entity.setUnitOfMeasurements(units);
                });
        pfReg.addEntry(NavigationPropertyMain.SENSOR, AbstractTableMultiDatastreams::getSensorId,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    entity.setSensor(entityFactories.sensorFromId(tuple, table.getSensorId()));
                });
        pfReg.addEntry(NavigationPropertyMain.THING, AbstractTableMultiDatastreams::getThingId,
                (AbstractTableMultiDatastreams<J> table, Record tuple, MultiDatastream entity, DataSize dataSize) -> {
                    entity.setThing(entityFactories.thingFromId(tuple, table.getThingId()));
                });
        pfReg.addEntry(NavigationPropertyMain.OBSERVEDPROPERTIES, AbstractTableMultiDatastreams::getId, setterId);
        pfReg.addEntry(NavigationPropertyMain.OBSERVATIONS, AbstractTableMultiDatastreams::getId, setterId);
    }

    @Override
    public MultiDatastream newEntity() {
        return new MultiDatastream();
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getSensorId();

    public abstract TableField<Record, J> getThingId();

    @Override
    public abstract AbstractTableMultiDatastreams<J> as(Name as);

    @Override
    public abstract AbstractTableMultiDatastreams<J> as(String alias);

    @Override
    public AbstractTableMultiDatastreams<J> getThis() {
        return this;
    }

}
