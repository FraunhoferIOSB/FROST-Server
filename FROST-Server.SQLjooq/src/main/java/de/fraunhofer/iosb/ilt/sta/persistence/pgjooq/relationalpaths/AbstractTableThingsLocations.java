package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.TableField;

import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableThingsLocations<J> extends TableImpl<AbstractRecordThingsLocations<J>> {

    private static final long serialVersionUID = -1443552218;

    public abstract TableField<AbstractRecordThingsLocations<J>, J> getLocationId();

    public abstract TableField<AbstractRecordThingsLocations<J>, J> getThingId();

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

    @Override
    public abstract AbstractTableThingsLocations<J> as(Name as);

    @Override
    public abstract AbstractTableThingsLocations<J> as(String alias);

}
