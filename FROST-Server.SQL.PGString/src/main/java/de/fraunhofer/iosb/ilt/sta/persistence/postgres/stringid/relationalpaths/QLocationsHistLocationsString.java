package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import java.sql.Types;

/**
 * QLocationsHistLocationsString is a Querydsl query type for
 * QLocationsHistLocationsString
 */
public class QLocationsHistLocationsString extends AbstractQLocationsHistLocations<QLocationsHistLocationsString, StringPath, String> {

    private static final long serialVersionUID = -678464558;
    private static final String TABLE_NAME = "LOCATIONS_HIST_LOCATIONS";

    public static final QLocationsHistLocationsString LOCATIONSHISTLOCATIONS = new QLocationsHistLocationsString(TABLE_NAME);

    public final StringPath histLocationId = createString("histLocationId");

    public final StringPath locationId = createString("locationId");

    public QLocationsHistLocationsString(String variable) {
        super(QLocationsHistLocationsString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
