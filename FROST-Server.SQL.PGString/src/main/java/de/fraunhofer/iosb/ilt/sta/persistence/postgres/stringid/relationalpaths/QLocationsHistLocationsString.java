package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import java.sql.Types;

/**
 * QLocationsHistLocationsString is a Querydsl query type for QLocationsHistLocationsString
 */
public class QLocationsHistLocationsString extends AbstractQLocationsHistLocations<QLocationsHistLocationsString, StringPath, String> {

    private static final long serialVersionUID = -678464558;

    public static final QLocationsHistLocationsString LOCATIONSHISTLOCATIONS = new QLocationsHistLocationsString("LOCATIONS_HIST_LOCATIONS");

    public final StringPath histLocationId = createString("histLocationId");

    public final StringPath locationId = createString("locationId");

    public QLocationsHistLocationsString(String variable) {
        super(QLocationsHistLocationsString.class, forVariable(variable), "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocationsString(String variable, String schema, String table) {
        super(QLocationsHistLocationsString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocationsHistLocationsString(String variable, String schema) {
        super(QLocationsHistLocationsString.class, forVariable(variable), schema, "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocationsString(Path<? extends QLocationsHistLocationsString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    public QLocationsHistLocationsString(PathMetadata metadata) {
        super(QLocationsHistLocationsString.class, metadata, "PUBLIC", "LOCATIONS_HIST_LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(histLocationId, ColumnMetadata.named("HIST_LOCATION_ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(locationId, ColumnMetadata.named("LOCATION_ID").ofType(Types.VARCHAR).withSize(36).notNull());
    }

    @Override
    public StringPath getLocationId() {
        return locationId;
    }

    @Override
    public StringPath getHistLocationId() {
        return histLocationId;
    }

    @Override
    public QLocationsHistLocationsString newWithAlias(String variable) {
        return new QLocationsHistLocationsString(variable);
    }

}
