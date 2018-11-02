package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObsProperties;
import java.sql.Types;
import java.util.UUID;

/**
 * QObsPropertiesUuid is a Querydsl query type for QObsPropertiesUuid
 */
public class QObsPropertiesUuid extends AbstractQObsProperties<QObsPropertiesUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 1235830773;

    public static final QObsPropertiesUuid OBSPROPERTIES = new QObsPropertiesUuid("OBS_PROPERTIES");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public QObsPropertiesUuid(String variable) {
        super(QObsPropertiesUuid.class, forVariable(variable), "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsPropertiesUuid(String variable, String schema, String table) {
        super(QObsPropertiesUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QObsPropertiesUuid(String variable, String schema) {
        super(QObsPropertiesUuid.class, forVariable(variable), schema, "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsPropertiesUuid(Path<? extends QObsPropertiesUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsPropertiesUuid(PathMetadata metadata) {
        super(QObsPropertiesUuid.class, metadata, "PUBLIC", "OBS_PROPERTIES");
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
    public QObsPropertiesUuid newWithAlias(String variable) {
        return new QObsPropertiesUuid(variable);
    }

}
