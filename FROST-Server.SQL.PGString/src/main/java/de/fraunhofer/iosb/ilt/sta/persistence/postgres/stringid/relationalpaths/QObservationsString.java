package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import java.sql.Types;

/**
 * QObservationsString is a Querydsl query type for QObservationsString
 */
public class QObservationsString extends AbstractQObservations<QObservationsString, StringPath, String> {

    private static final long serialVersionUID = 974377723;

    public static final QObservationsString OBSERVATIONS = new QObservationsString("OBSERVATIONS");

    public final StringPath datastreamId = createString("datastreamId");

    public final StringPath featureId = createString("featureId");

    public final StringPath id = createString("id");

    public final StringPath multiDatastreamId = createString("multiDatastreamId");

    public QObservationsString(String variable) {
        super(QObservationsString.class, forVariable(variable), "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    public QObservationsString(String variable, String schema, String table) {
        super(QObservationsString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QObservationsString(String variable, String schema) {
        super(QObservationsString.class, forVariable(variable), schema, "OBSERVATIONS");
        addMetadata();
    }

    public QObservationsString(Path<? extends QObservationsString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    public QObservationsString(PathMetadata metadata) {
        super(QObservationsString.class, metadata, "PUBLIC", "OBSERVATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(datastreamId, ColumnMetadata.named("DATASTREAM_ID").ofType(Types.VARCHAR).withSize(36));
        addMetadata(featureId, ColumnMetadata.named("FEATURE_ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(multiDatastreamId, ColumnMetadata.named("MULTI_DATASTREAM_ID").ofType(Types.VARCHAR).withSize(36));
    }

    @Override
    public StringPath getId() {
        return id;
    }

    @Override
    public StringPath getDatastreamId() {
        return datastreamId;
    }

    @Override
    public StringPath getFeatureId() {
        return featureId;
    }

    @Override
    public StringPath getMultiDatastreamId() {
        return multiDatastreamId;
    }

    @Override
    public QObservationsString newWithAlias(String variable) {
        return new QObservationsString(variable);
    }

}
