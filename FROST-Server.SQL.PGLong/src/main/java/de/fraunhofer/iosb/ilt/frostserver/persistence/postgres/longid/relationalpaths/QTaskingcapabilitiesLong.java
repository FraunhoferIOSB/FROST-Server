package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQTaskingCapabilities;
import java.sql.Types;

/**
 * QTaskingcapabilitiesLong is a Querydsl query type for
 * QTaskingcapabilitiesLong
 */
public class QTaskingcapabilitiesLong extends AbstractQTaskingCapabilities<QTaskingcapabilitiesLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 1215506612;
    private static final String TABLE_NAME = "TASKINGCAPABILITIES";

    public static final QTaskingcapabilitiesLong TASKINGCAPABILITIES = new QTaskingcapabilitiesLong(TABLE_NAME);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> actuatorId = createNumber("actuatorId", Long.class);

    public final NumberPath<Long> thingId = createNumber("thingId", Long.class);

    public QTaskingcapabilitiesLong(String variable) {
        super(QTaskingcapabilitiesLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(actuatorId, ColumnMetadata.named("ACTUATOR_ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    @Override
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public NumberPath<Long> getActuatorId() {
        return actuatorId;
    }

    @Override
    public NumberPath<Long> getThingId() {
        return thingId;
    }

    @Override
    public QTaskingcapabilitiesLong newWithAlias(String variable) {
        return new QTaskingcapabilitiesLong(variable);
    }

}
