package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableFeatures;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongFeatures extends AbstractTableFeatures<Long> {

    private static final long serialVersionUID = 750481677;

    /**
     * The reference instance of <code>public.FEATURES</code>
     */
    public static final TableLongFeatures FEATURES = new TableLongFeatures();

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return ID;
    }

    /**
     * The column <code>public.FEATURES.ID</code>.
     */
    public final TableField<Record, Long> ID = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"FEATURES_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * Create a <code>public.FEATURES</code> table reference
     */
    public TableLongFeatures() {
        super();
    }

    /**
     * Create an aliased <code>public.FEATURES</code> table reference
     */
    public TableLongFeatures(String alias) {
        this(DSL.name(alias), FEATURES);
    }

    /**
     * Create an aliased <code>public.FEATURES</code> table reference
     */
    public TableLongFeatures(Name alias) {
        this(alias, FEATURES);
    }

    private TableLongFeatures(Name alias, TableLongFeatures aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongFeatures as(String alias) {
        return new TableLongFeatures(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongFeatures as(Name alias) {
        return new TableLongFeatures(alias, this);
    }

}
