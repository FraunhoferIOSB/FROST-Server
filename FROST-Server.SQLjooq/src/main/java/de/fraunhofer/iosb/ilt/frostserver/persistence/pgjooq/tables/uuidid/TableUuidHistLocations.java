package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableHistLocations;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableUuidHistLocations extends AbstractTableHistLocations<UUID> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.HIST_LOCATIONS</code>
     */
    public static final TableUuidHistLocations HIST_LOCATIONS = new TableUuidHistLocations();

    /**
     * The column <code>public.HIST_LOCATIONS.ID</code>.
     */
    public final TableField<Record, UUID> id = createField(DSL.name("ID"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.HIST_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, UUID> thingId = createField(DSL.name("THING_ID"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * Create a <code>public.HIST_LOCATIONS</code> table reference
     */
    public TableUuidHistLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.HIST_LOCATIONS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableUuidHistLocations(Name alias) {
        this(alias, HIST_LOCATIONS);
    }

    private TableUuidHistLocations(Name alias, TableUuidHistLocations aliased) {
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
    public TableField<Record, UUID> getThingId() {
        return thingId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidHistLocations as(String alias) {
        return new TableUuidHistLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidHistLocations as(Name alias) {
        return new TableUuidHistLocations(alias, this);
    }

}
