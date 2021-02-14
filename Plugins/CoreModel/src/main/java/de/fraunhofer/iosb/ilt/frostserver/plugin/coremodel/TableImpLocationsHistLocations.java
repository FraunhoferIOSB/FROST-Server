package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaLinkTable;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableImpLocationsHistLocations<J extends Comparable> extends StaLinkTable<J, TableImpLocationsHistLocations<J>> {

    private static final long serialVersionUID = -1022733888;

    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, J> colLocationId = createField(DSL.name("LOCATION_ID"), getIdType(), this);
    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.HIST_LOCATION_ID</code>.
     */
    public final TableField<Record, J> colHistLocationId = createField(DSL.name("HIST_LOCATION_ID"), getIdType(), this);

    /**
     * Create a <code>public.LOCATIONS_HIST_LOCATIONS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     */
    public TableImpLocationsHistLocations(DataType<J> idType) {
        super(idType, DSL.name("LOCATIONS_HIST_LOCATIONS"), null);
    }

    private TableImpLocationsHistLocations(Name alias, TableImpLocationsHistLocations<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    public TableField<Record, J> getLocationId() {
        return colLocationId;
    }

    public TableField<Record, J> getHistLocationId() {
        return colHistLocationId;
    }

    @Override
    public TableImpLocationsHistLocations<J> as(Name alias) {
        return new TableImpLocationsHistLocations<>(alias, this);
    }

    @Override
    public TableImpLocationsHistLocations<J> getThis() {
        return this;
    }

}
