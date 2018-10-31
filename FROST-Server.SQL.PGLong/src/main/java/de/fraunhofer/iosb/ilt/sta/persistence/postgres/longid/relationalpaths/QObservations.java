package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import java.sql.Types;

/**
 * QObservations is a Querydsl query type for QObservations
 */
public class QObservations extends AbstractQObservations<QObservations, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -1854525274;

    public static final QObservations observations = new QObservations("OBSERVATIONS");

    public final NumberPath<Long> datastreamId = createNumber("datastreamId", Long.class);

    public final NumberPath<Long> featureId = createNumber("featureId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> multiDatastreamId = createNumber("multiDatastreamId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QObservations> observationsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFeatures> observationsFeatureIdFkey = createForeignKey(featureId, "ID");

    public final com.querydsl.sql.ForeignKey<QMultiDatastreams> observationsMultiDatastreamIdFkey = createForeignKey(multiDatastreamId, "ID");

    public final com.querydsl.sql.ForeignKey<QDatastreams> observationsDatastreamIdFkey = createForeignKey(datastreamId, "ID");

    public QObservations(String variable) {
        super(QObservations.class, forVariable(variable), "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    public QObservations(String variable, String schema, String table) {
        super(QObservations.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QObservations(String variable, String schema) {
        super(QObservations.class, forVariable(variable), schema, "OBSERVATIONS");
        addMetadata();
    }

    public QObservations(Path<? extends QObservations> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    public QObservations(PathMetadata metadata) {
        super(QObservations.class, metadata, "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(datastreamId, ColumnMetadata.named("DATASTREAM_ID").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(featureId, ColumnMetadata.named("FEATURE_ID").withIndex(12).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(multiDatastreamId, ColumnMetadata.named("MULTI_DATASTREAM_ID").withIndex(16).ofType(Types.BIGINT).withSize(19));
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
    public QObservations newWithAlias(String variable) {
        return new QObservations(variable);
    }

}
