package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableMultiDatastreamsObsProperties<J extends Comparable> extends TableImpl<Record> implements StaTable<J> {

    private static final long serialVersionUID = 344714892;

    /**
     * The column <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.RANK</code>.
     */
    public final TableField<Record, Integer> rank = createField(DSL.name("RANK"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code> table
     * reference
     */
    protected AbstractTableMultiDatastreamsObsProperties() {
        this(DSL.name("MULTI_DATASTREAMS_OBS_PROPERTIES"), null);
    }

    protected AbstractTableMultiDatastreamsObsProperties(Name alias, AbstractTableMultiDatastreamsObsProperties<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableMultiDatastreamsObsProperties(Name alias, AbstractTableMultiDatastreamsObsProperties<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public abstract TableField<Record, J> getMultiDatastreamId();

    public abstract TableField<Record, J> getObsPropertyId();

    @Override
    public abstract AbstractTableMultiDatastreamsObsProperties<J> as(Name as);

    @Override
    public abstract AbstractTableMultiDatastreamsObsProperties<J> as(String alias);

}
