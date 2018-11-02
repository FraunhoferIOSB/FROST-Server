package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.sql.ColumnMetadata;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocations;
import java.sql.Types;
import java.util.UUID;

/**
 * QLocationsUuid is a Querydsl query type for QLocationsUuid
 */
public class QLocationsUuid extends AbstractQLocations<QLocationsUuid, ComparablePath<UUID>, UUID> {

    private static final long serialVersionUID = 365881856;

    public static final QLocationsUuid LOCATIONS = new QLocationsUuid("LOCATIONS");

    public final ComparablePath<UUID> genFoiId = createComparable("genFoiId", UUID.class);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public QLocationsUuid(String variable) {
        super(QLocationsUuid.class, forVariable(variable), "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    public QLocationsUuid(String variable, String schema, String table) {
        super(QLocationsUuid.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocationsUuid(String variable, String schema) {
        super(QLocationsUuid.class, forVariable(variable), schema, "LOCATIONS");
        addMetadata();
    }

    public QLocationsUuid(Path<? extends QLocationsUuid> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    public QLocationsUuid(PathMetadata metadata) {
        super(QLocationsUuid.class, metadata, "PUBLIC", "LOCATIONS");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(genFoiId, ColumnMetadata.named("GEN_FOI_ID").ofType(Types.BINARY).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("ID").ofType(Types.BINARY).withSize(2147483647).notNull());
    }

    /**
     * @return the id
     */
    @Override
    public ComparablePath<UUID> getId() {
        return id;
    }

    @Override
    public ComparablePath<UUID> getGenFoiId() {
        return genFoiId;
    }

    @Override
    public QLocationsUuid newWithAlias(String variable) {
        return new QLocationsUuid(variable);
    }

}
