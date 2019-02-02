package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQTaskingCapabilities;
import java.sql.Types;
import java.util.UUID;

/**
 * QTaskingcapabilitiesUuid is a Querydsl query type for Taskingcapabilities
 */
public class QTaskingcapabilitiesUuid extends AbstractQTaskingCapabilities<QTaskingcapabilitiesUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 1215506612;
    private static final String TABLE_NAME = "TASKINGCAPABILITIES";

    public static final QTaskingcapabilitiesUuid TASKINGCAPABILITIES = new QTaskingcapabilitiesUuid(TABLE_NAME);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final ComparablePath<UUID> actuatorId = createComparable("actuatorId", UUID.class);

    public final ComparablePath<UUID> thingId = createComparable("thingId", UUID.class);

    public QTaskingcapabilitiesUuid(String variable) {
        super(QTaskingcapabilitiesUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(actuatorId, ColumnMetadata.named("ACTUATOR_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public ComparablePath<UUID> getActuatorId() {
        return actuatorId;
    }

    @Override
    public ComparablePath<UUID> getThingId() {
        return thingId;
    }

    @Override
    public QTaskingcapabilitiesUuid newWithAlias(String variable) {
        return new QTaskingcapabilitiesUuid(variable);
    }

}
