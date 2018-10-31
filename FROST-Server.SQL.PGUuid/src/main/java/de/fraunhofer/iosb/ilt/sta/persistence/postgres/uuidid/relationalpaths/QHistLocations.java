package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import java.sql.Types;
import java.util.UUID;

/**
 * QHistLocations is a Querydsl query type for QHistLocations
 */
public class QHistLocations extends AbstractQHistLocations<QHistLocations, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = -1683099650;

    public static final QHistLocations histLocations = new QHistLocations("HIST_LOCATIONS");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final ComparablePath<UUID> thingId = createComparable("thingId", UUID.class);

    public final com.querydsl.sql.PrimaryKey<QHistLocations> histLocationsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QThings> histLocationsThingIdFkey = createForeignKey(thingId, "ID");

    public final com.querydsl.sql.ForeignKey<QLocationsHistLocations> _locationsHistLocationsHistLocationIdFkey = createInvForeignKey(id, "HIST_LOCATION_ID");

    public QHistLocations(String variable) {
        super(QHistLocations.class, forVariable(variable), "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocations(String variable, String schema, String table) {
        super(QHistLocations.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHistLocations(String variable, String schema) {
        super(QHistLocations.class, forVariable(variable), schema, "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocations(Path<? extends QHistLocations> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocations(PathMetadata metadata) {
        super(QHistLocations.class, metadata, "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public ComparablePath<UUID> getThingId() {
        return thingId;
    }

    @Override
    public QHistLocations newWithAlias(String variable) {
        return new QHistLocations(variable);
    }

}
