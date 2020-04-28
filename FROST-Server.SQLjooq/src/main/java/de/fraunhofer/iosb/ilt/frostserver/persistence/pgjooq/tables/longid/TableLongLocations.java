package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongLocations extends AbstractTableLocations<Long> {

    private static final long serialVersionUID = -806078255;

    /**
     * The reference instance of <code>public.LOCATIONS</code>
     */
    public static final TableLongLocations LOCATIONS = new TableLongLocations();

    /**
     * The column <code>public.LOCATIONS.ID</code>.
     */
    public final TableField<Record, Long> colId = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.field("nextval('\"LOCATIONS_ID_seq\"'::regclass)", SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final TableField<Record, Long> colGenFoiId = createField(DSL.name("GEN_FOI_ID"), SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>public.LOCATIONS</code> table reference
     */
    public TableLongLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.LOCATIONS</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableLongLocations(Name alias) {
        this(alias, LOCATIONS);
    }

    private TableLongLocations(Name alias, TableLongLocations aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return colId;
    }

    @Override
    public TableField<Record, Long> getGenFoiId() {
        return colGenFoiId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongLocations as(String alias) {
        return new TableLongLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongLocations as(Name alias) {
        return new TableLongLocations(alias, this);
    }

}
