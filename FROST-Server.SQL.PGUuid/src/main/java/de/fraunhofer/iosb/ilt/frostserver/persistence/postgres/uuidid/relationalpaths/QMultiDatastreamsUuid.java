package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import java.sql.Types;
import java.util.UUID;

/**
 * QMultiDatastreamsUuid is a Querydsl query type for QMultiDatastreamsUuid
 */
public class QMultiDatastreamsUuid extends AbstractQMultiDatastreams<QMultiDatastreamsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 1620555310;
    private static final String TABLE_NAME = "MULTI_DATASTREAMS";

    public static final QMultiDatastreamsUuid MULTIDATASTREAMS = new QMultiDatastreamsUuid(TABLE_NAME);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final ComparablePath<UUID> sensorId = createComparable("sensorId", UUID.class);

    public final ComparablePath<UUID> thingId = createComparable("thingId", UUID.class);

    public QMultiDatastreamsUuid(String variable) {
        super(QMultiDatastreamsUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
