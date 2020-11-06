/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.IdGenerationHandler;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import java.util.UUID;
import org.jooq.impl.SQLDataType;

/**
 *
 * @author jab
 * @author scf
 */
public class PostgresPersistenceManagerUuid extends PostgresPersistenceManager<UUID> {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/tablesUuid.xml";

    private static final IdManagerUuid ID_MANAGER = new IdManagerUuid();
    private static TableCollection<UUID> tableCollection = new TableCollection<UUID>(UuidId.PERSISTENCE_TYPE_BYTEARRAY, SQLDataType.UUID);

    public PostgresPersistenceManagerUuid() {
        super(ID_MANAGER, tableCollection);
    }

    @Override
    public String getLiquibaseChangelogFilename() {
        return LIQUIBASE_CHANGELOG_FILENAME;
    }

    @Override
    public IdGenerationHandler createIdGenerationHanlder(Entity e) {
        return new IdGenerationHandlerUuid(e);
    }

}
