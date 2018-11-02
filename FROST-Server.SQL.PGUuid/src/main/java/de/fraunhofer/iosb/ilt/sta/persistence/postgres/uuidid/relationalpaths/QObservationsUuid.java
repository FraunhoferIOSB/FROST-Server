package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import java.sql.Types;
import java.util.UUID;

/**
 * QObservationsUuid is a Querydsl query type for QObservationsUuid
 */
public class QObservationsUuid extends AbstractQObservations<QObservationsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = -1085407259;

    public static final QObservationsUuid OBSERVATIONS = new QObservationsUuid("OBSERVATIONS");

    public final ComparablePath<UUID> datastreamId = createComparable("datastreamId", UUID.class);

    public final ComparablePath<UUID> featureId = createComparable("featureId", UUID.class);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final ComparablePath<UUID> multiDatastreamId = createComparable("multiDatastreamId", UUID.class);

    public QObservationsUuid(String variable) {
        super(QObservationsUuid.class, forVariable(variable), "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    public QObservationsUuid(String variable, String schema, String table) {
        super(QObservationsUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QObservationsUuid(String variable, String schema) {
        super(QObservationsUuid.class, forVariable(variable), schema, "OBSERVATIONS");
        addMetadata();
    }

    public QObservationsUuid(Path<? extends QObservationsUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    public QObservationsUuid(PathMetadata metadata) {
        super(QObservationsUuid.class, metadata, "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(datastreamId, ColumnMetadata.named("DATASTREAM_ID").ofType(Types.BINARY).withSize(2147483647));
        addMetadata(featureId, ColumnMetadata.named("FEATURE_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(multiDatastreamId, ColumnMetadata.named("MULTI_DATASTREAM_ID").ofType(Types.BINARY).withSize(2147483647));
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public ComparablePath<UUID> getDatastreamId() {
        return datastreamId;
    }

    @Override
    public ComparablePath<UUID> getFeatureId() {
        return featureId;
    }

    @Override
    public ComparablePath<UUID> getMultiDatastreamId() {
        return multiDatastreamId;
    }

    @Override
    public QObservationsUuid newWithAlias(String variable) {
        return new QObservationsUuid(variable);
    }

}
