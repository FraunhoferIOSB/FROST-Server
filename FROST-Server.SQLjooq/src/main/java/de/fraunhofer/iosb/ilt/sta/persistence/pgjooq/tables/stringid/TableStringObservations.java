package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableObservations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringObservations extends AbstractTableObservations<String> {

    private static final long serialVersionUID = -1104422281;

    /**
     * The reference instance of <code>public.OBSERVATIONS</code>
     */
    public static final TableStringObservations OBSERVATIONS = new TableStringObservations();

    /**
     * @return The class holding records for this type
     */
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
     * The column <code>public.OBSERVATIONS.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.OBSERVATIONS.DATASTREAM_ID</code>.
     */
    public final TableField<Record, String> datastreamId = createField("DATASTREAM_ID", org.jooq.impl.SQLDataType.VARCHAR, this, "");

    /**
     * The column <code>public.OBSERVATIONS.FEATURE_ID</code>.
     */
    public final TableField<Record, String> featureId = createField("FEATURE_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.OBSERVATIONS.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, String> multiDatastreamId = createField("MULTI_DATASTREAM_ID", org.jooq.impl.SQLDataType.VARCHAR, this, "");

    /**
     * Create a <code>public.OBSERVATIONS</code> table reference
     */
    public TableStringObservations() {
        super();
    }

    /**
     * Create an aliased <code>public.OBSERVATIONS</code> table reference
     */
    public TableStringObservations(String alias) {
        this(DSL.name(alias), OBSERVATIONS);
    }

    /**
     * Create an aliased <code>public.OBSERVATIONS</code> table reference
     */
    public TableStringObservations(Name alias) {
        this(alias, OBSERVATIONS);
    }

    private TableStringObservations(Name alias, TableStringObservations aliased) {
        super(alias, aliased);
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
