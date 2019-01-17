package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableSensors;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongSensors extends AbstractTableSensors<Long> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The reference instance of <code>public.SENSORS</code>
     */
    public static final TableLongSensors SENSORS = new TableLongSensors();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return ID;
    }

    /**
     * The column <code>public.SENSORS.ID</code>.
     */
    public final TableField<Record, Long> ID = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"SENSORS_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * Create a <code>public.SENSORS</code> table reference
     */
    public TableLongSensors() {
        super();
    }

    /**
     * Create an aliased <code>public.SENSORS</code> table reference
     */
    public TableLongSensors(String alias) {
        this(DSL.name(alias), SENSORS);
    }

    /**
     * Create an aliased <code>public.SENSORS</code> table reference
     */
    public TableLongSensors(Name alias) {
        this(alias, SENSORS);
    }

    private TableLongSensors(Name alias, TableLongSensors aliased) {
        super(alias, aliased);
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
