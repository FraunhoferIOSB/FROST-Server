package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQFeatures;
import java.sql.Types;

/**
 * QFeaturesString is a Querydsl query type for QFeaturesString
 */
public class QFeaturesString extends AbstractQFeatures<QFeaturesString, StringPath, String> {

    private static final long serialVersionUID = 1880834929;

    public static final QFeaturesString FEATURES = new QFeaturesString("FEATURES");

    public final StringPath id = createString("id");

    public QFeaturesString(String variable) {
        super(QFeaturesString.class, forVariable(variable), "PUBLIC", "FEATURES");
        addMetadata();
    }

    public QFeaturesString(String variable, String schema, String table) {
        super(QFeaturesString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFeaturesString(String variable, String schema) {
        super(QFeaturesString.class, forVariable(variable), schema, "FEATURES");
        addMetadata();
    }

    public QFeaturesString(Path<? extends QFeaturesString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "FEATURES");
        addMetadata();
    }

    public QFeaturesString(PathMetadata metadata) {
        super(QFeaturesString.class, metadata, "PUBLIC", "FEATURES");
        addMetadata();
    }

    private void addMetadata() {
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
    public QFeaturesString newWithAlias(String variable) {
        return new QFeaturesString(variable);
    }

}
