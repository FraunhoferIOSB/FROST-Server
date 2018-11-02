package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocations;
import java.sql.Types;

/**
 * QLocationsLong is a Querydsl query type for QLocationsLong
 */
public class QLocationsLong extends AbstractQLocations<QLocationsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 1565350111;

    public static final QLocationsLong LOCATIONS = new QLocationsLong("LOCATIONS");

    public final NumberPath<Long> genFoiId = createNumber("genFoiId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QLocationsLong(String variable) {
        super(QLocationsLong.class, forVariable(variable), "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    public QLocationsLong(String variable, String schema, String table) {
        super(QLocationsLong.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocationsLong(String variable, String schema) {
        super(QLocationsLong.class, forVariable(variable), schema, "LOCATIONS");
        addMetadata();
    }

    public QLocationsLong(Path<? extends QLocationsLong> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    public QLocationsLong(PathMetadata metadata) {
        super(QLocationsLong.class, metadata, "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(genFoiId, ColumnMetadata.named("GEN_FOI_ID").ofType(Types.BIGINT).withSize(19));
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
    public NumberPath<Long> getGenFoiId() {
        return genFoiId;
    }

    @Override
    public QLocationsLong newWithAlias(String variable) {
        return new QLocationsLong(variable);
    }

}
