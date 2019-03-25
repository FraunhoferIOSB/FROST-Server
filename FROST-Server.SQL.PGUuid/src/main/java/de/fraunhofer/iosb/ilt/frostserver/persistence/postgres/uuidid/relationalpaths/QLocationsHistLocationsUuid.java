package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import java.sql.Types;
import java.util.UUID;

/**
 * QLocationsHistLocationsUuid is a Querydsl query type for
 * QLocationsHistLocationsUuid
 */
public class QLocationsHistLocationsUuid extends AbstractQLocationsHistLocations<QLocationsHistLocationsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 68027452;
    private static final String TABLE_NAME = "LOCATIONS_HIST_LOCATIONS";

    public static final QLocationsHistLocationsUuid LOCATIONSHISTLOCATIONS = new QLocationsHistLocationsUuid(TABLE_NAME);

    public final ComparablePath<UUID> histLocationId = createComparable("histLocationId", UUID.class);

    public final ComparablePath<UUID> locationId = createComparable("locationId", UUID.class);

    public QLocationsHistLocationsUuid(String variable) {
        super(QLocationsHistLocationsUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
