package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreamsObsProperties;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.codegen.maven.example.Public;
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
    public Class<RecordLongMultiDatastreamsObsProperties> getRecordType() {
        return RecordLongMultiDatastreamsObsProperties.class;
    }

    @Override
    public TableField<AbstractRecordMultiDatastreamsObsProperties<Long>, Long> getMultiDatastreamId() {
        return MULTI_DATASTREAM_ID;
    }

    @Override
    public TableField<AbstractRecordMultiDatastreamsObsProperties<Long>, Long> getObsPropertyId() {
        return OBS_PROPERTY_ID;
    }

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<AbstractRecordMultiDatastreamsObsProperties<Long>, Long> MULTI_DATASTREAM_ID = createField("MULTI_DATASTREAM_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID</code>.
     */
    public final TableField<AbstractRecordMultiDatastreamsObsProperties<Long>, Long> OBS_PROPERTY_ID = createField("OBS_PROPERTY_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

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
     */
    public TableLongMultiDatastreamsObsProperties(String alias) {
        this(DSL.name(alias), MULTI_DATASTREAMS_OBS_PROPERTIES);
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code>
     * table reference
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
    public Schema getSchema() {
        return Public.PUBLIC;
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
