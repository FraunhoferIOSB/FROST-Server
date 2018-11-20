package de.fraunhofer.iosb.ilt.frostserver.auth.basic;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;
import java.sql.Types;

/**
 * QLocationsHistLocationsLong is a Querydsl query type for
 * QLocationsHistLocationsLong
 */
public class QUsers extends RelationalPathBase<QUsers> {

    private static final long serialVersionUID = 1713698749;
    private static final String TABLE_NAME = "USERS";

    public static final QUsers USERS = new QUsers(TABLE_NAME);

    public final StringPath userName = createString("USER_NAME");
    public final StringPath userPass = createString("USER_PASS");

    public QUsers(String variable) {
        super(QUsers.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(userName, ColumnMetadata.named("USER_NAME").ofType(Types.BIGINT).withSize(25).notNull());
        addMetadata(userPass, ColumnMetadata.named("USER_PASS").ofType(Types.BIGINT).withSize(255).notNull());
    }

    public QUsers newWithAlias(String variable) {
        return new QUsers(variable);
    }

}
