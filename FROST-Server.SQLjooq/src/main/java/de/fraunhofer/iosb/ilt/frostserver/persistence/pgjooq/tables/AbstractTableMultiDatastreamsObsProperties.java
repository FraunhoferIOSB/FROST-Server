package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class AbstractTableMultiDatastreamsObsProperties<J extends Comparable> extends StaLinkTable<J, AbstractTableMultiDatastreamsObsProperties<J>> {

    private static final long serialVersionUID = 344714892;

    private static AbstractTableMultiDatastreamsObsProperties INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> AbstractTableMultiDatastreamsObsProperties<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new AbstractTableMultiDatastreamsObsProperties(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new AbstractTableMultiDatastreamsObsProperties<>(idType);
    }

    /**
     * The column <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.RANK</code>.
     */
    public final TableField<Record, Integer> colRank = createField(DSL.name("RANK"), SQLDataType.INTEGER.nullable(false), this);

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, J> colMultiDatastreamId = createField(DSL.name("MULTI_DATASTREAM_ID"), getIdType(), this);

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, J> colObsPropertyId = createField(DSL.name("OBS_PROPERTY_ID"), getIdType(), this);

    /**
     * Create a <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code> table
     * reference
     */
    protected AbstractTableMultiDatastreamsObsProperties(DataType<J> idType) {
        super(idType, DSL.name("MULTI_DATASTREAMS_OBS_PROPERTIES"), null);
    }

    protected AbstractTableMultiDatastreamsObsProperties(Name alias, AbstractTableMultiDatastreamsObsProperties<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    public TableField<Record, J> getMultiDatastreamId() {
        return colMultiDatastreamId;
    }

    public TableField<Record, J> getObsPropertyId() {
        return colObsPropertyId;
    }

    @Override
    public AbstractTableMultiDatastreamsObsProperties<J> as(Name alias) {
        return new AbstractTableMultiDatastreamsObsProperties<>(alias, this);
    }

    @Override
    public AbstractTableMultiDatastreamsObsProperties<J> as(String alias) {
        return new AbstractTableMultiDatastreamsObsProperties<>(DSL.name(alias), this);
    }

    @Override
    public AbstractTableMultiDatastreamsObsProperties<J> getThis() {
        return this;
    }

}
