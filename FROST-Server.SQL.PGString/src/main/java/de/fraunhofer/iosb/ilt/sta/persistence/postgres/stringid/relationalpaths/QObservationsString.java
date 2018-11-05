package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

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
    private static final String TABLE_NAME = "OBSERVATIONS";

    public static final QObservationsString OBSERVATIONS = new QObservationsString(TABLE_NAME);

    public final StringPath datastreamId = createString("datastreamId");

    public final StringPath featureId = createString("featureId");

    public final StringPath id = createString("id");

    public final StringPath multiDatastreamId = createString("multiDatastreamId");

    public QObservationsString(String variable) {
        super(QObservationsString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
