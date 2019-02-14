package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableMultiDatastreamsObsProperties;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringMultiDatastreamsObsProperties extends AbstractTableMultiDatastreamsObsProperties<String> {

    private static final long serialVersionUID = 344714892;

    /**
     * The reference instance of
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     */
    public static final TableStringMultiDatastreamsObsProperties MULTI_DATASTREAMS_OBS_PROPERTIES = new TableStringMultiDatastreamsObsProperties();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getMultiDatastreamId() {
        return multiDatastreamId;
    }

    @Override
    public TableField<Record, String> getObsPropertyId() {
        return ObsPropertyId;
    }

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, String> multiDatastreamId = createField("MULTI_DATASTREAM_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, String> ObsPropertyId = createField("OBS_PROPERTY_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code> table
     * reference
     */
    public TableStringMultiDatastreamsObsProperties() {
        super();
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     * table reference
     */
    public TableStringMultiDatastreamsObsProperties(String alias) {
        this(DSL.name(alias), MULTI_DATASTREAMS_OBS_PROPERTIES);
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     * table reference
     */
    public TableStringMultiDatastreamsObsProperties(Name alias) {
        this(alias, MULTI_DATASTREAMS_OBS_PROPERTIES);
    }

    private TableStringMultiDatastreamsObsProperties(Name alias, TableStringMultiDatastreamsObsProperties aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringMultiDatastreamsObsProperties as(String alias) {
        return new TableStringMultiDatastreamsObsProperties(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringMultiDatastreamsObsProperties as(Name alias) {
        return new TableStringMultiDatastreamsObsProperties(alias, this);
    }

}
