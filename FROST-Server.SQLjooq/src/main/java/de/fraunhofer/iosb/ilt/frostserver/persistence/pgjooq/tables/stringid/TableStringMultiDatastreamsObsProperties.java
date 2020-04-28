package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreamsObsProperties;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringMultiDatastreamsObsProperties extends AbstractTableMultiDatastreamsObsProperties<String> {

    private static final long serialVersionUID = 344714892;

    /**
     * The reference instance of
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     */
    public static final TableStringMultiDatastreamsObsProperties MULTI_DATASTREAMS_OBS_PROPERTIES = new TableStringMultiDatastreamsObsProperties();

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, String> colMultiDatastreamId = createField(DSL.name("MULTI_DATASTREAM_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, String> colObsPropertyId = createField(DSL.name("OBS_PROPERTY_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

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
     *
     * @param alias The alias to use in queries.
     */
    public TableStringMultiDatastreamsObsProperties(Name alias) {
        this(alias, MULTI_DATASTREAMS_OBS_PROPERTIES);
    }

    private TableStringMultiDatastreamsObsProperties(Name alias, TableStringMultiDatastreamsObsProperties aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getMultiDatastreamId() {
        return colMultiDatastreamId;
    }

    @Override
    public TableField<Record, String> getObsPropertyId() {
        return colObsPropertyId;
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
