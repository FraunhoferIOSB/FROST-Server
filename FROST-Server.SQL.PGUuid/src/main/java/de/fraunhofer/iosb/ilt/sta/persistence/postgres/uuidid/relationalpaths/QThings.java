package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThings;
import java.sql.Types;
import java.util.UUID;

/**
 * QThings is a Querydsl query type for QThings
 */
public class QThings extends AbstractQThings<QThings, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = -1745724957;

    public static final QThings things = new QThings("THINGS");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final com.querydsl.sql.PrimaryKey<QThings> thingsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QThingsLocations> _thingsLocationsThingIdFkey = createInvForeignKey(id, "THING_ID");

    public final com.querydsl.sql.ForeignKey<QHistLocations> _histLocationsThingIdFkey = createInvForeignKey(id, "THING_ID");

    public final com.querydsl.sql.ForeignKey<QDatastreams> _datastreamsThingIdFkey = createInvForeignKey(id, "THING_ID");

    public final com.querydsl.sql.ForeignKey<QMultiDatastreams> _multiDatastreamsThingIdFkey = createInvForeignKey(id, "THING_ID");

    public QThings(String variable) {
        super(QThings.class, forVariable(variable), "PUBLIC", "THINGS");
        addMetadata();
    }

    public QThings(String variable, String schema, String table) {
        super(QThings.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QThings(String variable, String schema) {
        super(QThings.class, forVariable(variable), schema, "THINGS");
        addMetadata();
    }

    public QThings(Path<? extends QThings> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "THINGS");
        addMetadata();
    }

    public QThings(PathMetadata metadata) {
        super(QThings.class, metadata, "PUBLIC", "THINGS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public QThings newWithAlias(String variable) {
        return new QThings(variable);
    }

}
