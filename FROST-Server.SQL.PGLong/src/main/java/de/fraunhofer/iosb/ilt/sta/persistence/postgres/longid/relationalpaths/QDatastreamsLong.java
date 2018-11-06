package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import java.sql.Types;

/**
 * QDatastreamsLong is a Querydsl query type for QDatastreamsLong
 */
public class QDatastreamsLong extends AbstractQDatastreams<QDatastreamsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -222215350;
    private static final String TABLE_NAME = "DATASTREAMS";

    public static final QDatastreamsLong DATASTREAMS = new QDatastreamsLong(TABLE_NAME);

    private final NumberPath<Long> id = createNumber("id", Long.class);

    private final NumberPath<Long> obsPropertyId = createNumber("obsPropertyId", Long.class);

    private final NumberPath<Long> sensorId = createNumber("sensorId", Long.class);

    private final NumberPath<Long> thingId = createNumber("thingId", Long.class);

    public QDatastreamsLong(String variable) {
        super(QDatastreamsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(obsPropertyId, ColumnMetadata.named("OBS_PROPERTY_ID").ofType(Types.BIGINT).withSize(19).notNull());
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

    /**
     * @return the obsPropertyId
     */
    @Override
    public NumberPath<Long> getObsPropertyId() {
        return obsPropertyId;
    }

    /**
     * @return the sensorId
     */
    @Override
    public NumberPath<Long> getSensorId() {
        return sensorId;
    }

    /**
     * @return the thingId
     */
    @Override
    public NumberPath<Long> getThingId() {
        return thingId;
    }

    @Override
    public QDatastreamsLong newWithAlias(String variable) {
        return new QDatastreamsLong(variable);
    }

}
