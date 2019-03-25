package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.stringid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQHistLocations;
import java.sql.Types;

/**
 * QHistLocationsString is a Querydsl query type for QHistLocationsString
 */
public class QHistLocationsString extends AbstractQHistLocations<QHistLocationsString, StringPath, String> {

    private static final long serialVersionUID = 2040692648;
    private static final String TABLE_NAME = "HIST_LOCATIONS";

    public static final QHistLocationsString HISTLOCATIONS = new QHistLocationsString(TABLE_NAME);

    public final StringPath id = createString("id");

    public final StringPath thingId = createString("thingId");

    public QHistLocationsString(String variable) {
        super(QHistLocationsString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
