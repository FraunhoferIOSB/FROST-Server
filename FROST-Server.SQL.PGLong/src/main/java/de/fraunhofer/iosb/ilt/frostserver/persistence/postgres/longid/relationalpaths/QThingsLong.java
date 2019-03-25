package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQThings;
import java.sql.Types;

/**
 * QThingsLong is a Querydsl query type for QThingsLong
 */
public class QThingsLong extends AbstractQThings<QThingsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -180719772;
    private static final String TABLE_NAME = "THINGS";

    public static final QThingsLong THINGS = new QThingsLong(TABLE_NAME);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QThingsLong(String variable) {
        super(QThingsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
