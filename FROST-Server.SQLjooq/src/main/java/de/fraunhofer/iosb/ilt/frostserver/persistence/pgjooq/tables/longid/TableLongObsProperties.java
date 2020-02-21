package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObsProperties;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongObsProperties extends AbstractTableObsProperties<Long> {

    private static final long serialVersionUID = -1873692390;

    /**
     * The reference instance of <code>public.OBS_PROPERTIES</code>
     */
    public static final TableLongObsProperties OBS_PROPERTIES = new TableLongObsProperties();

    /**
     * The column <code>public.OBS_PROPERTIES.ID</code>.
     */
    public final TableField<Record, Long> id = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.field("nextval('\"OBS_PROPERTIES_ID_seq\"'::regclass)", SQLDataType.BIGINT)), this, "");

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableLongObsProperties() {
        super();
    }

    /**
     * Create an aliased <code>public.OBS_PROPERTIES</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableLongObsProperties(Name alias) {
        this(alias, OBS_PROPERTIES);
    }

    private TableLongObsProperties(Name alias, TableLongObsProperties aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return id;
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
