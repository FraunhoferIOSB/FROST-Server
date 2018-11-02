package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
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

    public static final QMultiDatastreamsString MULTIDATASTREAMS = new QMultiDatastreamsString("MULTI_DATASTREAMS");

    public final StringPath id = createString("id");

    public final StringPath sensorId = createString("sensorId");

    public final StringPath thingId = createString("thingId");

    public QMultiDatastreamsString(String variable) {
        super(QMultiDatastreamsString.class, forVariable(variable), "PUBLIC", "MULTI_DATASTREAMS");
        addMetadata();
    }

    public QMultiDatastreamsString(String variable, String schema, String table) {
        super(QMultiDatastreamsString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMultiDatastreamsString(String variable, String schema) {
        super(QMultiDatastreamsString.class, forVariable(variable), schema, "MULTI_DATASTREAMS");
        addMetadata();
    }

    public QMultiDatastreamsString(Path<? extends QMultiDatastreamsString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "MULTI_DATASTREAMS");
        addMetadata();
    }

    public QMultiDatastreamsString(PathMetadata metadata) {
        super(QMultiDatastreamsString.class, metadata, "PUBLIC", "MULTI_DATASTREAMS");
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
