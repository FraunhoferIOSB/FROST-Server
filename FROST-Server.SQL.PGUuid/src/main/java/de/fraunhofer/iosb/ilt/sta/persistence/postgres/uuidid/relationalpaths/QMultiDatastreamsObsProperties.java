package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreamsObsProperties;
import java.sql.Types;
import java.util.UUID;

/**
 * QMultiDatastreamsObsProperties is a Querydsl query type for
 * QMultiDatastreamsObsProperties
 */
public class QMultiDatastreamsObsProperties extends AbstractQMultiDatastreamsObsProperties<QMultiDatastreamsObsProperties, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 2126924485;

    public static final QMultiDatastreamsObsProperties multiDatastreamsObsProperties = new QMultiDatastreamsObsProperties("MULTI_DATASTREAMS_OBS_PROPERTIES");

    public final ComparablePath<UUID> multiDatastreamId = createComparable("multiDatastreamId", UUID.class);

    public final ComparablePath<UUID> obsPropertyId = createComparable("obsPropertyId", UUID.class);

    public final com.querydsl.sql.PrimaryKey<QMultiDatastreamsObsProperties> multiDatastreamsObsPropertiesPkey = createPrimaryKey(multiDatastreamId, obsPropertyId, rank);

    public final com.querydsl.sql.ForeignKey<QMultiDatastreams> mdopMultiDatastreamIdFkey = createForeignKey(multiDatastreamId, "ID");

    public final com.querydsl.sql.ForeignKey<QObsProperties> mdopObsPropertyIdFkey = createForeignKey(obsPropertyId, "ID");

    public QMultiDatastreamsObsProperties(String variable) {
        super(QMultiDatastreamsObsProperties.class, forVariable(variable), "PUBLIC", "MULTI_DATASTREAMS_OBS_PROPERTIES");
        addMetadata();
    }

    public QMultiDatastreamsObsProperties(String variable, String schema, String table) {
        super(QMultiDatastreamsObsProperties.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMultiDatastreamsObsProperties(String variable, String schema) {
        super(QMultiDatastreamsObsProperties.class, forVariable(variable), schema, "MULTI_DATASTREAMS_OBS_PROPERTIES");
        addMetadata();
    }

    public QMultiDatastreamsObsProperties(Path<? extends QMultiDatastreamsObsProperties> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "MULTI_DATASTREAMS_OBS_PROPERTIES");
        addMetadata();
    }

    public QMultiDatastreamsObsProperties(PathMetadata metadata) {
        super(QMultiDatastreamsObsProperties.class, metadata, "PUBLIC", "MULTI_DATASTREAMS_OBS_PROPERTIES");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(multiDatastreamId, ColumnMetadata.named("MULTI_DATASTREAM_ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(obsPropertyId, ColumnMetadata.named("OBS_PROPERTY_ID").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

    @Override
    public ComparablePath<UUID> getMultiDatastreamId() {
        return multiDatastreamId;
    }

    @Override
    public ComparablePath<UUID> getObsPropertyId() {
        return obsPropertyId;
    }

    @Override
    public QMultiDatastreamsObsProperties newWithAlias(String variable) {
        return new QMultiDatastreamsObsProperties(variable);
    }

}
