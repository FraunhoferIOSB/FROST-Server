package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringLocations extends AbstractTableLocations<String> {

    private static final long serialVersionUID = -806078255;

    /**
     * The reference instance of <code>public.LOCATIONS</code>
     */
    public static final TableStringLocations LOCATIONS = new TableStringLocations();

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

    @Override
    public TableField<Record, String> getGenFoiId() {
        return genFoiId;
    }

    /**
     * The column <code>public.LOCATIONS.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final TableField<Record, String> genFoiId = createField("GEN_FOI_ID", org.jooq.impl.SQLDataType.VARCHAR, this, "");

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
