package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaLinkTable;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableImpThingsLocations extends StaLinkTable<TableImpThingsLocations> {

    public static final String NAME_TABLE = "THINGS_LOCATIONS";
    public static final String NAME_COL_TL_LOCATIONID = "LOCATION_ID";
    public static final String NAME_COL_TL_THINGID = "THING_ID";

    private static final long serialVersionUID = -1443552218;

    /**
     * The column <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, ?> colThingId;

    /**
     * The column <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, ?> colLocationId;

    /**
     * Create a <code>public.THINGS_LOCATIONS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     */
    public TableImpThingsLocations(DataType<?> idTypeThing, DataType<?> idTypeLocation) {
        super(DSL.name(NAME_TABLE), null);
        colThingId = createField(DSL.name(NAME_COL_TL_THINGID), idTypeThing);
        colLocationId = createField(DSL.name(NAME_COL_TL_LOCATIONID), idTypeLocation);
    }

    private TableImpThingsLocations(Name alias, TableImpThingsLocations aliased) {
        super(alias, aliased);
        colThingId = createField(DSL.name(NAME_COL_TL_THINGID), aliased.colThingId.getDataType());
        colLocationId = createField(DSL.name(NAME_COL_TL_LOCATIONID), aliased.colLocationId.getDataType());
    }

    public TableField<Record, ?> getLocationId() {
        return colLocationId;
    }

    public TableField<Record, ?> getThingId() {
        return colThingId;
    }

    @Override
    public TableImpThingsLocations as(Name alias) {
        return new TableImpThingsLocations(alias, this).initCustomFields();
    }

    @Override
    public TableImpThingsLocations getThis() {
        return this;
    }

}
