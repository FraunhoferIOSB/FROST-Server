package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableUuidObservations extends AbstractTableObservations<UUID> {

    private static final long serialVersionUID = -1104422281;

    /**
     * The reference instance of <code>public.OBSERVATIONS</code>
     */
    public static final TableUuidObservations OBSERVATIONS = new TableUuidObservations();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return id;
    }

    @Override
    public TableField<Record, UUID> getDatastreamId() {
        return datastreamId;
    }

    @Override
    public TableField<Record, UUID> getFeatureId() {
        return featureId;
    }

    @Override
    public TableField<Record, UUID> getMultiDatastreamId() {
        return multiDatastreamId;
    }

    /**
     * The column <code>public.OBSERVATIONS.ID</code>.
     */
    public final TableField<Record, UUID> id = createField("ID", org.jooq.impl.SQLDataType.UUID.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.OBSERVATIONS.DATASTREAM_ID</code>.
     */
    public final TableField<Record, UUID> datastreamId = createField("DATASTREAM_ID", org.jooq.impl.SQLDataType.UUID, this, "");

    /**
     * The column <code>public.OBSERVATIONS.FEATURE_ID</code>.
     */
    public final TableField<Record, UUID> featureId = createField("FEATURE_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.OBSERVATIONS.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, UUID> multiDatastreamId = createField("MULTI_DATASTREAM_ID", org.jooq.impl.SQLDataType.UUID, this, "");

    /**
     * Create a <code>public.OBSERVATIONS</code> table reference
     */
    public TableUuidObservations() {
        super();
    }

    /**
     * Create an aliased <code>public.OBSERVATIONS</code> table reference
     */
    public TableUuidObservations(String alias) {
        this(DSL.name(alias), OBSERVATIONS);
    }

    /**
     * Create an aliased <code>public.OBSERVATIONS</code> table reference
     */
    public TableUuidObservations(Name alias) {
        this(alias, OBSERVATIONS);
    }

    private TableUuidObservations(Name alias, TableUuidObservations aliased) {
        super(alias, aliased);
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
