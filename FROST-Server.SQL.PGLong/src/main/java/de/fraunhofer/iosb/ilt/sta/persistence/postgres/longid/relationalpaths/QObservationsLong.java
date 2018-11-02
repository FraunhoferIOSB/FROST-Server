package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import java.sql.Types;

/**
 * QObservationsLong is a Querydsl query type for QObservationsLong
 */
public class QObservationsLong extends AbstractQObservations<QObservationsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -1854525274;

    public static final QObservationsLong OBSERVATIONS = new QObservationsLong("OBSERVATIONS");

    public final NumberPath<Long> datastreamId = createNumber("datastreamId", Long.class);

    public final NumberPath<Long> featureId = createNumber("featureId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> multiDatastreamId = createNumber("multiDatastreamId", Long.class);

    public QObservationsLong(String variable) {
        super(QObservationsLong.class, forVariable(variable), "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    public QObservationsLong(String variable, String schema, String table) {
        super(QObservationsLong.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QObservationsLong(String variable, String schema) {
        super(QObservationsLong.class, forVariable(variable), schema, "OBSERVATIONS");
        addMetadata();
    }

    public QObservationsLong(Path<? extends QObservationsLong> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    public QObservationsLong(PathMetadata metadata) {
        super(QObservationsLong.class, metadata, "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(datastreamId, ColumnMetadata.named("DATASTREAM_ID").ofType(Types.BIGINT).withSize(19));
        addMetadata(featureId, ColumnMetadata.named("FEATURE_ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(multiDatastreamId, ColumnMetadata.named("MULTI_DATASTREAM_ID").ofType(Types.BIGINT).withSize(19));
    }

    /**
     * @return the id
     */
    @Override
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public NumberPath<Long> getDatastreamId() {
        return datastreamId;
    }

    @Override
    public NumberPath<Long> getFeatureId() {
        return featureId;
    }

    @Override
    public NumberPath<Long> getMultiDatastreamId() {
        return multiDatastreamId;
    }

    @Override
    public QObservationsLong newWithAlias(String variable) {
        return new QObservationsLong(variable);
    }

}
