/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.LiquibaseHelper.CHANGE_SET_NAME;

import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 * MariaDB implementation of the JOOQ Persistence Manager.
 */
public class MariadbPersistenceManager extends JooqAbsPersistenceManager {

    private DSLContext dslContext;

    @Override
    public DSLContext getDslContext() {
        if (dslContext == null) {
            dslContext = DSL.using(getConnectionProvider().get(), SQLDialect.MARIADB);
        }
        return dslContext;
    }

    @Override
    public ExpressionHelper createExpressionHelper(QueryBuilder queryBuilder) {
        return new ExpressionHelper(getCoreSettings(), queryBuilder);
    }

    @Override
    public String checkForUpgrades(Map<String, Object> liquibaseParams) {
        liquibaseParams.put(CHANGE_SET_NAME, "MariadbPersistenceManager");
        return checkForUpgrades(LIQUIBASE_CHANGELOG_FILENAME, liquibaseParams);
    }

    @Override
    public boolean doUpgrades(Writer out, Map<String, Object> liquibaseParams) throws UpgradeFailedException, IOException {
        liquibaseParams.put(CHANGE_SET_NAME, "MariadbPersistenceManager");
        return doUpgrades(LIQUIBASE_CHANGELOG_FILENAME, liquibaseParams, out);
    }

    @Override
    public boolean hasUpdateReturning() {
        return false;
    }

    @Override
    public boolean hasDistinctOn() {
        return false;
    }

}
