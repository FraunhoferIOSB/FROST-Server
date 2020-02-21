package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringLocations extends AbstractTableLocations<String> {

    private static final long serialVersionUID = -806078255;

    /**
     * The reference instance of <code>public.LOCATIONS</code>
     */
    public static final TableStringLocations LOCATIONS = new TableStringLocations();

    /**
     * The column <code>public.LOCATIONS.ID</code>.
     */
    public final TableField<Record, String> id = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final TableField<Record, String> genFoiId = createField(DSL.name("GEN_FOI_ID"), SQLDataType.VARCHAR, this, "");

    /**
     * Create a <code>public.LOCATIONS</code> table reference
     */
    public TableStringLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.LOCATIONS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringLocations(Name alias) {
        this(alias, LOCATIONS);
    }

    private TableStringLocations(Name alias, TableStringLocations aliased) {
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

    @Override
    public TableField<Record, String> getGenFoiId() {
        return genFoiId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringLocations as(String alias) {
        return new TableStringLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringLocations as(Name alias) {
        return new TableStringLocations(alias, this);
    }

}
