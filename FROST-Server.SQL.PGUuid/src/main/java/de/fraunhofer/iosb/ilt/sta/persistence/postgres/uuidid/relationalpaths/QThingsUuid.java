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
 * QThingsUuid is a Querydsl query type for QThingsUuid
 */
public class QThingsUuid extends AbstractQThings<QThingsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = -1745724957;

    public static final QThingsUuid THINGS = new QThingsUuid("THINGS");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final com.querydsl.sql.PrimaryKey<QThingsUuid> thingsPkey = createPrimaryKey(id);

    public QThingsUuid(String variable) {
        super(QThingsUuid.class, forVariable(variable), "PUBLIC", "THINGS");
        addMetadata();
    }

    public QThingsUuid(String variable, String schema, String table) {
        super(QThingsUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QThingsUuid(String variable, String schema) {
        super(QThingsUuid.class, forVariable(variable), schema, "THINGS");
        addMetadata();
    }

    public QThingsUuid(Path<? extends QThingsUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "THINGS");
        addMetadata();
    }

    public QThingsUuid(PathMetadata metadata) {
        super(QThingsUuid.class, metadata, "PUBLIC", "THINGS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public QThingsUuid newWithAlias(String variable) {
        return new QThingsUuid(variable);
    }

}
