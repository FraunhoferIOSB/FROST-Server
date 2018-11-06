package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

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
    private static final String TABLE_NAME = "FEATURES";

    public static final QFeaturesString FEATURES = new QFeaturesString(TABLE_NAME);

    public final StringPath id = createString("id");

    public QFeaturesString(String variable) {
        super(QFeaturesString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
