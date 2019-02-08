package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableSensors;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringSensors extends AbstractTableSensors<String> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The reference instance of <code>public.SENSORS</code>
     */
    public static final TableStringSensors SENSORS = new TableStringSensors();

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
     * The column <code>public.SENSORS.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.SENSORS</code> table reference
     */
    public TableStringSensors() {
        super();
    }

    /**
     * Create an aliased <code>public.SENSORS</code> table reference
     */
    public TableStringSensors(String alias) {
        this(DSL.name(alias), SENSORS);
    }

    /**
     * Create an aliased <code>public.SENSORS</code> table reference
     */
    public TableStringSensors(Name alias) {
        this(alias, SENSORS);
    }

    private TableStringSensors(Name alias, TableStringSensors aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringSensors as(String alias) {
        return new TableStringSensors(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringSensors as(Name alias) {
        return new TableStringSensors(alias, this);
    }

}
