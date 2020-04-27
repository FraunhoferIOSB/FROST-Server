package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableActuators;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringActuators extends AbstractTableActuators<String> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The reference instance of <code>public.ACTUATORS</code>
     */
    public static final TableStringActuators ACTUATORS = new TableStringActuators();

    /**
     * The column <code>public.ACTUATORS.ID</code>.
     */
    public final TableField<Record, String> colId = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.ACTUATORS</code> table reference
     */
    public TableStringActuators() {
        super();
    }

    /**
     * Create an aliased <code>public.ACTUATORS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringActuators(Name alias) {
        this(alias, ACTUATORS);
    }

    private TableStringActuators(Name alias, TableStringActuators aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return colId;
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
