package de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.spatial.RelationalPathSpatial;

/**
 * AbstractQLocationsHistLocations is a Querydsl query type for
 * AbstractQLocationsHistLocations
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQLocationsHistLocations<T extends AbstractQLocationsHistLocations, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = 1713698749;

    public AbstractQLocationsHistLocations(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
    }

    /**
     * @return The Path to the Location id.
     */
    public abstract I getLocationId();

    /**
     * @return The Path to the HistLocation id.
     */
    public abstract I getHistLocationId();

    /**
     * Create a new instance, with a different alias.
     *
     * @param variable The alias to use for the new instance.
     * @return a new instance, with the given alias.
     */
    public abstract AbstractQLocationsHistLocations<T, I, J> newWithAlias(String variable);

}
