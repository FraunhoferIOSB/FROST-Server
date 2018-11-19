package de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.relationalpaths;

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
    private static final String TABLE_NAME = "OBS_PROPERTIES";

    public static final QObsPropertiesLong OBSPROPERTIES = new QObsPropertiesLong(TABLE_NAME);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QObsPropertiesLong(String variable) {
        super(QObsPropertiesLong.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
