package de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQThings is a Querydsl query type for AbstractQThings
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQThings<T extends AbstractQThings, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = -180719772;

    public final StringPath description = createString("description");

    public final StringPath name = createString("name");

    public final StringPath properties = createString("properties");

    public AbstractQThings(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(2).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(4).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(properties, ColumnMetadata.named("PROPERTIES").withIndex(3).ofType(Types.CLOB).withSize(2147483647));
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
    public abstract AbstractQThings<T, I, J> newWithAlias(String variable);
}
