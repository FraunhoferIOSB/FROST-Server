package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQThings;
import java.sql.Types;
import java.util.UUID;

/**
 * QThingsUuid is a Querydsl query type for QThingsUuid
 */
public class QThingsUuid extends AbstractQThings<QThingsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = -1745724957;
    private static final String TABLE_NAME = "THINGS";

    public static final QThingsUuid THINGS = new QThingsUuid(TABLE_NAME);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final com.querydsl.sql.PrimaryKey<QThingsUuid> thingsPkey = createPrimaryKey(id);

    public QThingsUuid(String variable) {
        super(QThingsUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
