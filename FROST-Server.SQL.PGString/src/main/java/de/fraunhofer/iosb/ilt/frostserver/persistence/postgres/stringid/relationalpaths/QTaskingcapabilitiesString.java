package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.stringid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQTaskingCapabilities;
import java.sql.Types;

/**
 * QTaskingcapabilitiesString is a Querydsl query type for Taskingcapabilities
 */
public class QTaskingcapabilitiesString extends AbstractQTaskingCapabilities<QTaskingcapabilitiesString, StringPath, String> {

    private static final long serialVersionUID = 1215506612;
    private static final String TABLE_NAME = "TASKINGCAPABILITIES";

    public static final QTaskingcapabilitiesString TASKINGCAPABILITIES = new QTaskingcapabilitiesString(TABLE_NAME);

    public final StringPath id = createString("id");

    public final StringPath actuatorId = createString("actuatorId");

    public final StringPath thingId = createString("thingId");

    public QTaskingcapabilitiesString(String variable) {
        super(QTaskingcapabilitiesString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(actuatorId, ColumnMetadata.named("ACTUATOR_ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.VARCHAR).withSize(36).notNull());
    }

    @Override
    public StringPath getId() {
        return id;
    }

    @Override
    public StringPath getActuatorId() {
        return actuatorId;
    }

    @Override
    public StringPath getThingId() {
        return thingId;
    }

    @Override
    public QTaskingcapabilitiesString newWithAlias(String variable) {
        return new QTaskingcapabilitiesString(variable);
    }

}
