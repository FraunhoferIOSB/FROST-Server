package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreamsObsProperties;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongMultiDatastreamsObsProperties extends AbstractTableMultiDatastreamsObsProperties<Long> {

    private static final long serialVersionUID = 344714892;

    /**
     * The reference instance of
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     */
    public static final TableLongMultiDatastreamsObsProperties MULTI_DATASTREAMS_OBS_PROPERTIES = new TableLongMultiDatastreamsObsProperties();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getMultiDatastreamId() {
        return multiDatastreamId;
    }

    @Override
    public TableField<Record, Long> getObsPropertyId() {
        return obsPropertyId;
    }

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, Long> multiDatastreamId = createField("MULTI_DATASTREAM_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, Long> obsPropertyId = createField("OBS_PROPERTY_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code> table
     * reference
     */
    public TableLongMultiDatastreamsObsProperties() {
        super();
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     * table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableLongMultiDatastreamsObsProperties(Name alias) {
        this(alias, MULTI_DATASTREAMS_OBS_PROPERTIES);
    }

    private TableLongMultiDatastreamsObsProperties(Name alias, TableLongMultiDatastreamsObsProperties aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongMultiDatastreamsObsProperties as(String alias) {
        return new TableLongMultiDatastreamsObsProperties(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongMultiDatastreamsObsProperties as(Name alias) {
        return new TableLongMultiDatastreamsObsProperties(alias, this);
    }

}
