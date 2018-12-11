package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQTasks;
import java.sql.Types;

/**
 * QTasksLong is a Querydsl query type for Tasks
 */
public class QTasksLong extends AbstractQTasks<QTasksLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 825255311;
    private static final String TABLE_NAME = "TASKS";

    public static final QTasksLong TASKS = new QTasksLong(TABLE_NAME);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> taskingcapabilityId = createNumber("taskingcapabilityId", Long.class);

    public QTasksLong(String variable) {
        super(QTasksLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(taskingcapabilityId, ColumnMetadata.named("TASKINGCAPABILITY_ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    @Override
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public NumberPath<Long> getTaskingcapabilityId() {
        return taskingcapabilityId;
    }

    @Override
    public QTasksLong newWithAlias(String variable) {
        return new QTasksLong(variable);
    }

}
