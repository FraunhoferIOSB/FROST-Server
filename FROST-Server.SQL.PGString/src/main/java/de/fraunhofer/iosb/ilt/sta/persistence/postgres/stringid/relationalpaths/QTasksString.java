package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQTasks;
import java.sql.Types;

/**
 * QTasksString is a Querydsl query type for Tasks
 */
public class QTasksString extends AbstractQTasks<QTasksString, StringPath, String> {

    private static final long serialVersionUID = 825255311;
    private static final String TABLE_NAME = "TASKS";

    public static final QTasksString TASKS = new QTasksString(TABLE_NAME);

    public final StringPath id = createString("id");

    public final StringPath taskingcapabilityId = createString("taskingcapabilityId");

    public QTasksString(String variable) {
        super(QTasksString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(taskingcapabilityId, ColumnMetadata.named("TASKINGCAPABILITY_ID").ofType(Types.VARCHAR).withSize(36).notNull());
    }

    @Override
    public StringPath getId() {
        return id;
    }

    @Override
    public StringPath getTaskingcapabilityId() {
        return taskingcapabilityId;
    }

    @Override
    public QTasksString newWithAlias(String variable) {
        return new QTasksString(variable);
    }

}
