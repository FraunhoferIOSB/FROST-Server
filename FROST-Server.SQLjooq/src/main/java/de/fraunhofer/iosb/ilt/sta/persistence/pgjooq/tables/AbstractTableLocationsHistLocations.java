package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableLocationsHistLocations<J> extends TableImpl<Record> {

    private static final long serialVersionUID = -1022733888;

    public abstract TableField<Record, J> getLocationId();

    public abstract TableField<Record, J> getHistLocationId();

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

    @Override
    public abstract AbstractTableLocationsHistLocations<J> as(Name as);

    @Override
    public abstract AbstractTableLocationsHistLocations<J> as(String alias);

}
