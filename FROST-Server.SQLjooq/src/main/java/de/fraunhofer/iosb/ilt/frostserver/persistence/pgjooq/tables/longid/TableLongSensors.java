package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableSensors;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongSensors extends AbstractTableSensors<Long> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The reference instance of <code>public.SENSORS</code>
     */
    public static final TableLongSensors SENSORS = new TableLongSensors();

    /**
     * The column <code>public.SENSORS.ID</code>.
     */
    public final TableField<Record, Long> colId = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.field("nextval('\"SENSORS_ID_seq\"'::regclass)", SQLDataType.BIGINT)), this, "");

    /**
     * Create a <code>public.SENSORS</code> table reference
     */
    public TableLongSensors() {
        super();
    }

    /**
     * Create an aliased <code>public.SENSORS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableLongSensors(Name alias) {
        this(alias, SENSORS);
    }

    private TableLongSensors(Name alias, TableLongSensors aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return colId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongSensors as(String alias) {
        return new TableLongSensors(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongSensors as(Name alias) {
        return new TableLongSensors(alias, this);
    }

}
