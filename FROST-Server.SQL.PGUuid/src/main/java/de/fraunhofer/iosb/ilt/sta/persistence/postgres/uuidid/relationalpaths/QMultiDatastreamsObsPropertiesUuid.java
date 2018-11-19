package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreamsObsProperties;
import java.sql.Types;
import java.util.UUID;

/**
 * QMultiDatastreamsObsPropertiesUuid is a Querydsl query type for
 * QMultiDatastreamsObsPropertiesUuid
 */
public class QMultiDatastreamsObsPropertiesUuid extends AbstractQMultiDatastreamsObsProperties<QMultiDatastreamsObsPropertiesUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 2126924485;
    private static final String TABLE_NAME = "MULTI_DATASTREAMS_OBS_PROPERTIES";

    public static final QMultiDatastreamsObsPropertiesUuid MULTIDATASTREAMSOBSPROPERTIES = new QMultiDatastreamsObsPropertiesUuid(TABLE_NAME);

    public final ComparablePath<UUID> multiDatastreamId = createComparable("multiDatastreamId", UUID.class);

    public final ComparablePath<UUID> obsPropertyId = createComparable("obsPropertyId", UUID.class);

    public QMultiDatastreamsObsPropertiesUuid(String variable) {
        super(QMultiDatastreamsObsPropertiesUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(multiDatastreamId, ColumnMetadata.named("MULTI_DATASTREAM_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
        addMetadata(obsPropertyId, ColumnMetadata.named("OBS_PROPERTY_ID").ofType(Types.BINARY).withSize(2147483647).notNull());
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
    public QMultiDatastreamsObsPropertiesUuid newWithAlias(String variable) {
        return new QMultiDatastreamsObsPropertiesUuid(variable);
    }

}
