package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongObservations extends AbstractTableObservations<Long> {

    private static final long serialVersionUID = -1104422281;

    /**
     * The reference instance of <code>public.OBSERVATIONS</code>
     */
    public static final TableLongObservations OBSERVATIONS = new TableLongObservations();

    /**
     * The column <code>public.OBSERVATIONS.ID</code>.
     */
    public final TableField<Record, Long> colId = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.field("nextval('\"OBSERVATIONS_ID_seq\"'::regclass)", SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.OBSERVATIONS.DATASTREAM_ID</code>.
     */
    public final TableField<Record, Long> colDatastreamId = createField(DSL.name("DATASTREAM_ID"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.OBSERVATIONS.FEATURE_ID</code>.
     */
    public final TableField<Record, Long> colFeatureId = createField(DSL.name("FEATURE_ID"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.OBSERVATIONS.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, Long> colMultiDatastreamId = createField(DSL.name("MULTI_DATASTREAM_ID"), SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>public.OBSERVATIONS</code> table reference
     */
    public TableLongObservations() {
        super();
    }

    /**
     * Create an aliased <code>public.OBSERVATIONS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableLongObservations(Name alias) {
        this(alias, OBSERVATIONS);
    }

    private TableLongObservations(Name alias, TableLongObservations aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return colId;
    }

    @Override
    public TableField<Record, Long> getDatastreamId() {
        return colDatastreamId;
    }

    @Override
    public TableField<Record, Long> getFeatureId() {
        return colFeatureId;
    }

    @Override
    public TableField<Record, Long> getMultiDatastreamId() {
        return colMultiDatastreamId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongObservations as(String alias) {
        return new TableLongObservations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongObservations as(Name alias) {
        return new TableLongObservations(alias, this);
    }

}
