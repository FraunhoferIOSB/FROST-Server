package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableImpMultiDatastreamsObsProperties<J extends Comparable> extends StaLinkTable<J, TableImpMultiDatastreamsObsProperties<J>> {

    private static final long serialVersionUID = 344714892;

    private static TableImpMultiDatastreamsObsProperties INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> TableImpMultiDatastreamsObsProperties<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new TableImpMultiDatastreamsObsProperties(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new TableImpMultiDatastreamsObsProperties<>(idType);
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
    protected TableImpMultiDatastreamsObsProperties(DataType<J> idType) {
        super(idType, DSL.name("MULTI_DATASTREAMS_OBS_PROPERTIES"), null);
    }

    protected TableImpMultiDatastreamsObsProperties(Name alias, TableImpMultiDatastreamsObsProperties<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    public TableField<Record, J> getMultiDatastreamId() {
        return colMultiDatastreamId;
    }

    public TableField<Record, J> getObsPropertyId() {
        return colObsPropertyId;
    }

    @Override
    public TableImpMultiDatastreamsObsProperties<J> as(Name alias) {
        return new TableImpMultiDatastreamsObsProperties<>(alias, this);
    }

    @Override
    public TableImpMultiDatastreamsObsProperties<J> as(String alias) {
        return new TableImpMultiDatastreamsObsProperties<>(DSL.name(alias), this);
    }

    @Override
    public TableImpMultiDatastreamsObsProperties<J> getThis() {
        return this;
    }

}
