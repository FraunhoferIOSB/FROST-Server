package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostGisGeometryBinding;
import org.geolatte.geom.Geometry;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableLocations<J> extends TableImpl<AbstractRecordLocations<J>> implements StaTable<J, AbstractRecordLocations<J>> {

    private static final long serialVersionUID = -806078255;
    public static final String TABLE_NAME = "LOCATIONS";

    @Override
    public abstract TableField<AbstractRecordLocations<J>, J> getId();

    public abstract TableField<AbstractRecordLocations<J>, J> getGenFoiId();

    @Override
    public final UniqueKey<AbstractRecordLocations<J>> getPrimaryKey() {
        if (primaryKey == null) {
            primaryKey = Internal.createUniqueKey(this, TABLE_NAME + "_PKEY", getId());
        }
        return primaryKey;
    }

    private UniqueKey<AbstractRecordLocations<J>> primaryKey;

    /**
     * The column <code>public.LOCATIONS.DESCRIPTION</code>.
     */
    public final TableField<AbstractRecordLocations<J>, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.LOCATIONS.ENCODING_TYPE</code>.
     */
    public final TableField<AbstractRecordLocations<J>, String> encodingType = createField("ENCODING_TYPE", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.LOCATIONS.LOCATION</code>.
     */
    public final TableField<AbstractRecordLocations<J>, String> location = createField("LOCATION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.LOCATIONS.GEOM</code>.
     */
    public final TableField<AbstractRecordLocations<J>, Geometry> geom = createField("GEOM", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"geometry\""), this, "", new PostGisGeometryBinding());

    /**
     * The column <code>public.LOCATIONS.NAME</code>.
     */
    public final TableField<AbstractRecordLocations<J>, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB.defaultValue(org.jooq.impl.DSL.field("'no name'::text", org.jooq.impl.SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.LOCATIONS.PROPERTIES</code>.
     */
    public final TableField<AbstractRecordLocations<J>, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.LOCATIONS</code> table reference
     */
    protected AbstractTableLocations() {
        this(DSL.name(TABLE_NAME), null);
    }

    protected AbstractTableLocations(Name alias, AbstractTableLocations<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableLocations(Name alias, AbstractTableLocations<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public abstract AbstractTableLocations<J> as(Name as);

    @Override
    public abstract AbstractTableLocations<J> as(String alias);

}
