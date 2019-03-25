package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQTaskingCapabilities is a Querydsl query type for Taskingcapabilities
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQTaskingCapabilities<T extends AbstractQTaskingCapabilities, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = 1215506612;

    public final StringPath description = createString("description");

    public final StringPath name = createString("name");

    public final StringPath properties = createString("properties");

    public final StringPath taskingParameters = createString("taskingParameters");

    public AbstractQTaskingCapabilities(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("NAME").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(properties, ColumnMetadata.named("PROPERTIES").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(taskingParameters, ColumnMetadata.named("TASKING_PARAMETERS").ofType(Types.CLOB).withSize(2147483647));
    }

    /**
     * @return The Path to the id.
     */
    public abstract I getId();

    /**
     * @return The Path to the id of the Actor.
     */
    public abstract I getActuatorId();

    /**
     * @return The Path to the id of the Actor.
     */
    public abstract I getThingId();

    /**
     * Create a new instance, with a different alias.
     *
     * @param variable The alias to use for the new instance.
     * @return a new instance, with the given alias.
     */
    public abstract AbstractQTaskingCapabilities<T, I, J> newWithAlias(String variable);

}
