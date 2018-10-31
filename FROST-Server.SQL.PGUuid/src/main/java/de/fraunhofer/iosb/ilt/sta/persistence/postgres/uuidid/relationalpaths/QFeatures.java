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
 * QFeatures is a Querydsl query type for QFeatures
 */
public class QFeatures extends AbstractQFeatures<QFeatures, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 175404379;

    public static final QFeatures features = new QFeatures("FEATURES");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final com.querydsl.sql.PrimaryKey<QFeatures> featuresPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QObservations> _observationsFeatureIdFkey = createInvForeignKey(id, "FEATURE_ID");

    public QFeatures(String variable) {
        super(QFeatures.class, forVariable(variable), "PUBLIC", "FEATURES");
        addMetadata();
    }

    public QFeatures(String variable, String schema, String table) {
        super(QFeatures.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFeatures(String variable, String schema) {
        super(QFeatures.class, forVariable(variable), schema, "FEATURES");
        addMetadata();
    }

    public QFeatures(Path<? extends QFeatures> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "FEATURES");
        addMetadata();
    }

    public QFeatures(PathMetadata metadata) {
        super(QFeatures.class, metadata, "PUBLIC", "FEATURES");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public QFeatures newWithAlias(String variable) {
        return new QFeatures(variable);
    }

}
