package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class AbstractTableLocationsHistLocations<J extends Comparable> extends StaLinkTable<J, AbstractTableLocationsHistLocations<J>> {

    private static final long serialVersionUID = -1022733888;

    private static AbstractTableLocationsHistLocations INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> AbstractTableLocationsHistLocations<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new AbstractTableLocationsHistLocations(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new AbstractTableLocationsHistLocations<>(idType);
    }

    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, J> colLocationId = createField(DSL.name("LOCATION_ID"), getIdType(), this);
    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.HIST_LOCATION_ID</code>.
     */
    public final TableField<Record, J> colHistLocationId = createField(DSL.name("HIST_LOCATION_ID"), getIdType(), this);

    /**
     * Create a <code>public.LOCATIONS_HIST_LOCATIONS</code> table reference
     */
    private AbstractTableLocationsHistLocations(DataType<J> idType) {
        super(idType, DSL.name("LOCATIONS_HIST_LOCATIONS"), null);
    }

    private AbstractTableLocationsHistLocations(Name alias, AbstractTableLocationsHistLocations<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    public TableField<Record, J> getLocationId() {
        return colLocationId;
    }

    public TableField<Record, J> getHistLocationId() {
        return colHistLocationId;
    }

    @Override
    public AbstractTableLocationsHistLocations<J> as(Name alias) {
        return new AbstractTableLocationsHistLocations<>(alias, this);
    }

    @Override
    public AbstractTableLocationsHistLocations<J> as(String alias) {
        return new AbstractTableLocationsHistLocations<>(DSL.name(alias), this);
    }

    @Override
    public AbstractTableLocationsHistLocations<J> getThis() {
        return this;
    }

}
