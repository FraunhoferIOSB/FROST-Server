package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObsProperties;
import java.sql.Types;

/**
 * QObsProperties is a Querydsl query type for QObsProperties
 */
public class QObsProperties extends AbstractQObsProperties<QObsProperties, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -1131991212;

    public static final QObsProperties obsProperties = new QObsProperties("OBS_PROPERTIES");

    public final NumberPath<Long> id = createNumber("id", Long.class);

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
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public QObsProperties newWithAlias(String variable) {
        return new QObsProperties(variable);
    }

}
