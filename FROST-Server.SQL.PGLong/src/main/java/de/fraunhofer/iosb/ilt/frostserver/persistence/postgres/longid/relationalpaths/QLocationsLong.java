package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQLocations;
import java.sql.Types;

/**
 * QLocationsLong is a Querydsl query type for QLocationsLong
 */
public class QLocationsLong extends AbstractQLocations<QLocationsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 1565350111;
    private static final String TABLE_NAME = "LOCATIONS";

    public static final QLocationsLong LOCATIONS = new QLocationsLong(TABLE_NAME);

    public final NumberPath<Long> genFoiId = createNumber("genFoiId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QLocationsLong(String variable) {
        super(QLocationsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
