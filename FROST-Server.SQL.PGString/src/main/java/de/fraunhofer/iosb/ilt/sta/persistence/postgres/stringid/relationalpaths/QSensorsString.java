package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import java.sql.Types;

/**
 * QSensorsString is a Querydsl query type for QSensorsString
 */
public class QSensorsString extends AbstractQSensors<QSensorsString, StringPath, String> {

    private static final long serialVersionUID = -2105995707;

    public static final QSensorsString SENSORS = new QSensorsString("SENSORS");

    public final StringPath id = createString("id");

    public QSensorsString(String variable) {
        super(QSensorsString.class, forVariable(variable), "PUBLIC", "SENSORS");
        addMetadata();
    }

    public QSensorsString(String variable, String schema, String table) {
        super(QSensorsString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSensorsString(String variable, String schema) {
        super(QSensorsString.class, forVariable(variable), schema, "SENSORS");
        addMetadata();
    }

    public QSensorsString(Path<? extends QSensorsString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "SENSORS");
        addMetadata();
    }

    public QSensorsString(PathMetadata metadata) {
        super(QSensorsString.class, metadata, "PUBLIC", "SENSORS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.VARCHAR).withSize(36).notNull());
    }

    @Override
    public StringPath getId() {
        return id;
    }

    @Override
    public QSensorsString newWithAlias(String variable) {
        return new QSensorsString(variable);
    }

}
