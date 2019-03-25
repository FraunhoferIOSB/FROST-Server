package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQThingsLocations;
import java.sql.Types;

/**
 * QThingsLocationsLong is a Querydsl query type for QThingsLocationsLong
 */
public class QThingsLocationsLong extends AbstractQThingsLocations<QThingsLocationsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -1059514278;
    private static final String TABLE_NAME = "THINGS_LOCATIONS";

    public static final QThingsLocationsLong THINGSLOCATIONS = new QThingsLocationsLong(TABLE_NAME);

    public final NumberPath<Long> locationId = createNumber("locationId", Long.class);

    public final NumberPath<Long> thingId = createNumber("thingId", Long.class);

    public QThingsLocationsLong(String variable) {
        super(QThingsLocationsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(locationId, ColumnMetadata.named("LOCATION_ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    @Override
    public NumberPath<Long> getLocationId() {
        return locationId;
    }

    @Override
    public NumberPath<Long> getThingId() {
        return thingId;
    }

    @Override
    public QThingsLocationsLong newWithAlias(String variable) {
        return new QThingsLocationsLong(variable);
    }

}
