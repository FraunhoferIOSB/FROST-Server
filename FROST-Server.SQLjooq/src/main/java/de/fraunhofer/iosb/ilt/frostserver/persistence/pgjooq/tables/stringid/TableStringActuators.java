package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableActuators;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringActuators extends AbstractTableActuators<String> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The reference instance of <code>public.ACTUATORS</code>
     */
    public static final TableStringActuators ACTUATORS = new TableStringActuators();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
    }

    /**
     * The column <code>public.ACTUATORS.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.ACTUATORS</code> table reference
     */
    public TableStringActuators() {
        super();
    }

    /**
     * Create an aliased <code>public.ACTUATORS</code> table reference
     */
    public TableStringActuators(String alias) {
        this(DSL.name(alias), ACTUATORS);
    }

    /**
     * Create an aliased <code>public.ACTUATORS</code> table reference
     */
    public TableStringActuators(Name alias) {
        this(alias, ACTUATORS);
    }

    private TableStringActuators(Name alias, TableStringActuators aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringActuators as(String alias) {
        return new TableStringActuators(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringActuators as(Name alias) {
        return new TableStringActuators(alias, this);
    }

}
