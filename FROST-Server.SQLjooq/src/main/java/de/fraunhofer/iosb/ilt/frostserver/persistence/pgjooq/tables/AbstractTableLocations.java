package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import org.geolatte.geom.Geometry;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableLocations<J extends Comparable> extends StaTableAbstract<J> {

    private static final long serialVersionUID = -806078255;
    public static final String TABLE_NAME = "LOCATIONS";

    private UniqueKey<Record> primaryKey;

    /**
     * The column <code>public.LOCATIONS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.LOCATIONS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name("ENCODING_TYPE"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.LOCATIONS.LOCATION</code>.
     */
    public final TableField<Record, String> colLocation = createField(DSL.name("LOCATION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.LOCATIONS.GEOM</code>.
     */
    public final TableField<Record, Geometry> colGeom = createField(DSL.name("GEOM"), DefaultDataType.getDefaultDataType("\"public\".\"geometry\""), this, "", new PostGisGeometryBinding());

    /**
     * The column <code>public.LOCATIONS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.LOCATIONS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType("\"pg_catalog\".\"jsonb\""), this, "", new JsonBinding());

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
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationManyToMany<>(this, tables.getTableThingsLocations(), tables.getTableThings(), EntityType.THING)
                        .setSourceFieldAcc(AbstractTableLocations::getId)
                        .setSourceLinkFieldAcc(AbstractTableThingsLocations::getLocationId)
                        .setTargetLinkFieldAcc(AbstractTableThingsLocations::getThingId)
                        .setTargetFieldAcc(AbstractTableThings::getId)
        );

        registerRelation(
                new RelationManyToMany<>(this, tables.getTableLocationsHistLocations(), tables.getTableHistLocations(), EntityType.HISTORICALLOCATION)
                        .setSourceFieldAcc(AbstractTableLocations::getId)
                        .setSourceLinkFieldAcc(AbstractTableLocationsHistLocations::getLocationId)
                        .setTargetLinkFieldAcc(AbstractTableLocationsHistLocations::getHistLocationId)
                        .setTargetFieldAcc(AbstractTableHistLocations::getId)
        );
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getGenFoiId();

    @Override
    public final UniqueKey<Record> getPrimaryKey() {
        if (primaryKey == null) {
            primaryKey = Internal.createUniqueKey(this, TABLE_NAME + "_PKEY", getId());
        }
        return primaryKey;
    }

    @Override
    public abstract AbstractTableLocations<J> as(Name as);

    @Override
    public abstract AbstractTableLocations<J> as(String alias);

}
