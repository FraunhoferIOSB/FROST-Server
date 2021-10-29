package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaLinkTable;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableImpLocationsHistLocations extends StaLinkTable<TableImpLocationsHistLocations> {

    public static final String NAME_TABLE = "LOCATIONS_HIST_LOCATIONS";
    public static final String NAME_COL_HISTLOCATIONID = "HIST_LOCATION_ID";
    public static final String NAME_COL_LOCATIONID = "LOCATION_ID";

    private static final long serialVersionUID = -1022733888;

    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, ?> colLocationId;
    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.HIST_LOCATION_ID</code>.
     */
    public final TableField<Record, ?> colHistLocationId;

    /**
     * Create a <code>public.LOCATIONS_HIST_LOCATIONS</code> table reference.
     *
     * @param idTypeLocation The (SQL)DataType of the LocationId column used in
     * the actual database.
     * @param idTypeHistLoc The (SQL)DataType of the HistLocationId column used
     * in the actual database.
     */
    public TableImpLocationsHistLocations(DataType<?> idTypeLocation, DataType<?> idTypeHistLoc) {
        super(DSL.name(NAME_TABLE), null);
        colLocationId = createField(DSL.name(NAME_COL_LOCATIONID), idTypeLocation);
        colHistLocationId = createField(DSL.name(NAME_COL_HISTLOCATIONID), idTypeHistLoc);
    }

    private TableImpLocationsHistLocations(Name alias, TableImpLocationsHistLocations aliased) {
        super(alias, aliased);
        colLocationId = createField(DSL.name(NAME_COL_LOCATIONID), aliased.colLocationId.getDataType());
        colHistLocationId = createField(DSL.name(NAME_COL_HISTLOCATIONID), aliased.colHistLocationId.getDataType());
    }

    public TableField<Record, ?> getLocationId() {
        return colLocationId;
    }

    public TableField<Record, ?> getHistLocationId() {
        return colHistLocationId;
    }

    @Override
    public TableImpLocationsHistLocations as(Name alias) {
        return new TableImpLocationsHistLocations(alias, this).initCustomFields();
    }

    @Override
    public TableImpLocationsHistLocations getThis() {
        return this;
    }

}
