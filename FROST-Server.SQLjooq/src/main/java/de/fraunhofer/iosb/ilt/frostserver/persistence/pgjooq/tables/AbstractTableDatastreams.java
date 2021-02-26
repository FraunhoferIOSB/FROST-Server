package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertySetter;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
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

public abstract class AbstractTableDatastreams<J extends Comparable> extends StaTableAbstract<J, Datastream, AbstractTableDatastreams<J>> {

    private static final long serialVersionUID = -1460005950;

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
     * Create a <code>public.DATASTREAMS</code> table reference
     */
    protected AbstractTableDatastreams() {
        this(DSL.name("DATASTREAMS"), null);
    }

    protected AbstractTableDatastreams(Name alias, AbstractTableDatastreams<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableDatastreams(Name alias, AbstractTableDatastreams<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, tables.getTableThings(), EntityType.THING)
                        .setSourceFieldAccessor(AbstractTableDatastreams::getThingId)
                        .setTargetFieldAccessor(AbstractTableThings::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableSensors(), EntityType.SENSOR)
                        .setSourceFieldAccessor(AbstractTableDatastreams::getSensorId)
                        .setTargetFieldAccessor(AbstractTableSensors::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableObsProperties(), EntityType.OBSERVEDPROPERTY)
                        .setSourceFieldAccessor(AbstractTableDatastreams::getObsPropertyId)
                        .setTargetFieldAccessor(AbstractTableObsProperties::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableObservations(), EntityType.OBSERVATION, true)
                        .setSourceFieldAccessor(AbstractTableDatastreams::getId)
                        .setTargetFieldAccessor(AbstractTableObservations::getDatastreamId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        final PropertySetter<AbstractTableDatastreams<J>, Datastream> setterId
                = (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> entity.setId(idManager.fromObject(tuple.get(table.getId())));
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableDatastreams::getId, setterId);
        pfReg.addEntry(
                EntityPropertyMain.SELFLINK,
                AbstractTableDatastreams::getId,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> entity.setId(idManager.fromObject(tuple.get(table.getId()))));
        pfReg.addEntry(
                EntityPropertyMain.NAME,
                table -> table.colName,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> entity.setName(tuple.get(table.colName)));
        pfReg.addEntry(
                EntityPropertyMain.DESCRIPTION,
                table -> table.colDescription,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> entity.setDescription(tuple.get(table.colDescription)));
        pfReg.addEntry(
                EntityPropertyMain.OBSERVATIONTYPE,
                table -> table.colObservationType,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> entity.setObservationType(tuple.get(table.colObservationType)));
        pfReg.addEntry(EntityPropertyMain.OBSERVEDAREA,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> {
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
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize)
                -> entity.setPhenomenonTime(Utils.intervalFromTimes(
                        tuple.get(table.colPhenomenonTimeStart),
                        tuple.get(table.colPhenomenonTimeEnd))),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));

        pfReg.addEntry(EntityPropertyMain.PROPERTIES, table -> table.colProperties,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> {
                    JsonValue props = Utils.getFieldJsonValue(tuple, table.colProperties);
                    dataSize.increase(props.getStringLength());
                    entity.setProperties(props.getMapValue());
                });
        pfReg.addEntry(EntityPropertyMain.RESULTTIME,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize)
                -> entity.setResultTime(Utils.intervalFromTimes(
                        tuple.get(table.colResultTimeStart),
                        tuple.get(table.colResultTimeEnd))),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colResultTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colResultTimeEnd));
        pfReg.addEntry(EntityPropertyMain.UNITOFMEASUREMENT,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> {
                    final UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement(
                            tuple.get(table.colUnitName),
                            tuple.get(table.colUnitSymbol),
                            tuple.get(table.colUnitDefinition));
                    entity.setUnitOfMeasurement(unitOfMeasurement);
                },
                new NFP<>("definition", table -> table.colUnitDefinition),
                new NFP<>("name", table -> table.colUnitName),
                new NFP<>("symbol", table -> table.colUnitSymbol)
        );
        pfReg.addEntry(
                NavigationPropertyMain.SENSOR,
                AbstractTableDatastreams::getSensorId,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> entity.setSensor(entityFactories.sensorFromId(tuple, table.getSensorId())));
        pfReg.addEntry(
                NavigationPropertyMain.OBSERVEDPROPERTY,
                AbstractTableDatastreams::getObsPropertyId,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> entity.setObservedProperty(entityFactories.observedProperyFromId(tuple, table.getSensorId())));
        pfReg.addEntry(
                NavigationPropertyMain.THING,
                AbstractTableDatastreams::getThingId,
                (AbstractTableDatastreams<J> table, Record tuple, Datastream entity, DataSize dataSize) -> entity.setThing(entityFactories.thingFromId(tuple, table.getThingId())));
        pfReg.addEntry(NavigationPropertyMain.OBSERVATIONS, AbstractTableDatastreams::getId, setterId);
    }

    @Override
    public Datastream newEntity() {
        return new Datastream();
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getSensorId();

    public abstract TableField<Record, J> getObsPropertyId();

    public abstract TableField<Record, J> getThingId();

    @Override
    public abstract AbstractTableDatastreams<J> as(String alias);

    @Override
    public abstract AbstractTableDatastreams<J> as(Name as);

    @Override
    public PropertyFields<AbstractTableDatastreams<J>, Datastream> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == EntityPropertyMain.UNITOFMEASUREMENT) {
            PropertyFields<AbstractTableDatastreams<J>, Datastream> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);
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
    public AbstractTableDatastreams<J> getThis() {
        return this;
    }

    protected PropertyFields<AbstractTableDatastreams<J>, Datastream> propertyFieldForUoM(final Field field, final EntityPropertyCustomSelect epCustomSelect) {
        PropertyFields<AbstractTableDatastreams<J>, Datastream> pfs = new PropertyFields<>(
                epCustomSelect,
                (tbl, tuple, entity, dataSize) -> {
                    final String value = String.valueOf(tuple.get(field));
                    UnitOfMeasurement uom = entity.getUnitOfMeasurement();
                    if (uom == null) {
                        uom = new UnitOfMeasurementPartial();
                        entity.setUnitOfMeasurement(uom);
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
                        // Do nothing
                    }
                });
        pfs.addField("1", t -> field);
        return pfs;
    }
}
