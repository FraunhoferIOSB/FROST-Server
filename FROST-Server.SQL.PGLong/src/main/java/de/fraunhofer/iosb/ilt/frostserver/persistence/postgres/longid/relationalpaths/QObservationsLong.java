package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQObservations;
import java.sql.Types;

/**
 * QObservationsLong is a Querydsl query type for Observations
 */
public class QObservationsLong extends AbstractQObservations<QObservationsLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -1854525274;
    private static final String TABLE_NAME = "OBSERVATIONS";

    public static final QObservationsLong OBSERVATIONS = new QObservationsLong(TABLE_NAME);

    public final NumberPath<Long> datastreamId = createNumber("datastreamId", Long.class);

    public final NumberPath<Long> featureId = createNumber("featureId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> multiDatastreamId = createNumber("multiDatastreamId", Long.class);

    public QObservationsLong(String variable) {
        super(QObservationsLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(datastreamId, ColumnMetadata.named("DATASTREAM_ID").ofType(Types.BIGINT).withSize(19));
        addMetadata(featureId, ColumnMetadata.named("FEATURE_ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(multiDatastreamId, ColumnMetadata.named("MULTI_DATASTREAM_ID").ofType(Types.BIGINT).withSize(19));
    }

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
