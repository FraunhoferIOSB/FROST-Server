package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObsProperties;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableUuidObsProperties extends AbstractTableObsProperties<UUID> {

    private static final long serialVersionUID = -1873692390;

    /**
     * The reference instance of <code>public.OBS_PROPERTIES</code>
     */
    public static final TableUuidObsProperties OBS_PROPERTIES = new TableUuidObsProperties();

    /**
     * The column <code>public.OBS_PROPERTIES.ID</code>.
     */
    public final TableField<Record, UUID> id = createField(DSL.name("ID"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.UUID)), this, "");

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableUuidObsProperties() {
        super();
    }

    /**
     * Create an aliased <code>public.OBS_PROPERTIES</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableUuidObsProperties(Name alias) {
        this(alias, OBS_PROPERTIES);
    }

    private TableUuidObsProperties(Name alias, TableUuidObsProperties aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidObsProperties as(String alias) {
        return new TableUuidObsProperties(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidObsProperties as(Name alias) {
        return new TableUuidObsProperties(alias, this);
    }

}
