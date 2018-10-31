package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThings;
import java.sql.Types;

/**
 * QThings is a Querydsl query type for QThings
 */
public class QThings extends AbstractQThings<QThings, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -180719772;

    public static final QThings things = new QThings("THINGS");

    public final NumberPath<Long> id = createNumber("id", Long.class);

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
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public QThings newWithAlias(String variable) {
        return new QThings(variable);
    }

}
