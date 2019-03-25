package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableHistLocations<J> extends TableImpl<Record> implements StaTable<J> {

    private static final long serialVersionUID = -1457801967;

    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getThingId();

    /**
     * The column <code>public.HIST_LOCATIONS.TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> time = createField("TIME", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * Create a <code>public.HIST_LOCATIONS</code> table reference
     */
    protected AbstractTableHistLocations() {
        this(DSL.name("HIST_LOCATIONS"), null);
    }

    protected AbstractTableHistLocations(Name alias, AbstractTableHistLocations<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableHistLocations(Name alias, AbstractTableHistLocations<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public abstract AbstractTableHistLocations<J> as(Name as);

    @Override
    public abstract AbstractTableHistLocations<J> as(String alias);

}
