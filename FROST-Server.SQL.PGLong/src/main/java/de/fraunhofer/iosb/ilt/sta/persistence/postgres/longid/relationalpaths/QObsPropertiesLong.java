package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObsProperties;
import java.sql.Types;

/**
 * QObsPropertiesLong is a Querydsl query type for QObsPropertiesLong
 */
public class QObsPropertiesLong extends AbstractQObsProperties<QObsPropertiesLong, NumberPath<Long>, Long> {

    private static final long serialVersionUID = -1131991212;

    public static final QObsPropertiesLong OBSPROPERTIES = new QObsPropertiesLong("OBS_PROPERTIES");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QObsPropertiesLong(String variable) {
        super(QObsPropertiesLong.class, forVariable(variable), "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsPropertiesLong(String variable, String schema, String table) {
        super(QObsPropertiesLong.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QObsPropertiesLong(String variable, String schema) {
        super(QObsPropertiesLong.class, forVariable(variable), schema, "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsPropertiesLong(Path<? extends QObsPropertiesLong> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsPropertiesLong(PathMetadata metadata) {
        super(QObsPropertiesLong.class, metadata, "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BIGINT).withSize(19).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public NumberPath<Long> getId() {
        return id;
    }

    @Override
    public QObsPropertiesLong newWithAlias(String variable) {
        return new QObsPropertiesLong(variable);
    }

}
