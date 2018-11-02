package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThingsLocations;
import java.sql.Types;
import java.util.UUID;

/**
 * QThingsLocationsUuid is a Querydsl query type for QThingsLocationsUuid
 */
public class QThingsLocationsUuid extends AbstractQThingsLocations<QThingsLocationsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = -1915253573;

    public static final QThingsLocationsUuid THINGSLOCATIONS = new QThingsLocationsUuid("THINGS_LOCATIONS");

    public final ComparablePath<UUID> locationId = createComparable("locationId", UUID.class);

    public final ComparablePath<UUID> thingId = createComparable("thingId", UUID.class);

    public QThingsLocationsUuid(String variable) {
        super(QThingsLocationsUuid.class, forVariable(variable), "PUBLIC", "THINGS_LOCATIONS");
        addMetadata();
    }

    public QThingsLocationsUuid(String variable, String schema, String table) {
        super(QThingsLocationsUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QThingsLocationsUuid(String variable, String schema) {
        super(QThingsLocationsUuid.class, forVariable(variable), schema, "THINGS_LOCATIONS");
        addMetadata();
    }

    public QThingsLocationsUuid(Path<? extends QThingsLocationsUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "THINGS_LOCATIONS");
        addMetadata();
    }

    public QThingsLocationsUuid(PathMetadata metadata) {
        super(QThingsLocationsUuid.class, metadata, "PUBLIC", "THINGS_LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(locationId, ColumnMetadata.named("LOCATION_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    @Override
    public ComparablePath<UUID> getLocationId() {
        return locationId;
    }

    @Override
    public ComparablePath<UUID> getThingId() {
        return thingId;
    }

    @Override
    public QThingsLocationsUuid newWithAlias(String variable) {
        return new QThingsLocationsUuid(variable);
    }

}
