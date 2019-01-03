package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableObsProperties;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.codegen.maven.example.Public;
import org.jooq.impl.DSL;

public class TableLongObsProperties extends AbstractTableObsProperties<Long> {

    private static final long serialVersionUID = -1873692390;

    /**
     * The reference instance of <code>public.OBS_PROPERTIES</code>
     */
    public static final TableLongObsProperties OBS_PROPERTIES = new TableLongObsProperties();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<RecordLongObsProperties> getRecordType() {
        return RecordLongObsProperties.class;
    }

    @Override
    public TableField<AbstractRecordObsProperties<Long>, Long> getId() {
        return ID;
    }

    /**
     * The column <code>public.OBS_PROPERTIES.ID</code>.
     */
    public final TableField<AbstractRecordObsProperties<Long>, Long> ID = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"OBS_PROPERTIES_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableLongObsProperties() {
        super();
    }

    /**
     * Create an aliased <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableLongObsProperties(String alias) {
        this(DSL.name(alias), OBS_PROPERTIES);
    }

    /**
     * Create an aliased <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableLongObsProperties(Name alias) {
        this(alias, OBS_PROPERTIES);
    }

    private TableLongObsProperties(Name alias, TableLongObsProperties aliased) {
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
    public TableLongObsProperties as(String alias) {
        return new TableLongObsProperties(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongObsProperties as(Name alias) {
        return new TableLongObsProperties(alias, this);
    }

}
