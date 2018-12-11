package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQActuators;
import java.sql.Types;

/**
 * QActuatorsString is a Querydsl query type for Actuators
 */
public class QActuatorsString extends AbstractQActuators<QActuatorsString, StringPath, String> {

    private static final long serialVersionUID = -1003317477;
    private static final String TABLE_NAME = "ACTUATORS";

    public static final QActuatorsString ACTUATORS = new QActuatorsString(TABLE_NAME);

    public final StringPath id = createString("id");

    public QActuatorsString(String variable) {
        super(QActuatorsString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
    public QActuatorsString newWithAlias(String variable) {
        return new QActuatorsString(variable);
    }

}
