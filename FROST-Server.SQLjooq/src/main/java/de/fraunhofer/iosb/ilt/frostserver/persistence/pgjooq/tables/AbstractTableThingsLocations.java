package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableThingsLocations<J extends Comparable> extends TableImpl<Record> implements StaTable<J,AbstractTableThingsLocations<J>> {

    private static final long serialVersionUID = -1443552218;

    /**
     * Create a <code>public.THINGS_LOCATIONS</code> table reference
     */
    protected AbstractTableThingsLocations() {
        this(DSL.name("THINGS_LOCATIONS"), null);
    }

    protected AbstractTableThingsLocations(Name alias, AbstractTableThingsLocations<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableThingsLocations(Name alias, AbstractTableThingsLocations<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public abstract TableField<Record, J> getLocationId();

    public abstract TableField<Record, J> getThingId();

    @Override
    public abstract AbstractTableThingsLocations<J> as(Name as);

    @Override
    public abstract AbstractTableThingsLocations<J> as(String alias);

    @Override
    public AbstractTableThingsLocations<J> getThis() {
        return this;
    }

}
