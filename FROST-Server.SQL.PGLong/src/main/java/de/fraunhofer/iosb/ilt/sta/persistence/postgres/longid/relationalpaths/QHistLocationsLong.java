package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import java.sql.Types;

/**
 * QHistLocationsLong is a Querydsl query type for QHistLocationsLong
 */
public class QHistLocationsLong extends AbstractQHistLocations<QHistLocationsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 244045661;
    private static final String TABLE_NAME = "HIST_LOCATIONS";

    public static final QHistLocationsLong HISTLOCATIONS = new QHistLocationsLong(TABLE_NAME);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> thingId = createNumber("thingId", Long.class);

    public QHistLocationsLong(String variable) {
        super(QHistLocationsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public NumberPath<Long> getThingId() {
        return thingId;
    }

    @Override
    public QHistLocationsLong newWithAlias(String variable) {
        return new QHistLocationsLong(variable);
    }

}
