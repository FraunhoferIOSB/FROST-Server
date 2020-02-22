package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocations;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableUuidLocations extends AbstractTableLocations<UUID> {

    private static final long serialVersionUID = -806078255;

    /**
     * The reference instance of <code>public.LOCATIONS</code>
     */
    public static final TableUuidLocations LOCATIONS = new TableUuidLocations();

    /**
     * The column <code>public.LOCATIONS.ID</code>.
     */
    public final TableField<Record, UUID> id = createField(DSL.name("ID"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final TableField<Record, UUID> genFoiId = createField(DSL.name("GEN_FOI_ID"), SQLDataType.UUID, this, "");

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
