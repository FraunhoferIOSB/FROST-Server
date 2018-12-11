package de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQActuators is a Querydsl query type for AbstractQActuators
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQActuators<T extends AbstractQActuators, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = -1003317477;

    public final StringPath description = createString("description");

    public final StringPath encodingType = createString("encodingType");

    public final StringPath metadata = createString("metadata");

    public final StringPath name = createString("name");

    public final StringPath properties = createString("properties");

    public AbstractQActuators(Class<? extends T> type, PathMetadata pathMetadata, String schema, String table) {
        super(type, pathMetadata, schema, table);
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(encodingType, ColumnMetadata.named("ENCODING_TYPE").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(metadata, ColumnMetadata.named("METADATA").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("NAME").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(properties, ColumnMetadata.named("PROPERTIES").ofType(Types.CLOB).withSize(2147483647));
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
    public abstract AbstractQActuators<T, I, J> newWithAlias(String variable);

}
