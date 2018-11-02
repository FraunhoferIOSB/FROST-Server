package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQFeatures;
import java.sql.Types;

/**
 * QFeaturesLong is a Querydsl query type for QFeaturesLong
 */
public class QFeaturesLong extends AbstractQFeatures<QFeaturesLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = 906833564;

    public static final QFeaturesLong FEATURES = new QFeaturesLong("FEATURES");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QFeaturesLong(String variable) {
        super(QFeaturesLong.class, forVariable(variable), "PUBLIC", "FEATURES");
        addMetadata();
    }

    public QFeaturesLong(String variable, String schema, String table) {
        super(QFeaturesLong.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFeaturesLong(String variable, String schema) {
        super(QFeaturesLong.class, forVariable(variable), schema, "FEATURES");
        addMetadata();
    }

    public QFeaturesLong(Path<? extends QFeaturesLong> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "FEATURES");
        addMetadata();
    }

    public QFeaturesLong(PathMetadata metadata) {
        super(QFeaturesLong.class, metadata, "PUBLIC", "FEATURES");
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
    public QFeaturesLong newWithAlias(String variable) {
        return new QFeaturesLong(variable);
    }

}
