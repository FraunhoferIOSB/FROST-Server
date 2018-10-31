package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import java.sql.Types;

/**
 * QSensors is a Querydsl query type for QSensors
 */
public class QSensors extends AbstractQSensors<QSensors, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 2019004858;

    public static final QSensors sensors = new QSensors("SENSORS");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.querydsl.sql.PrimaryKey<QSensors> sensorsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QMultiDatastreams> _multiDatastreamsSensorIdFkey = createInvForeignKey(id, "SENSOR_ID");

    public final com.querydsl.sql.ForeignKey<QDatastreams> _datastreamsSensorIdFkey = createInvForeignKey(id, "SENSOR_ID");

    public QSensors(String variable) {
        super(QSensors.class, forVariable(variable), "PUBLIC", "SENSORS");
        addMetadata();
    }

    public QSensors(String variable, String schema, String table) {
        super(QSensors.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSensors(String variable, String schema) {
        super(QSensors.class, forVariable(variable), schema, "SENSORS");
        addMetadata();
    }

    public QSensors(Path<? extends QSensors> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "SENSORS");
        addMetadata();
    }

    public QSensors(PathMetadata metadata) {
        super(QSensors.class, metadata, "PUBLIC", "SENSORS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public QSensors newWithAlias(String variable) {
        return new QSensors(variable);
    }

}
