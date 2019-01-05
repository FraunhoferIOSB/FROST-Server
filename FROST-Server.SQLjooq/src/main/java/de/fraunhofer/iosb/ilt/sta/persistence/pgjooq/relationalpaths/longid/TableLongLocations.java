package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableLocations;
import org.jooq.Name;
import org.jooq.TableField;

import org.jooq.impl.DSL;

public class TableLongLocations extends AbstractTableLocations<Long> {

    private static final long serialVersionUID = -806078255;

    /**
     * The reference instance of <code>public.LOCATIONS</code>
     */
    public static final TableLongLocations LOCATIONS = new TableLongLocations();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<RecordLongLocations> getRecordType() {
        return RecordLongLocations.class;
    }

    @Override
    public TableField<AbstractRecordLocations<Long>, Long> getId() {
        return ID;
    }

    @Override
    public TableField<AbstractRecordLocations<Long>, Long> getGenFoiId() {
        return genFoiId;
    }

    /**
     * The column <code>public.LOCATIONS.ID</code>.
     */
    public final TableField<AbstractRecordLocations<Long>, Long> ID = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"LOCATIONS_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final TableField<AbstractRecordLocations<Long>, Long> genFoiId = createField("GEN_FOI_ID", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>public.LOCATIONS</code> table reference
     */
    public TableLongLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.LOCATIONS</code> table reference
     */
    public TableLongLocations(String alias) {
        this(DSL.name(alias), LOCATIONS);
    }

    /**
     * Create an aliased <code>public.LOCATIONS</code> table reference
     */
    public TableLongLocations(Name alias) {
        this(alias, LOCATIONS);
    }

    private TableLongLocations(Name alias, TableLongLocations aliased) {
        super(alias, aliased);
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
