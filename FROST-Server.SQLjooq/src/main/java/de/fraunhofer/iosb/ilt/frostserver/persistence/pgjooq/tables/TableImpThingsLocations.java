package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableImpThingsLocations<J extends Comparable> extends StaLinkTable<J, TableImpThingsLocations<J>> {

    private static final long serialVersionUID = -1443552218;

    private static TableImpThingsLocations INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> TableImpThingsLocations<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new TableImpThingsLocations(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new TableImpThingsLocations<>(idType);
    }

    /**
     * The column <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, J> colThingId = createField(DSL.name("THING_ID"), getIdType(), this);

    /**
     * The column <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, J> colLocationId = createField(DSL.name("LOCATION_ID"), getIdType(), this);

    /**
     * Create a <code>public.THINGS_LOCATIONS</code> table reference
     */
    private TableImpThingsLocations(DataType<J> idType) {
        super(idType, DSL.name("THINGS_LOCATIONS"), null);
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
        return new TableImpThingsLocations<>(alias, this);
    }

    @Override
    public TableImpThingsLocations<J> as(String alias) {
        return new TableImpThingsLocations<>(DSL.name(alias), this);
    }

    @Override
    public TableImpThingsLocations<J> getThis() {
        return this;
    }

}
