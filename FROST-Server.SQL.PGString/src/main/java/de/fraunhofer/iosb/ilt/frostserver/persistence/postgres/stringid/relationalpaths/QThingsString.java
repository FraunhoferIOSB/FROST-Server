package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.stringid.relationalpaths;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQThings;
import java.sql.Types;

/**
 * QThingsString is a Querydsl query type for QThingsString
 */
public class QThingsString extends AbstractQThings<QThingsString, StringPath, String> {

    private static final long serialVersionUID = -1006520967;
    private static final String TABLE_NAME = "THINGS";

    public static final QThingsString THINGS = new QThingsString(TABLE_NAME);

    public final StringPath id = createString("id");

    public QThingsString(String variable) {
        super(QThingsString.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
    public QThingsString newWithAlias(String variable) {
        return new QThingsString(variable);
    }

}
