package de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQMultiDatastreamsObsProperties is a Querydsl query type for
 * AbstractQMultiDatastreamsObsProperties
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQMultiDatastreamsObsProperties<T extends AbstractQMultiDatastreamsObsProperties, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = -838888412;

    public final NumberPath<Integer> rank = createNumber("rank", Integer.class);

    public AbstractQMultiDatastreamsObsProperties(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(rank, ColumnMetadata.named("RANK").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
    }

    /**
     * @return The Path to the id of the MultiDatastream.
     */
    public abstract I getMultiDatastreamId();

    /**
     * @return The Path to the id of the ObservedProperties.
     */
    public abstract I getObsPropertyId();

    /**
     * Create a new instance, with a different alias.
     *
     * @param variable The alias to use for the new instance.
     * @return a new instance, with the given alias.
     */
    public abstract AbstractQMultiDatastreamsObsProperties<T, I, J> newWithAlias(String variable);
}
