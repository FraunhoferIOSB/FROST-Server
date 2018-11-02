package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import java.sql.Types;

/**
 * QLocationsHistLocationsLong is a Querydsl query type for QLocationsHistLocationsLong
 */
public class QLocationsHistLocationsLong extends AbstractQLocationsHistLocations<QLocationsHistLocationsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 1713698749;

    public static final QLocationsHistLocationsLong LOCATIONSHISTLOCATIONS = new QLocationsHistLocationsLong("LOCATIONS_HIST_LOCATIONS");

    public final NumberPath<Long> histLocationId = createNumber("histLocationId", Long.class);

    public final NumberPath<Long> locationId = createNumber("locationId", Long.class);

    public QLocationsHistLocationsLong(String variable) {
        super(QLocationsHistLocationsLong.class, forVariable(variable), "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocationsLong(String variable, String schema, String table) {
        super(QLocationsHistLocationsLong.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocationsHistLocationsLong(String variable, String schema) {
        super(QLocationsHistLocationsLong.class, forVariable(variable), schema, "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocationsLong(Path<? extends QLocationsHistLocationsLong> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocationsLong(PathMetadata metadata) {
        super(QLocationsHistLocationsLong.class, metadata, "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
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
