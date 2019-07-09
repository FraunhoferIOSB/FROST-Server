package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocations;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableUuidLocations extends AbstractTableLocations<UUID> {

    private static final long serialVersionUID = -806078255;

    /**
     * The reference instance of <code>public.LOCATIONS</code>
     */
    public static final TableUuidLocations LOCATIONS = new TableUuidLocations();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return id;
    }

    @Override
    public TableField<Record, UUID> getGenFoiId() {
        return genFoiId;
    }

    /**
     * The column <code>public.LOCATIONS.ID</code>.
     */
    public final TableField<Record, UUID> id = createField("ID", org.jooq.impl.SQLDataType.UUID.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final TableField<Record, UUID> genFoiId = createField("GEN_FOI_ID", org.jooq.impl.SQLDataType.UUID, this, "");

    /**
     * Create a <code>public.LOCATIONS</code> table reference
     */
    public TableUuidLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.LOCATIONS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableUuidLocations(Name alias) {
        this(alias, LOCATIONS);
    }

    private TableUuidLocations(Name alias, TableUuidLocations aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidLocations as(String alias) {
        return new TableUuidLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidLocations as(Name alias) {
        return new TableUuidLocations(alias, this);
    }

}
