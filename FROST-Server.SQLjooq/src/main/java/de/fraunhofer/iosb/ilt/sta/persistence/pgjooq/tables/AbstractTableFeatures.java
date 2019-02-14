package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostGisGeometryBinding;
import org.geolatte.geom.Geometry;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableFeatures<J> extends TableImpl<Record> implements StaTable<J> {

    private static final long serialVersionUID = 750481677;

    public abstract TableField<Record, J> getId();

    /**
     * The column <code>public.FEATURES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.FEATURES.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> encodingType = createField("ENCODING_TYPE", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.FEATURES.FEATURE</code>.
     */
    public final TableField<Record, String> feature = createField("FEATURE", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.FEATURES.GEOM</code>.
     */
    public final TableField<Record, Geometry> geom = createField("GEOM", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"geometry\""), this, "", new PostGisGeometryBinding());

    /**
     * The column <code>public.FEATURES.NAME</code>.
     */
    public final TableField<Record, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB.defaultValue(org.jooq.impl.DSL.field("'no name'::text", org.jooq.impl.SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.FEATURES.PROPERTIES</code>.
     */
    public final TableField<Record, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.FEATURES</code> table reference
     */
    protected AbstractTableFeatures() {
        this(DSL.name("FEATURES"), null);
    }

    protected AbstractTableFeatures(Name alias, AbstractTableFeatures<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableFeatures(Name alias, AbstractTableFeatures<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public abstract AbstractTableFeatures<J> as(Name as);

    @Override
    public abstract AbstractTableFeatures<J> as(String alias);

}
