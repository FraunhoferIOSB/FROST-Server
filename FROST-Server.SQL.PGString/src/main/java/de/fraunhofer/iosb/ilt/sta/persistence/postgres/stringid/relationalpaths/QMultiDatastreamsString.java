package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import java.sql.Types;

/**
 * QMultiDatastreamsString is a Querydsl query type for QMultiDatastreamsString
 */
public class QMultiDatastreamsString extends AbstractQMultiDatastreams<QMultiDatastreamsString, StringPath, String> {

    private static final long serialVersionUID = -1888350652;
    private static final String TABLE_NAME = "MULTI_DATASTREAMS";

    public static final QMultiDatastreamsString MULTIDATASTREAMS = new QMultiDatastreamsString(TABLE_NAME);

    public final StringPath id = createString("id");

    public final StringPath sensorId = createString("sensorId");

    public final StringPath thingId = createString("thingId");

    public QMultiDatastreamsString(String variable) {
        super(QMultiDatastreamsString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(sensorId, ColumnMetadata.named("SENSOR_ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(thingId, ColumnMetadata.named("THING_ID").ofType(Types.VARCHAR).withSize(36).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public StringPath getId() {
        return id;
    }

    @Override
    public StringPath getThingId() {
        return thingId;
    }

    @Override
    public StringPath getSensorId() {
        return sensorId;
    }

    @Override
    public QMultiDatastreamsString newWithAlias(String variable) {
        return new QMultiDatastreamsString(variable);
    }

}
