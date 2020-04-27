package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import java.time.OffsetDateTime;
import org.geolatte.geom.Geometry;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableMultiDatastreams<J extends Comparable> extends StaTableAbstract<J> {

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
    public final TableField<Record, String> colObservationTypes = createField(DSL.name("OBSERVATION_TYPES"), SQLDataType.CLOB, this, "");

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
    public final TableField<Record, String> colUnitOfMeasurements = createField(DSL.name("UNIT_OF_MEASUREMENTS"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVED_AREA</code>.
     */
    public final TableField<Record, Geometry> colObservedArea = createField(DSL.name("OBSERVED_AREA"), DefaultDataType.getDefaultDataType("\"public\".\"geometry\""), this, "", new PostGisGeometryBinding());

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> colObservedAreaText = DSL.field("ST_AsGeoJSON(?)", String.class, colObservedArea);

    /**
     * The column <code>public.MULTI_DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<Record, String> colProperties = createField(DSL.name("PROPERTIES"), SQLDataType.CLOB, this, "");

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
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getSensorId();

    public abstract TableField<Record, J> getThingId();

    @Override
    public abstract AbstractTableMultiDatastreams<J> as(Name as);

    @Override
    public abstract AbstractTableMultiDatastreams<J> as(String alias);

}
