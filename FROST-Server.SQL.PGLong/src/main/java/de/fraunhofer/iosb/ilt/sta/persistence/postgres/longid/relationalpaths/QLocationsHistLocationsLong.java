package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import java.sql.Types;

/**
 * QLocationsHistLocationsLong is a Querydsl query type for
 * QLocationsHistLocationsLong
 */
public class QLocationsHistLocationsLong extends AbstractQLocationsHistLocations<QLocationsHistLocationsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 1713698749;
    private static final String TABLE_NAME = "LOCATIONS_HIST_LOCATIONS";

    public static final QLocationsHistLocationsLong LOCATIONSHISTLOCATIONS = new QLocationsHistLocationsLong(TABLE_NAME);

    public final NumberPath<Long> histLocationId = createNumber("histLocationId", Long.class);

    public final NumberPath<Long> locationId = createNumber("locationId", Long.class);

    public QLocationsHistLocationsLong(String variable) {
        super(QLocationsHistLocationsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(histLocationId, ColumnMetadata.named("HIST_LOCATION_ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(locationId, ColumnMetadata.named("LOCATION_ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    @Override
    public NumberPath<Long> getLocationId() {
        return locationId;
    }

    @Override
    public NumberPath<Long> getHistLocationId() {
        return histLocationId;
    }

    @Override
    public QLocationsHistLocationsLong newWithAlias(String variable) {
        return new QLocationsHistLocationsLong(variable);
    }

}
