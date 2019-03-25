package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import java.sql.Types;

/**
 * QMultiDatastreamsLong is a Querydsl query type for QMultiDatastreamsLong
 */
public class QMultiDatastreamsLong extends AbstractQMultiDatastreams<QMultiDatastreamsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -1916297617;
    private static final String TABLE_NAME = "MULTI_DATASTREAMS";

    public static final QMultiDatastreamsLong MULTIDATASTREAMS = new QMultiDatastreamsLong(TABLE_NAME);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> sensorId = createNumber("sensorId", Long.class);

    public final NumberPath<Long> thingId = createNumber("thingId", Long.class);

    public QMultiDatastreamsLong(String variable) {
        super(QMultiDatastreamsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(sensorId, ColumnMetadata.named("SENSOR_ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public NumberPath<Long> getThingId() {
        return thingId;
    }

    @Override
    public NumberPath<Long> getSensorId() {
        return sensorId;
    }

    @Override
    public QMultiDatastreamsLong newWithAlias(String variable) {
        return new QMultiDatastreamsLong(variable);
    }

}
