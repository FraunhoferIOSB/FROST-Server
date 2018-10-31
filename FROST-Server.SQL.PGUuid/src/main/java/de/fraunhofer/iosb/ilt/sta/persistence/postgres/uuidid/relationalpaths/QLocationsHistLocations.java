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
 * QLocationsHistLocations is a Querydsl query type for QLocationsHistLocations
 */
public class QLocationsHistLocations extends AbstractQLocationsHistLocations<QLocationsHistLocations, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 68027452;

    public static final QLocationsHistLocations locationsHistLocations = new QLocationsHistLocations("LOCATIONS_HIST_LOCATIONS");

    public final ComparablePath<UUID> histLocationId = createComparable("histLocationId", UUID.class);

    public final ComparablePath<UUID> locationId = createComparable("locationId", UUID.class);

    public final com.querydsl.sql.PrimaryKey<QLocationsHistLocations> locationsHistLocationsPkey = createPrimaryKey(histLocationId, locationId);

    public final com.querydsl.sql.ForeignKey<QLocations> locationsHistLocationsLocationIdFkey = createForeignKey(locationId, "ID");

    public final com.querydsl.sql.ForeignKey<QHistLocations> locationsHistLocationsHistLocationIdFkey = createForeignKey(histLocationId, "ID");

    public QLocationsHistLocations(String variable) {
        super(QLocationsHistLocations.class, forVariable(variable), "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocations(String variable, String schema, String table) {
        super(QLocationsHistLocations.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocationsHistLocations(String variable, String schema) {
        super(QLocationsHistLocations.class, forVariable(variable), schema, "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocations(Path<? extends QLocationsHistLocations> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocations(PathMetadata metadata) {
        super(QLocationsHistLocations.class, metadata, "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(histLocationId, ColumnMetadata.named("HIST_LOCATION_ID").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(locationId, ColumnMetadata.named("LOCATION_ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
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
    public QLocationsHistLocations newWithAlias(String variable) {
        return new QLocationsHistLocations(variable);
    }

}
