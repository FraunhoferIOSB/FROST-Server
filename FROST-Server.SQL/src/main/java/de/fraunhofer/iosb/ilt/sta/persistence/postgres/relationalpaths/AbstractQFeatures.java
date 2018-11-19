package de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.spatial.GeometryPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQFeatures is a Querydsl query type for AbstractQFeatures
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQFeatures<T extends AbstractQFeatures, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = 906833564;

    public final StringPath description = createString("description");

    public final StringPath encodingType = createString("encodingType");

    public final StringPath feature = createString("feature");

    public final GeometryPath<org.geolatte.geom.Geometry> geom = createGeometry("geom", org.geolatte.geom.Geometry.class);

    public final StringPath name = createString("name");

    public final StringPath properties = createString("properties");

    public AbstractQFeatures(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(2).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(encodingType, ColumnMetadata.named("ENCODING_TYPE").withIndex(3).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(feature, ColumnMetadata.named("FEATURE").withIndex(4).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(geom, ColumnMetadata.named("GEOM").withIndex(5).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(properties, ColumnMetadata.named("PROPERTIES").withIndex(7).ofType(Types.CLOB).withSize(2147483647));
    }

    /**
     * @return The Path to the id.
     */
    public abstract I getId();

    /**
     * Create a new instance, with a different alias.
     *
     * @param variable The alias to use for the new instance.
     * @return a new instance, with the given alias.
     */
    public abstract AbstractQFeatures<T, I, J> newWithAlias(String variable);

}
