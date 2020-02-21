package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringObservations extends AbstractTableObservations<String> {

    private static final long serialVersionUID = -1104422281;

    /**
     * The reference instance of <code>public.OBSERVATIONS</code>
     */
    public static final TableStringObservations OBSERVATIONS = new TableStringObservations();

    /**
     * The column <code>public.OBSERVATIONS.ID</code>.
     */
    public final TableField<Record, String> id = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.OBSERVATIONS.DATASTREAM_ID</code>.
     */
    public final TableField<Record, String> datastreamId = createField(DSL.name("DATASTREAM_ID"), SQLDataType.VARCHAR, this, "");

    /**
     * The column <code>public.OBSERVATIONS.FEATURE_ID</code>.
     */
    public final TableField<Record, String> featureId = createField(DSL.name("FEATURE_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.OBSERVATIONS.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, String> multiDatastreamId = createField(DSL.name("MULTI_DATASTREAM_ID"), SQLDataType.VARCHAR, this, "");

    /**
     * Create a <code>public.OBSERVATIONS</code> table reference
     */
    public TableStringObservations() {
        super();
    }

    /**
     * Create an aliased <code>public.OBSERVATIONS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringObservations(Name alias) {
        this(alias, OBSERVATIONS);
    }

    private TableStringObservations(Name alias, TableStringObservations aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
    }

    @Override
    public TableField<Record, String> getDatastreamId() {
        return datastreamId;
    }

    @Override
    public TableField<Record, String> getFeatureId() {
        return featureId;
    }

    @Override
    public TableField<Record, String> getMultiDatastreamId() {
        return multiDatastreamId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringObservations as(String alias) {
        return new TableStringObservations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringObservations as(Name alias) {
        return new TableStringObservations(alias, this);
    }

}
