package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.uuidid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQActuators;
import java.sql.Types;
import java.util.UUID;

/**
 * QActuatorsUuid is a Querydsl query type for Actuators
 */
public class QActuatorsUuid extends AbstractQActuators<QActuatorsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = -1003317477;
    private static final String TABLE_NAME = "ACTUATORS";

    public static final QActuatorsUuid ACTUATORS = new QActuatorsUuid(TABLE_NAME);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public QActuatorsUuid(String variable) {
        super(QActuatorsUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public QActuatorsUuid newWithAlias(String variable) {
        return new QActuatorsUuid(variable);
    }

}
