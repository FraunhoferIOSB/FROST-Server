package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.codegen.maven.example.Public;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableMultiDatastreamsObsProperties<J> extends TableImpl<AbstractRecordMultiDatastreamsObsProperties<J>> {

    private static final long serialVersionUID = 344714892;

    public abstract TableField<AbstractRecordMultiDatastreamsObsProperties<J>, J> getMultiDatastreamId();

    public abstract TableField<AbstractRecordMultiDatastreamsObsProperties<J>, J> getObsPropertyId();

    /**
     * The column <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.RANK</code>.
     */
    public final TableField<AbstractRecordMultiDatastreamsObsProperties<J>, Integer> rank = createField("RANK", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public abstract AbstractTableMultiDatastreamsObsProperties<J> as(Name as);

    @Override
    public abstract AbstractTableMultiDatastreamsObsProperties<J> as(String alias);

}
