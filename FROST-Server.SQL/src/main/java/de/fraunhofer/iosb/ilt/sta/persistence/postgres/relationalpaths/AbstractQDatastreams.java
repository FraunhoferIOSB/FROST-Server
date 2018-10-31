package de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.spatial.GeometryPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.spatial.RelationalPathSpatial;
import java.sql.Types;

/**
 * AbstractQDatastreams is a Querydsl query type for AbstractQDatastreams
 *
 * @param <T> The implementation of this abstract class.
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public abstract class AbstractQDatastreams<T extends AbstractQDatastreams, I extends SimpleExpression<J> & Path<J>, J> extends RelationalPathSpatial<T> {

    private static final long serialVersionUID = -222215350;

    public final StringPath description = createString("description");

    public final StringPath name = createString("name");

    public final StringPath observationType = createString("observationType");

    public final GeometryPath<org.geolatte.geom.Geometry> observedArea = createGeometry("observedArea", org.geolatte.geom.Geometry.class);

    public final DateTimePath<java.sql.Timestamp> phenomenonTimeEnd = createDateTime("phenomenonTimeEnd", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> phenomenonTimeStart = createDateTime("phenomenonTimeStart", java.sql.Timestamp.class);

    public final StringPath properties = createString("properties");

    public final DateTimePath<java.sql.Timestamp> resultTimeEnd = createDateTime("resultTimeEnd", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> resultTimeStart = createDateTime("resultTimeStart", java.sql.Timestamp.class);

    public final StringPath unitDefinition = createString("unitDefinition");

    public final StringPath unitName = createString("unitName");

    public final StringPath unitSymbol = createString("unitSymbol");

    public AbstractQDatastreams(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(2).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(14).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(observationType, ColumnMetadata.named("OBSERVATION_TYPE").withIndex(3).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(observedArea, ColumnMetadata.named("OBSERVED_AREA").withIndex(15).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(phenomenonTimeEnd, ColumnMetadata.named("PHENOMENON_TIME_END").withIndex(5).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(phenomenonTimeStart, ColumnMetadata.named("PHENOMENON_TIME_START").withIndex(4).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(properties, ColumnMetadata.named("PROPERTIES").withIndex(16).ofType(Types.CLOB).withSize(2147483647));
        addMetadata(resultTimeEnd, ColumnMetadata.named("RESULT_TIME_END").withIndex(7).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(resultTimeStart, ColumnMetadata.named("RESULT_TIME_START").withIndex(6).ofType(Types.TIMESTAMP).withSize(23).withDigits(10));
        addMetadata(unitDefinition, ColumnMetadata.named("UNIT_DEFINITION").withIndex(13).ofType(Types.VARCHAR).withSize(255));
        addMetadata(unitName, ColumnMetadata.named("UNIT_NAME").withIndex(11).ofType(Types.VARCHAR).withSize(255));
        addMetadata(unitSymbol, ColumnMetadata.named("UNIT_SYMBOL").withIndex(12).ofType(Types.VARCHAR).withSize(255));
    }

    /**
     * @return The Path to the id.
     */
    public abstract I getId();

    /**
     * @return the Path to the observed property id
     */
    public abstract I getObsPropertyId();

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
    public abstract AbstractQDatastreams<T, I, J> newWithAlias(String variable);
}
