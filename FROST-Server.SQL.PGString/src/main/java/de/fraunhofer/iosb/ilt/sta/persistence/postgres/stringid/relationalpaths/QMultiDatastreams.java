package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import java.sql.Types;

/**
 * QMultiDatastreams is a Querydsl query type for QMultiDatastreams
 */
public class QMultiDatastreams extends AbstractQMultiDatastreams<QMultiDatastreams, StringPath, String> {

    private static final long serialVersionUID = -1888350652;

    public static final QMultiDatastreams multiDatastreams = new QMultiDatastreams("MULTI_DATASTREAMS");

    public final StringPath id = createString("id");

    public final StringPath sensorId = createString("sensorId");

    public final StringPath thingId = createString("thingId");

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
    public StringPath getId() {
        return id;
    }

    @Override
    public StringPath getThingId() {
        return thingId;
    }

    @Override
    public StringPath getSensorId() {
        return sensorId;
    }

    @Override
    public QMultiDatastreams newWithAlias(String variable) {
        return new QMultiDatastreams(variable);
    }

}
