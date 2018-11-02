package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
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

    public static final QFeaturesUuid FEATURES = new QFeaturesUuid("FEATURES");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public QFeaturesUuid(String variable) {
        super(QFeaturesUuid.class, forVariable(variable), "PUBLIC", "FEATURES");
        addMetadata();
    }

    public QFeaturesUuid(String variable, String schema, String table) {
        super(QFeaturesUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFeaturesUuid(String variable, String schema) {
        super(QFeaturesUuid.class, forVariable(variable), schema, "FEATURES");
        addMetadata();
    }

    public QFeaturesUuid(Path<? extends QFeaturesUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "FEATURES");
        addMetadata();
    }

    public QFeaturesUuid(PathMetadata metadata) {
        super(QFeaturesUuid.class, metadata, "PUBLIC", "FEATURES");
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
