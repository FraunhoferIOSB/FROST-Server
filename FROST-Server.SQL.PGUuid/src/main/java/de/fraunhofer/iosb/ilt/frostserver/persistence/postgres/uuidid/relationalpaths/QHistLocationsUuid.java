package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQHistLocations;
import java.sql.Types;
import java.util.UUID;

/**
 * QHistLocationsUuid is a Querydsl query type for QHistLocationsUuid
 */
public class QHistLocationsUuid extends AbstractQHistLocations<QHistLocationsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = -1683099650;
    private static final String TABLE_NAME = "HIST_LOCATIONS";

    public static final QHistLocationsUuid HISTLOCATIONS = new QHistLocationsUuid(TABLE_NAME);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final ComparablePath<UUID> thingId = createComparable("thingId", UUID.class);

    public QHistLocationsUuid(String variable) {
        super(QHistLocationsUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public ComparablePath<UUID> getThingId() {
        return thingId;
    }

    @Override
    public QHistLocationsUuid newWithAlias(String variable) {
        return new QHistLocationsUuid(variable);
    }

}
