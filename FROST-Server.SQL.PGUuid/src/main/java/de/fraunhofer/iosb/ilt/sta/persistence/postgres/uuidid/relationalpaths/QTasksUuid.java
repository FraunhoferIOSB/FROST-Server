package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQTasks;
import java.sql.Types;
import java.util.UUID;

/**
 * QTasksUuid is a Querydsl query type for Tasks
 */
public class QTasksUuid extends AbstractQTasks<QTasksUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 825255311;
    private static final String TABLE_NAME = "TASKS";

    public static final QTasksUuid TASKS = new QTasksUuid(TABLE_NAME);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final ComparablePath<UUID> taskingcapabilityId = createComparable("taskingcapabilityId", UUID.class);

    public QTasksUuid(String variable) {
        super(QTasksUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(taskingcapabilityId, ColumnMetadata.named("TASKINGCAPABILITY_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public ComparablePath<UUID> getTaskingcapabilityId() {
        return taskingcapabilityId;
    }

    @Override
    public QTasksUuid newWithAlias(String variable) {
        return new QTasksUuid(variable);
    }

}
