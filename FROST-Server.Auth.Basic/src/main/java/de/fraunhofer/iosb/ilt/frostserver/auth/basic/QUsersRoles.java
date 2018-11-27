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
public class QUsersRoles extends RelationalPathBase<QUsersRoles> {

    private static final long serialVersionUID = 1713698749;
    private static final String TABLE_NAME = "USER_ROLES";

    public static final QUsersRoles USER_ROLES = new QUsersRoles(TABLE_NAME);

    public final StringPath userName = createString("USER_NAME");
    public final StringPath roleName = createString("ROLE_NAME");

    public QUsersRoles(String variable) {
        super(QUsersRoles.class, forVariable(variable), "PUBLIC", TABLE_NAME);
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(userName, ColumnMetadata.named("USER_NAME").ofType(Types.BIGINT).withSize(25).notNull());
        addMetadata(roleName, ColumnMetadata.named("ROLE_NAME").ofType(Types.BIGINT).withSize(15).notNull());
    }

    public QUsersRoles newWithAlias(String variable) {
        return new QUsersRoles(variable);
    }

}
