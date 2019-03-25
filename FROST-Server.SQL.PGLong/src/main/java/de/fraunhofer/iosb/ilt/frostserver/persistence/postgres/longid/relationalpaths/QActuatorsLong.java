package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQActuators;
import java.sql.Types;

/**
 * QActuatorsLong is a Querydsl query type for QActuatorsLong
 */
public class QActuatorsLong extends AbstractQActuators<QActuatorsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -1003317477;
    private static final String TABLE_NAME = "ACTUATORS";

    public static final QActuatorsLong ACTUATORS = new QActuatorsLong(TABLE_NAME);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QActuatorsLong(String variable) {
        super(QActuatorsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
    public QActuatorsLong newWithAlias(String variable) {
        return new QActuatorsLong(variable);
    }

}
