package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableActuators;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongActuators extends AbstractTableActuators<Long> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The reference instance of <code>public.ACTUATORS</code>
     */
    public static final TableLongActuators ACTUATORS = new TableLongActuators();

    /**
     * The column <code>public.ACTUATORS.ID</code>.
     */
    public final TableField<Record, Long> id = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.field("nextval('\"ACTUATORS_ID_seq\"'::regclass)", SQLDataType.BIGINT)), this, "");

    /**
     * Create a <code>public.ACTUATORS</code> table reference
     */
    public TableLongActuators() {
        super();
    }

    /**
     * Create an aliased <code>public.ACTUATORS</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableLongActuators(Name alias) {
        this(alias, ACTUATORS);
    }

    private TableLongActuators(Name alias, TableLongActuators aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return id;
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
