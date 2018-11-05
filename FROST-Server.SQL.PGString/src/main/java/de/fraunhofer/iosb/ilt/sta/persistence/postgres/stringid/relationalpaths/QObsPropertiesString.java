package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

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
    private static final String TABLE_NAME = "OBS_PROPERTIES";

    public static final QObsPropertiesString OBSPROPERTIES = new QObsPropertiesString(TABLE_NAME);

    public final StringPath id = createString("id");

    public QObsPropertiesString(String variable) {
        super(QObsPropertiesString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
