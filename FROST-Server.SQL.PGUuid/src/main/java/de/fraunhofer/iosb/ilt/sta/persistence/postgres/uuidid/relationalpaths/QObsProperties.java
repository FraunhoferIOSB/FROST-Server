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
 * QObsProperties is a Querydsl query type for QObsProperties
 */
public class QObsProperties extends AbstractQObsProperties<QObsProperties, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 1235830773;

    public static final QObsProperties obsProperties = new QObsProperties("OBS_PROPERTIES");

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public final com.querydsl.sql.PrimaryKey<QObsProperties> obsPropertiesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QMultiDatastreamsObsProperties> _mdopObsPropertyIdFkey = createInvForeignKey(id, "OBS_PROPERTY_ID");

    public final com.querydsl.sql.ForeignKey<QDatastreams> _datastreamsObsPropertyIdFkey = createInvForeignKey(id, "OBS_PROPERTY_ID");

    public QObsProperties(String variable) {
        super(QObsProperties.class, forVariable(variable), "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsProperties(String variable, String schema, String table) {
        super(QObsProperties.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QObsProperties(String variable, String schema) {
        super(QObsProperties.class, forVariable(variable), schema, "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsProperties(Path<? extends QObsProperties> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsProperties(PathMetadata metadata) {
        super(QObsProperties.class, metadata, "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public QObsProperties newWithAlias(String variable) {
        return new QObsProperties(variable);
    }

}
