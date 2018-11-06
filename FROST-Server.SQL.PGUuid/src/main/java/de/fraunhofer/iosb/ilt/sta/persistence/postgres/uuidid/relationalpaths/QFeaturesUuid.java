package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQFeatures;
import java.sql.Types;
import java.util.UUID;

/**
 * QFeaturesUuid is a Querydsl query type for QFeaturesUuid
 */
public class QFeaturesUuid extends AbstractQFeatures<QFeaturesUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 175404379;
    private static final String TABLE_NAME = "FEATURES";

    public static final QFeaturesUuid FEATURES = new QFeaturesUuid(TABLE_NAME);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public QFeaturesUuid(String variable) {
        super(QFeaturesUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public QFeaturesUuid newWithAlias(String variable) {
        return new QFeaturesUuid(variable);
    }

}
