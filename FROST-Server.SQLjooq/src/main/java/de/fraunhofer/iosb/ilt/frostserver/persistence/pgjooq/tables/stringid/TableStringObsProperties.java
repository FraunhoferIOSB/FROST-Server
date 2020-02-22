package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObsProperties;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringObsProperties extends AbstractTableObsProperties<String> {

    private static final long serialVersionUID = -1873692390;

    /**
     * The reference instance of <code>public.OBS_PROPERTIES</code>
     */
    public static final TableStringObsProperties OBS_PROPERTIES = new TableStringObsProperties();

    /**
     * The column <code>public.OBS_PROPERTIES.ID</code>.
     */
    public final TableField<Record, String> id = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableStringObsProperties() {
        super();
    }

    /**
     * Create an aliased <code>public.OBS_PROPERTIES</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringObsProperties(Name alias) {
        this(alias, OBS_PROPERTIES);
    }

    private TableStringObsProperties(Name alias, TableStringObsProperties aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringObsProperties as(String alias) {
        return new TableStringObsProperties(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringObsProperties as(Name alias) {
        return new TableStringObsProperties(alias, this);
    }

}
