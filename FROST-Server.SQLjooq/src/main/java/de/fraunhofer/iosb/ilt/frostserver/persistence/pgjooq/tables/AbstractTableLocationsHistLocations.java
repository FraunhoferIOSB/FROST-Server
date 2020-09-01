package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableLocationsHistLocations<J extends Comparable> extends TableImpl<Record> implements StaTable<J, AbstractTableLocationsHistLocations<J>> {

    private static final long serialVersionUID = -1022733888;

    /**
     * Create a <code>public.LOCATIONS_HIST_LOCATIONS</code> table reference
     */
    protected AbstractTableLocationsHistLocations() {
        this(DSL.name("LOCATIONS_HIST_LOCATIONS"), null);
    }

    protected AbstractTableLocationsHistLocations(Name alias, AbstractTableLocationsHistLocations<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableLocationsHistLocations(Name alias, AbstractTableLocationsHistLocations<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public abstract TableField<Record, J> getLocationId();

    public abstract TableField<Record, J> getHistLocationId();

    @Override
    public abstract AbstractTableLocationsHistLocations<J> as(Name as);

    @Override
    public abstract AbstractTableLocationsHistLocations<J> as(String alias);

    @Override
    public AbstractTableLocationsHistLocations<J> getThis() {
        return this;
    }

}
