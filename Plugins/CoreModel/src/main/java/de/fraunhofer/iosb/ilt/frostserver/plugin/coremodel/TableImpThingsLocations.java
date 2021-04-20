package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaLinkTable;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableImpThingsLocations<J extends Comparable> extends StaLinkTable<J, TableImpThingsLocations<J>> {

    public static final String NAME_TABLE = "THINGS_LOCATIONS";
    public static final String NAME_COL_TL_LOCATIONID = "LOCATION_ID";
    public static final String NAME_COL_TL_THINGID = "THING_ID";

    private static final long serialVersionUID = -1443552218;

    /**
     * The column <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, J> colThingId = createField(DSL.name(NAME_COL_TL_THINGID), getIdType(), this);

    /**
     * The column <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, J> colLocationId = createField(DSL.name(NAME_COL_TL_LOCATIONID), getIdType(), this);

    /**
     * Create a <code>public.THINGS_LOCATIONS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     */
    public TableImpThingsLocations(DataType<J> idType) {
        super(idType, DSL.name(NAME_TABLE), null);
    }

    private TableImpThingsLocations(Name alias, TableImpThingsLocations<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    public TableField<Record, J> getLocationId() {
        return colLocationId;
    }

    public TableField<Record, J> getThingId() {
        return colThingId;
    }

    @Override
    public TableImpThingsLocations<J> as(Name alias) {
        return new TableImpThingsLocations<>(alias, this).initCustomFields();
    }

    @Override
    public TableImpThingsLocations<J> getThis() {
        return this;
    }

}
