package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableHistLocations;
import java.time.OffsetDateTime;
import org.jooq.Name;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongHistLocations extends AbstractTableHistLocations<Long> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.HIST_LOCATIONS</code>
     */
    public static final TableLongHistLocations HIST_LOCATIONS = new TableLongHistLocations();

    @Override
    public Class<RecordLongHistLocations> getRecordType() {
        return RecordLongHistLocations.class;
    }

    @Override
    public TableField<AbstractRecordHistLocations<Long>, Long> getId() {
        return ID;
    }

    @Override
    public TableField<AbstractRecordHistLocations<Long>, Long> getThingId() {
        return THING_ID;
    }

    /**
     * The column <code>public.HIST_LOCATIONS.ID</code>.
     */
    public final TableField<AbstractRecordHistLocations<Long>, Long> ID = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"HIST_LOCATIONS_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.HIST_LOCATIONS.TIME</code>.
     */
    public final TableField<AbstractRecordHistLocations<Long>, OffsetDateTime> time = createField("TIME", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.HIST_LOCATIONS.THING_ID</code>.
     */
    public final TableField<AbstractRecordHistLocations<Long>, Long> THING_ID = createField("THING_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.HIST_LOCATIONS</code> table reference
     */
    public TableLongHistLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.HIST_LOCATIONS</code> table reference
     */
    public TableLongHistLocations(String alias) {
        this(DSL.name(alias), HIST_LOCATIONS);
    }

    /**
     * Create an aliased <code>public.HIST_LOCATIONS</code> table reference
     */
    public TableLongHistLocations(Name alias) {
        this(alias, HIST_LOCATIONS);
    }

    private TableLongHistLocations(Name alias, TableLongHistLocations aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongHistLocations as(String alias) {
        return new TableLongHistLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongHistLocations as(Name alias) {
        return new TableLongHistLocations(alias, this);
    }

}
