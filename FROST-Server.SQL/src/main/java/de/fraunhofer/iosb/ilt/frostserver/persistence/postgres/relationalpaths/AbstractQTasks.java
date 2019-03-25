package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQTasks is a Querydsl query type for Tasks
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQTasks<T extends AbstractQTasks, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = 825255311;

    public final DateTimePath<java.sql.Timestamp> creationTime = createDateTime("creationTime", java.sql.Timestamp.class);

    public final StringPath taskingParameters = createString("taskingParameters");

    public AbstractQTasks(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata(creationTime, ColumnMetadata.named("CREATION_TIME").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(taskingParameters, ColumnMetadata.named("TASKING_PARAMETERS").ofType(Types.CLOB).withSize(2147483647));
    }

    /**
     * @return The Path to the id.
     */
    public abstract I getId();

    /**
     * @return the Path to the tasking capability id.
     */
    public abstract I getTaskingcapabilityId();

    /**
     * Create a new instance, with a different alias.
     *
     * @param variable The alias to use for the new instance.
     * @return a new instance, with the given alias.
     */
    public abstract AbstractQTasks<T, I, J> newWithAlias(String variable);

}
