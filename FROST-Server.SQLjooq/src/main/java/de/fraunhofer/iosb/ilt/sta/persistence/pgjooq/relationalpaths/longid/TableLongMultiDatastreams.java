package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreams;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.codegen.maven.example.Public;
import org.jooq.impl.DSL;

public class TableLongMultiDatastreams extends AbstractTableMultiDatastreams<Long> {

    private static final long serialVersionUID = 560943996;

    /**
     * The reference instance of <code>public.MULTI_DATASTREAMS</code>
     */
    public static final TableLongMultiDatastreams MULTI_DATASTREAMS = new TableLongMultiDatastreams();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<RecordLongMultiDatastreams> getRecordType() {
        return RecordLongMultiDatastreams.class;
    }

    @Override
    public TableField<AbstractRecordMultiDatastreams<Long>, Long> getId() {
        return ID;
    }

    @Override
    public TableField<AbstractRecordMultiDatastreams<Long>, Long> getSensorId() {
        return SENSOR_ID;
    }

    @Override
    public TableField<AbstractRecordMultiDatastreams<Long>, Long> getThingId() {
        return THING_ID;
    }

    /**
     * The column <code>public.MULTI_DATASTREAMS.ID</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<Long>, Long> ID = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"MULTI_DATASTREAMS_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<Long>, Long> SENSOR_ID = createField("SENSOR_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.THING_ID</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<Long>, Long> THING_ID = createField("THING_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.MULTI_DATASTREAMS</code> table reference
     */
    public TableLongMultiDatastreams() {
        super();
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS</code> table reference
     */
    public TableLongMultiDatastreams(String alias) {
        this(DSL.name(alias), MULTI_DATASTREAMS);
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS</code> table reference
     */
    public TableLongMultiDatastreams(Name alias) {
        this(alias, MULTI_DATASTREAMS);
    }

    private TableLongMultiDatastreams(Name alias, TableLongMultiDatastreams aliased) {
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
    public TableLongMultiDatastreams as(String alias) {
        return new TableLongMultiDatastreams(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongMultiDatastreams as(Name alias) {
        return new TableLongMultiDatastreams(alias, this);
    }

}
