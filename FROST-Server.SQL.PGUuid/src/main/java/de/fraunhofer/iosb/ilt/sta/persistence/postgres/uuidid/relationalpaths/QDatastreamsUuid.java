package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import java.sql.Types;
import java.util.UUID;

/**
 * QDatastreamsUuid is a Querydsl query type for QDatastreamsUuid
 */
public class QDatastreamsUuid extends AbstractQDatastreams<QDatastreamsUuid, ComparablePath<java.util.UUID>, java.util.UUID> {

    private static final long serialVersionUID = -1859973077;

    public static final QDatastreamsUuid DATASTREAMS = new QDatastreamsUuid("DATASTREAMS");

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> obsPropertyId = createComparable("obsPropertyId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> sensorId = createComparable("sensorId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> thingId = createComparable("thingId", java.util.UUID.class);

    public QDatastreamsUuid(String variable) {
        super(QDatastreamsUuid.class, forVariable(variable), "PUBLIC", "DATASTREAMS");
        addMetadata();
    }

    public QDatastreamsUuid(String variable, String schema, String table) {
        super(QDatastreamsUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDatastreamsUuid(String variable, String schema) {
        super(QDatastreamsUuid.class, forVariable(variable), schema, "DATASTREAMS");
        addMetadata();
    }

    public QDatastreamsUuid(Path<? extends QDatastreamsUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "DATASTREAMS");
        addMetadata();
    }

    public QDatastreamsUuid(PathMetadata metadata) {
        super(QDatastreamsUuid.class, metadata, "PUBLIC", "DATASTREAMS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(obsPropertyId, ColumnMetadata.named("OBS_PROPERTY_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(sensorId, ColumnMetadata.named("SENSOR_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    /**
     * @return the obsPropertyId
     */
    @Override
    public ComparablePath<UUID> getObsPropertyId() {
        return obsPropertyId;
    }

    /**
     * @return the sensorId
     */
    @Override
    public ComparablePath<UUID> getSensorId() {
        return sensorId;
    }

    /**
     * @return the thingId
     */
    @Override
    public ComparablePath<UUID> getThingId() {
        return thingId;
    }

    @Override
    public QDatastreamsUuid newWithAlias(String variable) {
        return new QDatastreamsUuid(variable);
    }

}
