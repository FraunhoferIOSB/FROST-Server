package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import java.sql.Types;

/**
 * QSensorsLong is a Querydsl query type for QSensorsLong
 */
public class QSensorsLong extends AbstractQSensors<QSensorsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 2019004858;

    public static final QSensorsLong SENSORS = new QSensorsLong("SENSORS");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QSensorsLong(String variable) {
        super(QSensorsLong.class, forVariable(variable), "PUBLIC", "SENSORS");
        addMetadata();
    }

    public QSensorsLong(String variable, String schema, String table) {
        super(QSensorsLong.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSensorsLong(String variable, String schema) {
        super(QSensorsLong.class, forVariable(variable), schema, "SENSORS");
        addMetadata();
    }

    public QSensorsLong(Path<? extends QSensorsLong> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "SENSORS");
        addMetadata();
    }

    public QSensorsLong(PathMetadata metadata) {
        super(QSensorsLong.class, metadata, "PUBLIC", "SENSORS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public QSensorsLong newWithAlias(String variable) {
        return new QSensorsLong(variable);
    }

}
