package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableFeatures;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongFeatures extends AbstractTableFeatures<Long> {

    private static final long serialVersionUID = 750481677;

    /**
     * The reference instance of <code>public.FEATURES</code>
     */
    public static final TableLongFeatures FEATURES = new TableLongFeatures();

    /**
     * The column <code>public.FEATURES.ID</code>.
     */
    public final TableField<Record, Long> colId = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.field("nextval('\"FEATURES_ID_seq\"'::regclass)", SQLDataType.BIGINT)), this, "");

    /**
     * Create a <code>public.FEATURES</code> table reference
     */
    public TableLongFeatures() {
        super();
    }

    /**
     * Create an aliased <code>public.FEATURES</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableLongFeatures(Name alias) {
        this(alias, FEATURES);
    }

    private TableLongFeatures(Name alias, TableLongFeatures aliased) {
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
