package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableFeatures;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringFeatures extends AbstractTableFeatures<String> {

    private static final long serialVersionUID = 750481677;

    /**
     * The reference instance of <code>public.FEATURES</code>
     */
    public static final TableStringFeatures FEATURES = new TableStringFeatures();

    /**
     * The column <code>public.FEATURES.ID</code>.
     */
    public final TableField<Record, String> colId = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.FEATURES</code> table reference
     */
    public TableStringFeatures() {
        super();
    }

    /**
     * Create an aliased <code>public.FEATURES</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringFeatures(Name alias) {
        this(alias, FEATURES);
    }

    private TableStringFeatures(Name alias, TableStringFeatures aliased) {
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
    public TableStringFeatures as(String alias) {
        return new TableStringFeatures(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringFeatures as(Name alias) {
        return new TableStringFeatures(alias, this);
    }

}
