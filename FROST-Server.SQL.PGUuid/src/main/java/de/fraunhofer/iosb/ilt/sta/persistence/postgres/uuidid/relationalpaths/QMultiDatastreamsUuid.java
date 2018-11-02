package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import java.sql.Types;
import java.util.UUID;

/**
 * QMultiDatastreamsUuid is a Querydsl query type for QMultiDatastreamsUuid
 */
public class QMultiDatastreamsUuid extends AbstractQMultiDatastreams<QMultiDatastreamsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 1620555310;

    public static final QMultiDatastreamsUuid MULTIDATASTREAMS = new QMultiDatastreamsUuid("MULTI_DATASTREAMS");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final ComparablePath<UUID> sensorId = createComparable("sensorId", UUID.class);

    public final ComparablePath<UUID> thingId = createComparable("thingId", UUID.class);

    public QMultiDatastreamsUuid(String variable) {
        super(QMultiDatastreamsUuid.class, forVariable(variable), "PUBLIC", "MULTI_DATASTREAMS");
        addMetadata();
    }

    public QMultiDatastreamsUuid(String variable, String schema, String table) {
        super(QMultiDatastreamsUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMultiDatastreamsUuid(String variable, String schema) {
        super(QMultiDatastreamsUuid.class, forVariable(variable), schema, "MULTI_DATASTREAMS");
        addMetadata();
    }

    public QMultiDatastreamsUuid(Path<? extends QMultiDatastreamsUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "MULTI_DATASTREAMS");
        addMetadata();
    }

    public QMultiDatastreamsUuid(PathMetadata metadata) {
        super(QMultiDatastreamsUuid.class, metadata, "PUBLIC", "MULTI_DATASTREAMS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(sensorId, ColumnMetadata.named("SENSOR_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public ComparablePath<UUID> getThingId() {
        return thingId;
    }

    @Override
    public ComparablePath<UUID> getSensorId() {
        return sensorId;
    }

    @Override
    public QMultiDatastreamsUuid newWithAlias(String variable) {
        return new QMultiDatastreamsUuid(variable);
    }

}
