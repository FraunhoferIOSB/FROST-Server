package de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths;

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
 * AbstractQObservations is a Querydsl query type for AbstractQObservations
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
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(parameters, ColumnMetadata.named("PARAMETERS").withIndex(10).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(phenomenonTimeEnd, ColumnMetadata.named("PHENOMENON_TIME_END").withIndex(3).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(phenomenonTimeStart, ColumnMetadata.named("PHENOMENON_TIME_START").withIndex(2).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(resultBoolean, ColumnMetadata.named("RESULT_BOOLEAN").withIndex(15).ofType(Types.BOOLEAN).withSize(1));
        addMetadata(resultJson, ColumnMetadata.named("RESULT_JSON").withIndex(14).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(resultNumber, ColumnMetadata.named("RESULT_NUMBER").withIndex(5).ofType(Types.DOUBLE).withSize(17));
        addMetadata(resultQuality, ColumnMetadata.named("RESULT_QUALITY").withIndex(7).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(resultString, ColumnMetadata.named("RESULT_STRING").withIndex(6).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(resultTime, ColumnMetadata.named("RESULT_TIME").withIndex(4).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(resultType, ColumnMetadata.named("RESULT_TYPE").withIndex(13).ofType(Types.TINYINT).withSize(3));
        addMetadata(validTimeEnd, ColumnMetadata.named("VALID_TIME_END").withIndex(9).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(validTimeStart, ColumnMetadata.named("VALID_TIME_START").withIndex(8).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
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
