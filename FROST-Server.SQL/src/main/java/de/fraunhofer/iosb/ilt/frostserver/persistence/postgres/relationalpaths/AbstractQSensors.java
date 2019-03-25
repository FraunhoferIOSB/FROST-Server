package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQSensors is a Querydsl query type for AbstractQSensors
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQSensors<T extends AbstractQSensors, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = 2019004858;

    public final StringPath description = createString("description");

    public final StringPath encodingType = createString("encodingType");

    public final StringPath metadata = createString("metadata");

    public final StringPath name = createString("name");

    public final StringPath properties = createString("properties");

    public AbstractQSensors(Class<? extends T> type, PathMetadata pathMetadata, String schema, String table) {
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
    public abstract AbstractQSensors<T, I, J> newWithAlias(String variable);
}
