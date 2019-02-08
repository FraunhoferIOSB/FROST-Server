package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableObsProperties;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringObsProperties extends AbstractTableObsProperties<String> {

    private static final long serialVersionUID = -1873692390;

    /**
     * The reference instance of <code>public.OBS_PROPERTIES</code>
     */
    public static final TableStringObsProperties OBS_PROPERTIES = new TableStringObsProperties();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
    }

    /**
     * The column <code>public.OBS_PROPERTIES.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableStringObsProperties() {
        super();
    }

    /**
     * Create an aliased <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableStringObsProperties(String alias) {
        this(DSL.name(alias), OBS_PROPERTIES);
    }

    /**
     * Create an aliased <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableStringObsProperties(Name alias) {
        this(alias, OBS_PROPERTIES);
    }

    private TableStringObsProperties(Name alias, TableStringObsProperties aliased) {
        super(alias, aliased);
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
