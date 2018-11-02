package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThingsLocations;
import java.sql.Types;

/**
 * QThingsLocationsString is a Querydsl query type for QThingsLocationsString
 */
public class QThingsLocationsString extends AbstractQThingsLocations<QThingsLocationsString, StringPath, String> {

    private static final long serialVersionUID = -1058612763;

    public static final QThingsLocationsString THINGSLOCATIONS = new QThingsLocationsString("THINGS_LOCATIONS");

    public final StringPath locationId = createString("locationId");

    public final StringPath thingId = createString("thingId");

    public QThingsLocationsString(String variable) {
        super(QThingsLocationsString.class, forVariable(variable), "PUBLIC", "THINGS_LOCATIONS");
        addMetadata();
    }

    public QThingsLocationsString(String variable, String schema, String table) {
        super(QThingsLocationsString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QThingsLocationsString(String variable, String schema) {
        super(QThingsLocationsString.class, forVariable(variable), schema, "THINGS_LOCATIONS");
        addMetadata();
    }

    public QThingsLocationsString(Path<? extends QThingsLocationsString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "THINGS_LOCATIONS");
        addMetadata();
    }

    public QThingsLocationsString(PathMetadata metadata) {
        super(QThingsLocationsString.class, metadata, "PUBLIC", "THINGS_LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(locationId, ColumnMetadata.named("LOCATION_ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.VARCHAR).withSize(36).notNull());
    }

    @Override
    public StringPath getLocationId() {
        return locationId;
    }

    @Override
    public StringPath getThingId() {
        return thingId;
    }

    @Override
    public QThingsLocationsString newWithAlias(String variable) {
        return new QThingsLocationsString(variable);
    }

}
