package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThings;
import java.sql.Types;

/**
 * QThingsString is a Querydsl query type for QThingsString
 */
public class QThingsString extends AbstractQThings<QThingsString, StringPath, String> {

    private static final long serialVersionUID = -1006520967;

    public static final QThingsString THINGS = new QThingsString("THINGS");

    public final StringPath id = createString("id");

    public QThingsString(String variable) {
        super(QThingsString.class, forVariable(variable), "PUBLIC", "THINGS");
        addMetadata();
    }

    public QThingsString(String variable, String schema, String table) {
        super(QThingsString.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QThingsString(String variable, String schema) {
        super(QThingsString.class, forVariable(variable), schema, "THINGS");
        addMetadata();
    }

    public QThingsString(Path<? extends QThingsString> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "THINGS");
        addMetadata();
    }

    public QThingsString(PathMetadata metadata) {
        super(QThingsString.class, metadata, "PUBLIC", "THINGS");
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
