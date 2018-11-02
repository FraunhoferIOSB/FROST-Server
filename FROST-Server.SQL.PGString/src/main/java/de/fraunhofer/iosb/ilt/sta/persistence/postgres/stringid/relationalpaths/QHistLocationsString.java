package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import java.sql.Types;

/**
 * QHistLocationsString is a Querydsl query type for QHistLocationsString
 */
public class QHistLocationsString extends AbstractQHistLocations<QHistLocationsString, StringPath, String> {

    private static final long serialVersionUID = 2040692648;

    public static final QHistLocationsString HISTLOCATIONS = new QHistLocationsString("HIST_LOCATIONS");

    public final StringPath id = createString("id");

    public final StringPath thingId = createString("thingId");

    public QHistLocationsString(String variable) {
        super(QHistLocationsString.class, forVariable(variable), "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocationsString(String variable, String schema, String table) {
        super(QHistLocationsString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHistLocationsString(String variable, String schema) {
        super(QHistLocationsString.class, forVariable(variable), schema, "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocationsString(Path<? extends QHistLocationsString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    public QHistLocationsString(PathMetadata metadata) {
        super(QHistLocationsString.class, metadata, "PUBLIC", "HIST_LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.VARCHAR).withSize(36).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public StringPath getId() {
        return id;
    }

    @Override
    public StringPath getThingId() {
        return thingId;
    }

    @Override
    public QHistLocationsString newWithAlias(String variable) {
        return new QHistLocationsString(variable);
    }

}
