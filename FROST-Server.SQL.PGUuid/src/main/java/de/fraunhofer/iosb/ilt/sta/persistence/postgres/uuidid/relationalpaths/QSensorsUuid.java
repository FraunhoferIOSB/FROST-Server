package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import java.sql.Types;
import java.util.UUID;

/**
 * QSensorsUuid is a Querydsl query type for QSensorsUuid
 */
public class QSensorsUuid extends AbstractQSensors<QSensorsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 748484379;
    private static final String TABLE_NAME = "SENSORS";

    public static final QSensorsUuid SENSORS = new QSensorsUuid(TABLE_NAME);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public QSensorsUuid(String variable) {
        super(QSensorsUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public QSensorsUuid newWithAlias(String variable) {
        return new QSensorsUuid(variable);
    }

}
