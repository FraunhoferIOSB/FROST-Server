package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreamsObsProperties;
import java.sql.Types;

/**
 * QMultiDatastreamsObsPropertiesLong is a Querydsl query type for
 * QMultiDatastreamsObsPropertiesLong
 */
public class QMultiDatastreamsObsPropertiesLong extends AbstractQMultiDatastreamsObsProperties<QMultiDatastreamsObsPropertiesLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -838888412;
    private static final String TABLE_NAME = "MULTI_DATASTREAMS_OBS_PROPERTIES";

    public static final QMultiDatastreamsObsPropertiesLong MULTIDATASTREAMSOBSPROPERTIES = new QMultiDatastreamsObsPropertiesLong(TABLE_NAME);

    public final NumberPath<Long> multiDatastreamId = createNumber("multiDatastreamId", Long.class);

    public final NumberPath<Long> obsPropertyId = createNumber("obsPropertyId", Long.class);

    public QMultiDatastreamsObsPropertiesLong(String variable) {
        super(QMultiDatastreamsObsPropertiesLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(multiDatastreamId, ColumnMetadata.named("MULTI_DATASTREAM_ID").ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(obsPropertyId, ColumnMetadata.named("OBS_PROPERTY_ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    @Override
    public NumberPath<Long> getMultiDatastreamId() {
        return multiDatastreamId;
    }

    @Override
    public NumberPath<Long> getObsPropertyId() {
        return obsPropertyId;
    }

    @Override
    public QMultiDatastreamsObsPropertiesLong newWithAlias(String variable) {
        return new QMultiDatastreamsObsPropertiesLong(variable);
    }

}
