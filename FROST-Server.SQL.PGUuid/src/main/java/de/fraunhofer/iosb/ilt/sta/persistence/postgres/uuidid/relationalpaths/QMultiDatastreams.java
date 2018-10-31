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
 * QMultiDatastreams is a Querydsl query type for QMultiDatastreams
 */
public class QMultiDatastreams extends AbstractQMultiDatastreams<QMultiDatastreams, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 1620555310;

    public static final QMultiDatastreams multiDatastreams = new QMultiDatastreams("MULTI_DATASTREAMS");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final ComparablePath<UUID> sensorId = createComparable("sensorId", UUID.class);

    public final ComparablePath<UUID> thingId = createComparable("thingId", UUID.class);

    public final com.querydsl.sql.PrimaryKey<QMultiDatastreams> multiDatastreamsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QSensors> multiDatastreamsSensorIdFkey = createForeignKey(sensorId, "ID");

    public final com.querydsl.sql.ForeignKey<QThings> multiDatastreamsThingIdFkey = createForeignKey(thingId, "ID");

    public final com.querydsl.sql.ForeignKey<QObservations> _observationsMultiDatastreamIdFkey = createInvForeignKey(id, "MULTI_DATASTREAM_ID");

    public final com.querydsl.sql.ForeignKey<QMultiDatastreamsObsProperties> _mdopMultiDatastreamIdFkey = createInvForeignKey(id, "MULTI_DATASTREAM_ID");

    public QMultiDatastreams(String variable) {
        super(QMultiDatastreams.class, forVariable(variable), "PUBLIC", "MULTI_DATASTREAMS");
        addMetadata();
    }

    public QMultiDatastreams(String variable, String schema, String table) {
        super(QMultiDatastreams.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMultiDatastreams(String variable, String schema) {
        super(QMultiDatastreams.class, forVariable(variable), schema, "MULTI_DATASTREAMS");
        addMetadata();
    }

    public QMultiDatastreams(Path<? extends QMultiDatastreams> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "MULTI_DATASTREAMS");
        addMetadata();
    }

    public QMultiDatastreams(PathMetadata metadata) {
        super(QMultiDatastreams.class, metadata, "PUBLIC", "MULTI_DATASTREAMS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(sensorId, ColumnMetadata.named("SENSOR_ID").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
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
    public QMultiDatastreams newWithAlias(String variable) {
        return new QMultiDatastreams(variable);
    }

}
