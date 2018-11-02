package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import java.sql.Types;
import java.util.UUID;

/**
 * QHistLocationsUuid is a Querydsl query type for QHistLocationsUuid
 */
public class QHistLocationsUuid extends AbstractQHistLocations<QHistLocationsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = -1683099650;

    public static final QHistLocationsUuid HISTLOCATIONS = new QHistLocationsUuid("HIST_LOCATIONS");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final ComparablePath<UUID> thingId = createComparable("thingId", UUID.class);

    public QHistLocationsUuid(String variable) {
        super(QHistLocationsUuid.class, forVariable(variable), "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocationsUuid(String variable, String schema, String table) {
        super(QHistLocationsUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHistLocationsUuid(String variable, String schema) {
        super(QHistLocationsUuid.class, forVariable(variable), schema, "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocationsUuid(Path<? extends QHistLocationsUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocationsUuid(PathMetadata metadata) {
        super(QHistLocationsUuid.class, metadata, "PUBLIC", "HIST_LOCATIONS");
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
