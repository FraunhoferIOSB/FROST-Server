package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import java.sql.Types;
import java.util.UUID;

/**
 * QSensorsUuid is a Querydsl query type for QSensorsUuid
 */
public class QSensorsUuid extends AbstractQSensors<QSensorsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 748484379;

    public static final QSensorsUuid SENSORS = new QSensorsUuid("SENSORS");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public QSensorsUuid(String variable) {
        super(QSensorsUuid.class, forVariable(variable), "PUBLIC", "SENSORS");
        addMetadata();
    }

    public QSensorsUuid(String variable, String schema, String table) {
        super(QSensorsUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSensorsUuid(String variable, String schema) {
        super(QSensorsUuid.class, forVariable(variable), schema, "SENSORS");
        addMetadata();
    }

    public QSensorsUuid(Path<? extends QSensorsUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "SENSORS");
        addMetadata();
    }

    public QSensorsUuid(PathMetadata metadata) {
        super(QSensorsUuid.class, metadata, "PUBLIC", "SENSORS");
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
