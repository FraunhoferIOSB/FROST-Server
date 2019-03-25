package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreamsObsProperties;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableUuidMultiDatastreamsObsProperties extends AbstractTableMultiDatastreamsObsProperties<UUID> {

    private static final long serialVersionUID = 344714892;

    /**
     * The reference instance of
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     */
    public static final TableUuidMultiDatastreamsObsProperties MULTI_DATASTREAMS_OBS_PROPERTIES = new TableUuidMultiDatastreamsObsProperties();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getMultiDatastreamId() {
        return multiDatastreamId;
    }

    @Override
    public TableField<Record, UUID> getObsPropertyId() {
        return ObsPropertyId;
    }

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, UUID> multiDatastreamId = createField("MULTI_DATASTREAM_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, UUID> ObsPropertyId = createField("OBS_PROPERTY_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * Create a <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code> table
     * reference
     */
    public TableUuidMultiDatastreamsObsProperties() {
        super();
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     * table reference
     */
    public TableUuidMultiDatastreamsObsProperties(String alias) {
        this(DSL.name(alias), MULTI_DATASTREAMS_OBS_PROPERTIES);
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     * table reference
     */
    public TableUuidMultiDatastreamsObsProperties(Name alias) {
        this(alias, MULTI_DATASTREAMS_OBS_PROPERTIES);
    }

    private TableUuidMultiDatastreamsObsProperties(Name alias, TableUuidMultiDatastreamsObsProperties aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidMultiDatastreamsObsProperties as(String alias) {
        return new TableUuidMultiDatastreamsObsProperties(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidMultiDatastreamsObsProperties as(Name alias) {
        return new TableUuidMultiDatastreamsObsProperties(alias, this);
    }

}
