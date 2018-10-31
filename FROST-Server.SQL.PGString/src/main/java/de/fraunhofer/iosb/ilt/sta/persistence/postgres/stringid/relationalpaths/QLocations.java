package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocations;
import java.sql.Types;

/**
 * QLocations is a Querydsl query type for QLocations
 */
public class QLocations extends AbstractQLocations<QLocations, StringPath, String> {

    private static final long serialVersionUID = 1694621354;

    public static final QLocations locations = new QLocations("LOCATIONS");

    public final StringPath genFoiId = createString("genFoiId");

    public final StringPath id = createString("id");

    public final com.querydsl.sql.PrimaryKey<QLocations> locationsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QLocationsHistLocations> _locationsHistLocationsLocationIdFkey = createInvForeignKey(id, "LOCATION_ID");

    public final com.querydsl.sql.ForeignKey<QThingsLocations> _thingsLocationsLocationIdFkey = createInvForeignKey(id, "LOCATION_ID");

    public QLocations(String variable) {
        super(QLocations.class, forVariable(variable), "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    public QLocations(String variable, String schema, String table) {
        super(QLocations.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocations(String variable, String schema) {
        super(QLocations.class, forVariable(variable), schema, "LOCATIONS");
        addMetadata();
    }

    public QLocations(Path<? extends QLocations> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    public QLocations(PathMetadata metadata) {
        super(QLocations.class, metadata, "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(genFoiId, ColumnMetadata.named("GEN_FOI_ID").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public StringPath getId() {
        return id;
    }

    @Override
    public StringPath getGenFoiId() {
        return genFoiId;
    }

    @Override
    public QLocations newWithAlias(String variable) {
        return new QLocations(variable);
    }

}
