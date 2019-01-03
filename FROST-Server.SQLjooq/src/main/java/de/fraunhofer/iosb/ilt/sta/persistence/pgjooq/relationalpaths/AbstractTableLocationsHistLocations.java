package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.codegen.maven.example.Public;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableLocationsHistLocations<J> extends TableImpl<AbstractRecordLocationsHistLocations<J>> {

    private static final long serialVersionUID = -1022733888;

    public abstract TableField<AbstractRecordLocationsHistLocations<J>, J> getLocationId();

    public abstract TableField<AbstractRecordLocationsHistLocations<J>, J> getHistLocationId();

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public abstract AbstractTableLocationsHistLocations<J> as(Name as);

    @Override
    public abstract AbstractTableLocationsHistLocations<J> as(String alias);

}
