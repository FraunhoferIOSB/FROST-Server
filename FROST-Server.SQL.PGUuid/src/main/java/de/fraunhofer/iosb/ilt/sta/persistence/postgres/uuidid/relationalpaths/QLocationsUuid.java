package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.relationalpaths;

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
    private static final String TABLE_NAME = "LOCATIONS";

    public static final QLocationsUuid LOCATIONS = new QLocationsUuid(TABLE_NAME);

    public final ComparablePath<UUID> genFoiId = createComparable("genFoiId", UUID.class);

    public final ComparablePath<UUID> id = createComparable("id", UUID.class);

    public QLocationsUuid(String variable) {
        super(QLocationsUuid.class, forVariable(variable), "PUBLIC", TABLE_NAME);
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
