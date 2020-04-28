package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableUuidObservations extends AbstractTableObservations<UUID> {

    private static final long serialVersionUID = -1104422281;

    /**
     * The reference instance of <code>public.OBSERVATIONS</code>
     */
    public static final TableUuidObservations OBSERVATIONS = new TableUuidObservations();

    /**
     * The column <code>public.OBSERVATIONS.ID</code>.
     */
    public final TableField<Record, UUID> colId = createField(DSL.name("ID"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.OBSERVATIONS.DATASTREAM_ID</code>.
     */
    public final TableField<Record, UUID> colDatastreamId = createField(DSL.name("DATASTREAM_ID"), SQLDataType.UUID, this, "");

    /**
     * The column <code>public.OBSERVATIONS.FEATURE_ID</code>.
     */
    public final TableField<Record, UUID> colFeatureId = createField(DSL.name("FEATURE_ID"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.OBSERVATIONS.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, UUID> colMultiDatastreamId = createField(DSL.name("MULTI_DATASTREAM_ID"), SQLDataType.UUID, this, "");

    /**
     * Create a <code>public.OBSERVATIONS</code> table reference
     */
    public TableUuidObservations() {
        super();
    }

    /**
     * Create an aliased <code>public.OBSERVATIONS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableUuidObservations(Name alias) {
        this(alias, OBSERVATIONS);
    }

    private TableUuidObservations(Name alias, TableUuidObservations aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return colId;
    }

    @Override
    public TableField<Record, UUID> getDatastreamId() {
        return colDatastreamId;
    }

    @Override
    public TableField<Record, UUID> getFeatureId() {
        return colFeatureId;
    }

    @Override
    public TableField<Record, UUID> getMultiDatastreamId() {
        return colMultiDatastreamId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidObservations as(String alias) {
        return new TableUuidObservations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidObservations as(Name alias) {
        return new TableUuidObservations(alias, this);
    }

}
