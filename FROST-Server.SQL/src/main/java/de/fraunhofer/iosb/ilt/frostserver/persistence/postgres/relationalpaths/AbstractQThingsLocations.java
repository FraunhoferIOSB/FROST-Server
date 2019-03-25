package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.spatial.RelationalPathSpatial;

/**
 * AbstractQThingsLocations is a Querydsl query type for
 * AbstractQThingsLocations
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQThingsLocations<T extends AbstractQThingsLocations, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = -1059514278;

    public AbstractQThingsLocations(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
    }

    /**
     * @return The Path to the id of the Thing.
     */
    public abstract I getThingId();

    /**
     * @return The Path to the id of the Location.
     */
    public abstract I getLocationId();

    /**
     * Create a new instance, with a different alias.
     *
     * @param variable The alias to use for the new instance.
     * @return a new instance, with the given alias.
     */
    public abstract AbstractQThingsLocations<T, I, J> newWithAlias(String variable);
}
