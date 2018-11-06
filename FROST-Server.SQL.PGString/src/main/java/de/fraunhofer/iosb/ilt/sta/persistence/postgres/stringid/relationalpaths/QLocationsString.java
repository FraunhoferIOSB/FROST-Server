package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

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
    private static final String TABLE_NAME = "LOCATIONS";

    public static final QLocationsString LOCATIONS = new QLocationsString(TABLE_NAME);

    public final StringPath genFoiId = createString("genFoiId");

    public final StringPath id = createString("id");

    public QLocationsString(String variable) {
        super(QLocationsString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
