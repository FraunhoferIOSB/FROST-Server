package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThings;
import java.sql.Types;

/**
 * QThingsLong is a Querydsl query type for QThingsLong
 */
public class QThingsLong extends AbstractQThings<QThingsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -180719772;

    public static final QThingsLong THINGS = new QThingsLong("THINGS");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QThingsLong(String variable) {
        super(QThingsLong.class, forVariable(variable), "PUBLIC", "THINGS");
        addMetadata();
    }

    public QThingsLong(String variable, String schema, String table) {
        super(QThingsLong.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QThingsLong(String variable, String schema) {
        super(QThingsLong.class, forVariable(variable), schema, "THINGS");
        addMetadata();
    }

    public QThingsLong(Path<? extends QThingsLong> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "THINGS");
        addMetadata();
    }

    public QThingsLong(PathMetadata metadata) {
        super(QThingsLong.class, metadata, "PUBLIC", "THINGS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public QThingsLong newWithAlias(String variable) {
        return new QThingsLong(variable);
    }

}
