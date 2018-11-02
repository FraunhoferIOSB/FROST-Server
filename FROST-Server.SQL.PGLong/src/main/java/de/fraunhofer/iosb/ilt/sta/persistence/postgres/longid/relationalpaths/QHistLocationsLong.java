package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import java.sql.Types;

/**
 * QHistLocationsLong is a Querydsl query type for QHistLocationsLong
 */
public class QHistLocationsLong extends AbstractQHistLocations<QHistLocationsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 244045661;

    public static final QHistLocationsLong HISTLOCATIONS = new QHistLocationsLong("HIST_LOCATIONS");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> thingId = createNumber("thingId", Long.class);

    public QHistLocationsLong(String variable) {
        super(QHistLocationsLong.class, forVariable(variable), "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocationsLong(String variable, String schema, String table) {
        super(QHistLocationsLong.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHistLocationsLong(String variable, String schema) {
        super(QHistLocationsLong.class, forVariable(variable), schema, "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocationsLong(Path<? extends QHistLocationsLong> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocationsLong(PathMetadata metadata) {
        super(QHistLocationsLong.class, metadata, "PUBLIC", "HIST_LOCATIONS");
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
