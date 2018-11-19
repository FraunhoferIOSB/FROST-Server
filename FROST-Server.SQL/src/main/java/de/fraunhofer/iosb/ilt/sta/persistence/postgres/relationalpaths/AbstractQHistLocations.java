package de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQHistLocations is a Querydsl query type for AbstractQHistLocations
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQHistLocations<T extends AbstractQHistLocations, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = 244045661;

    public final DateTimePath<java.sql.Timestamp> time = createDateTime("time", java.sql.Timestamp.class);

    public AbstractQHistLocations(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata(time, ColumnMetadata.named("TIME").withIndex(2).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
    }

    /**
     * @return The Path to the id.
     */
    public abstract I getId();

    /**
     * @return The Path to the Thing id.
     */
    public abstract I getThingId();

    /**
     * Create a new instance, with a different alias.
     *
     * @param variable The alias to use for the new instance.
     * @return a new instance, with the given alias.
     */
    public abstract AbstractQHistLocations<T, I, J> newWithAlias(String variable);

}
