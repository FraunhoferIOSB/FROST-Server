package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocations;
import java.sql.Types;

/**
 * QLocationsString is a Querydsl query type for QLocationsString
 */
public class QLocationsString extends AbstractQLocations<QLocationsString, StringPath, String> {

    private static final long serialVersionUID = 1694621354;

    public static final QLocationsString LOCATIONS = new QLocationsString("LOCATIONS");

    public final StringPath genFoiId = createString("genFoiId");

    public final StringPath id = createString("id");

    public QLocationsString(String variable) {
        super(QLocationsString.class, forVariable(variable), "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    public QLocationsString(String variable, String schema, String table) {
        super(QLocationsString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocationsString(String variable, String schema) {
        super(QLocationsString.class, forVariable(variable), schema, "LOCATIONS");
        addMetadata();
    }

    public QLocationsString(Path<? extends QLocationsString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    public QLocationsString(PathMetadata metadata) {
        super(QLocationsString.class, metadata, "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(genFoiId, ColumnMetadata.named("GEN_FOI_ID").ofType(Types.VARCHAR).withSize(36));
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.VARCHAR).withSize(36).notNull());
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
    public QLocationsString newWithAlias(String variable) {
        return new QLocationsString(variable);
    }

}
