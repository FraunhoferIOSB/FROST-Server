package de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.spatial.*;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQMultiDatastreams is a Querydsl query type for
 * AbstractQMultiDatastreams
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQMultiDatastreams<T extends AbstractQMultiDatastreams, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = -1916297617;

    public final StringPath description = createString("description");

    public final StringPath name = createString("name");

    public final StringPath observationTypes = createString("observationTypes");

    public final GeometryPath<org.geolatte.geom.Geometry> observedArea = createGeometry("observedArea", org.geolatte.geom.Geometry.class);

    public final DateTimePath<java.sql.Timestamp> phenomenonTimeEnd = createDateTime("phenomenonTimeEnd", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> phenomenonTimeStart = createDateTime("phenomenonTimeStart", java.sql.Timestamp.class);

    public final StringPath properties = createString("properties");

    public final DateTimePath<java.sql.Timestamp> resultTimeEnd = createDateTime("resultTimeEnd", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> resultTimeStart = createDateTime("resultTimeStart", java.sql.Timestamp.class);

    public final StringPath unitOfMeasurements = createString("unitOfMeasurements");

    public AbstractQMultiDatastreams(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("NAME").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(observationTypes, ColumnMetadata.named("OBSERVATION_TYPES").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(observedArea, ColumnMetadata.named("OBSERVED_AREA").ofType(Types.OTHER).withSize(2147483647));
        addMetadata(phenomenonTimeEnd, ColumnMetadata.named("PHENOMENON_TIME_END").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(phenomenonTimeStart, ColumnMetadata.named("PHENOMENON_TIME_START").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(properties, ColumnMetadata.named("PROPERTIES").ofType(Types.CLOB).withSize(2147483647));
        addMetadata(resultTimeEnd, ColumnMetadata.named("RESULT_TIME_END").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(resultTimeStart, ColumnMetadata.named("RESULT_TIME_START").ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(unitOfMeasurements, ColumnMetadata.named("UNIT_OF_MEASUREMENTS").ofType(Types.CLOB).withSize(2147483647));
    }

    /**
     * @return The Path to the id.
     */
    public abstract I getId();

    /**
     * @return the path to the sensor id
     */
    public abstract I getSensorId();

    /**
     * @return the path to the thing id
     */
    public abstract I getThingId();

    /**
     * Create a new instance, with a different alias.
     *
     * @param variable The alias to use for the new instance.
     * @return a new instance, with the given alias.
     */
    public abstract AbstractQMultiDatastreams<T, I, J> newWithAlias(String variable);
}
