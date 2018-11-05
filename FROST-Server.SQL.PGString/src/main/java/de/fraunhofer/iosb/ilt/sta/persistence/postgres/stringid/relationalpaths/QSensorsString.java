package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import java.sql.Types;

/**
 * QSensorsString is a Querydsl query type for QSensorsString
 */
public class QSensorsString extends AbstractQSensors<QSensorsString, StringPath, String> {

    private static final long serialVersionUID = -2105995707;
    private static final String TABLE_NAME = "SENSORS";

    public static final QSensorsString SENSORS = new QSensorsString(TABLE_NAME);

    public final StringPath id = createString("id");

    public QSensorsString(String variable) {
        super(QSensorsString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
