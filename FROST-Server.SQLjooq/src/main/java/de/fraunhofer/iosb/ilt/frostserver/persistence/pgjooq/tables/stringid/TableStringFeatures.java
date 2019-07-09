package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableFeatures;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringFeatures extends AbstractTableFeatures<String> {

    private static final long serialVersionUID = 750481677;

    /**
     * The reference instance of <code>public.FEATURES</code>
     */
    public static final TableStringFeatures FEATURES = new TableStringFeatures();

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
    }

    /**
     * The column <code>public.FEATURES.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

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
