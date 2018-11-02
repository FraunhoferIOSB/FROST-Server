package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import java.sql.Types;

/**
 * QDatastreamsString is a Querydsl query type for QDatastreamsString
 */
public class QDatastreamsString extends AbstractQDatastreams<QDatastreamsString, StringPath, String> {

    private static final long serialVersionUID = -546602411;

    public static final QDatastreamsString DATASTREAMS = new QDatastreamsString("DATASTREAMS");

    public final StringPath id = createString("id");

    public final StringPath obsPropertyId = createString("obsPropertyId");

    public final StringPath sensorId = createString("sensorId");

    public final StringPath thingId = createString("thingId");

    public QDatastreamsString(String variable) {
        super(QDatastreamsString.class, forVariable(variable), "PUBLIC", "DATASTREAMS");
        addMetadata();
    }

    public QDatastreamsString(String variable, String schema, String table) {
        super(QDatastreamsString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDatastreamsString(String variable, String schema) {
        super(QDatastreamsString.class, forVariable(variable), schema, "DATASTREAMS");
        addMetadata();
    }

    public QDatastreamsString(Path<? extends QDatastreamsString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "DATASTREAMS");
        addMetadata();
    }

    public QDatastreamsString(PathMetadata metadata) {
        super(QDatastreamsString.class, metadata, "PUBLIC", "DATASTREAMS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.VARCHAR).withSize(36).notNull());
        addMetadata(obsPropertyId, ColumnMetadata.named("OBS_PROPERTY_ID").ofType(Types.VARCHAR).withSize(36).notNull());
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

    /**
     * @return the obsPropertyId
     */
    @Override
    public StringPath getObsPropertyId() {
        return obsPropertyId;
    }

    /**
     * @return the sensorId
     */
    @Override
    public StringPath getSensorId() {
        return sensorId;
    }

    /**
     * @return the thingId
     */
    @Override
    public StringPath getThingId() {
        return thingId;
    }

    @Override
    public QDatastreamsString newWithAlias(String variable) {
        return new QDatastreamsString(variable);
    }

}
