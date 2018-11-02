package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObsProperties;
import java.sql.Types;

/**
 * QObsPropertiesString is a Querydsl query type for QObsPropertiesString
 */
public class QObsPropertiesString extends AbstractQObsProperties<QObsPropertiesString, StringPath, String> {

    private static final long serialVersionUID = 664655775;

    public static final QObsPropertiesString OBSPROPERTIES = new QObsPropertiesString("OBS_PROPERTIES");

    public final StringPath id = createString("id");

    public QObsPropertiesString(String variable) {
        super(QObsPropertiesString.class, forVariable(variable), "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsPropertiesString(String variable, String schema, String table) {
        super(QObsPropertiesString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QObsPropertiesString(String variable, String schema) {
        super(QObsPropertiesString.class, forVariable(variable), schema, "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsPropertiesString(Path<? extends QObsPropertiesString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    public QObsPropertiesString(PathMetadata metadata) {
        super(QObsPropertiesString.class, metadata, "PUBLIC", "OBS_PROPERTIES");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.VARCHAR).withSize(36).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public StringPath getId() {
        return id;
    }

    @Override
    public QObsPropertiesString newWithAlias(String variable) {
        return new QObsPropertiesString(variable);
    }

}
