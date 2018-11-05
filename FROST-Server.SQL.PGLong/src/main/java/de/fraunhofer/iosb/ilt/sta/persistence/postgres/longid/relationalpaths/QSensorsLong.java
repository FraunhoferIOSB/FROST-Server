package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import java.sql.Types;

/**
 * QSensorsLong is a Querydsl query type for QSensorsLong
 */
public class QSensorsLong extends AbstractQSensors<QSensorsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 2019004858;
    private static final String TABLE_NAME = "SENSORS";

    public static final QSensorsLong SENSORS = new QSensorsLong(TABLE_NAME);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QSensorsLong(String variable) {
        super(QSensorsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
