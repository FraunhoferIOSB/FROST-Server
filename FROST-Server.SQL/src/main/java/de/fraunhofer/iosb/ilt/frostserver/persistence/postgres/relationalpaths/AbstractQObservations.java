package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQObservations is a Querydsl query type for Observations
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQObservations<T extends AbstractQObservations, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = -1854525274;

    public final StringPath parameters = createString("parameters");

    public final DateTimePath<java.sql.Timestamp> phenomenonTimeEnd = createDateTime("phenomenonTimeEnd", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> phenomenonTimeStart = createDateTime("phenomenonTimeStart", java.sql.Timestamp.class);

    public final BooleanPath resultBoolean = createBoolean("resultBoolean");

    public final StringPath resultJson = createString("resultJson");

    public final NumberPath<Double> resultNumber = createNumber("resultNumber", Double.class);

    public final StringPath resultQuality = createString("resultQuality");

    public final StringPath resultString = createString("resultString");

    public final DateTimePath<java.sql.Timestamp> resultTime = createDateTime("resultTime", java.sql.Timestamp.class);

    public final NumberPath<Byte> resultType = createNumber("resultType", Byte.class);

    public final DateTimePath<java.sql.Timestamp> validTimeEnd = createDateTime("validTimeEnd", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> validTimeStart = createDateTime("validTimeStart", java.sql.Timestamp.class);

    public AbstractQObservations(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata(parameters, ColumnMetadata.named("PARAMETERS").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(phenomenonTimeEnd, ColumnMetadata.named("PHENOMENON_TIME_END").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(phenomenonTimeStart, ColumnMetadata.named("PHENOMENON_TIME_START").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(resultBoolean, ColumnMetadata.named("RESULT_BOOLEAN").ofType(Types.BOOLEAN).withSize(1));
        addMetadata(resultJson, ColumnMetadata.named("RESULT_JSON").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(resultNumber, ColumnMetadata.named("RESULT_NUMBER").ofType(Types.DOUBLE).withSize(17));
        addMetadata(resultQuality, ColumnMetadata.named("RESULT_QUALITY").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(resultString, ColumnMetadata.named("RESULT_STRING").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(resultTime, ColumnMetadata.named("RESULT_TIME").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(resultType, ColumnMetadata.named("RESULT_TYPE").ofType(Types.TINYINT).withSize(3));
        addMetadata(validTimeEnd, ColumnMetadata.named("VALID_TIME_END").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(validTimeStart, ColumnMetadata.named("VALID_TIME_START").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
    }

    /**
     * @return The Path to the id.
     */
    public abstract I getId();

    /**
     * @return The Path to the id of the Datastream.
     */
    public abstract I getDatastreamId();

    /**
     * @return The Path to the id of the MultiDatastream.
     */
    public abstract I getMultiDatastreamId();

    /**
     * @return The Path to the id of the FeatureOfInterest.
     */
    public abstract I getFeatureId();

    /**
     * Create a new instance, with a different alias.
     *
     * @param variable The alias to use for the new instance.
     * @return a new instance, with the given alias.
     */
    public abstract AbstractQObservations<T, I, J> newWithAlias(String variable);
}
