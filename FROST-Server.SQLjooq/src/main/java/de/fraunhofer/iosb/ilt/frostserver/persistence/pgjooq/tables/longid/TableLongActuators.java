package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableActuators;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongActuators extends AbstractTableActuators<Long> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The reference instance of <code>public.ACTUATORS</code>
     */
    public static final TableLongActuators ACTUATORS = new TableLongActuators();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return id;
    }

    /**
     * The column <code>public.ACTUATORS.ID</code>.
     */
    public final TableField<Record, Long> id = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"ACTUATORS_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * Create a <code>public.ACTUATORS</code> table reference
     */
    public TableLongActuators() {
        super();
    }

    /**
     * Create an aliased <code>public.ACTUATORS</code> table reference
     */
    public TableLongActuators(String alias) {
        this(DSL.name(alias), ACTUATORS);
    }

    /**
     * Create an aliased <code>public.ACTUATORS</code> table reference
     */
    public TableLongActuators(Name alias) {
        this(alias, ACTUATORS);
    }

    private TableLongActuators(Name alias, TableLongActuators aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongActuators as(String alias) {
        return new TableLongActuators(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongActuators as(Name alias) {
        return new TableLongActuators(alias, this);
    }

}
