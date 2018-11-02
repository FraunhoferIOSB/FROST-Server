package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import java.sql.Types;
import java.util.UUID;

/**
 * QLocationsHistLocationsUuid is a Querydsl query type for QLocationsHistLocationsUuid
 */
public class QLocationsHistLocationsUuid extends AbstractQLocationsHistLocations<QLocationsHistLocationsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 68027452;

    public static final QLocationsHistLocationsUuid LOCATIONSHISTLOCATIONS = new QLocationsHistLocationsUuid("LOCATIONS_HIST_LOCATIONS");

    public final ComparablePath<UUID> histLocationId = createComparable("histLocationId", UUID.class);

    public final ComparablePath<UUID> locationId = createComparable("locationId", UUID.class);

    public QLocationsHistLocationsUuid(String variable) {
        super(QLocationsHistLocationsUuid.class, forVariable(variable), "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocationsUuid(String variable, String schema, String table) {
        super(QLocationsHistLocationsUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocationsHistLocationsUuid(String variable, String schema) {
        super(QLocationsHistLocationsUuid.class, forVariable(variable), schema, "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocationsUuid(Path<? extends QLocationsHistLocationsUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocationsUuid(PathMetadata metadata) {
        super(QLocationsHistLocationsUuid.class, metadata, "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(histLocationId, ColumnMetadata.named("HIST_LOCATION_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(locationId, ColumnMetadata.named("LOCATION_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    @Override
    public ComparablePath<UUID> getLocationId() {
        return locationId;
    }

    @Override
    public ComparablePath<UUID> getHistLocationId() {
        return histLocationId;
    }

    @Override
    public QLocationsHistLocationsUuid newWithAlias(String variable) {
        return new QLocationsHistLocationsUuid(variable);
    }

}
