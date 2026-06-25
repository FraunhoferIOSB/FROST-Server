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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaLinkTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.request.EditFeatures;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import jakarta.json.JsonPatch;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import org.jooq.AttachableQueryPart;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;

/**
 * The interface for PersistanceManagers based on JOOQ.
 */
public interface JooqPersistenceManager extends LiquibaseUser, PersistenceManager {

    /**
     * Constant for logging access to link tables.
     */
    public static final String LINK_TABLE = "LinkTable";

    String checkForUpgrades(String liquibaseChangelogFilename, Map<String, Object> params);

    boolean doDelete(PathElementEntity pathElement) throws NoSuchEntityException;

    void doDelete(ResourcePath path, Query query);

    Entity doInsert(Entity entity, EditFeatures updateMode) throws NoSuchEntityException, IncompleteEntityException;

    EntityChangedMessage doUpdate(PathElementEntity pathElement, Entity entity, EditFeatures updateMode) throws NoSuchEntityException, IncompleteEntityException;

    EntityChangedMessage doUpdate(PathElementEntity pathElement, JsonPatch patch) throws NoSuchEntityException, IncompleteEntityException;

    boolean doUpgrades(String liquibaseChangelogFilename, Map<String, Object> params, Writer out) throws UpgradeFailedException, IOException;

    void generateLiquibaseVariables(Map<String, Object> target, String entity, String type);

    ExpressionHelper createExpressionHelper(QueryBuilder queryBuilder);

    Entity get(EntityType entityType, PkValue id, Query query);

    ConnectionUtils.ConnectionWrapper getConnectionProvider();

    public <R extends org.jooq.Record> ResultQuery<R> setTimeout(ResultQuery<R> query);

    public default int timeExecute(org.jooq.Query query, String loggedEntityName) {
        return timeExecution(query::execute, query, loggedEntityName);
    }

    public default <R extends org.jooq.Record> Result<R> timeFetch(ResultQuery<R> query, String loggedEntityName) {
        return timeExecution(query::fetch, query, loggedEntityName);
    }

    public default <R extends org.jooq.Record> R timeFetchOne(ResultQuery<R> query, String loggedEntityName) {
        return timeExecution(query::fetchOne, query, loggedEntityName);
    }

    public default <R extends org.jooq.Record> R timeFetchAny(ResultQuery<R> query, String loggedEntityName) {
        return timeExecution(query::fetchAny, query, loggedEntityName);
    }

    public <V> V timeExecution(QueryExecution<V> task, AttachableQueryPart loggableQuery, String loggedEntityName);

    DataType<?> getDataTypeFor(String type);

    Table<?> getDbTable(String tableName);

    Table<?> getDbTable(Name tableName);

    DSLContext getDslContext();

    EntityFactories getEntityFactories();

    StaLinkTableDynamic getOrCreateLinkTable(String tableName);

    TableCollection getTableCollection();

    /**
     * Modify the entity id.
     *
     * @param entity the Entity to modify the Id for.
     */
    void modifyClientSuppliedId(Entity entity);

    /**
     *
     * Checks if a client generated id can/should be used with respect to the
     * idGenerationMode.
     *
     * @param entity The entity to check the id for.
     * @return true if a valid client id can be used.
     * @throws IncompleteEntityException Will be thrown if @iot.id is missing
     * for client generated ids.
     * @throws IllegalArgumentException Will be thrown if idGenerationMode is
     * not supported.
     */
    boolean useClientSuppliedId(Entity entity) throws IncompleteEntityException;

    public default boolean hasUpdateReturning() {
        return true;
    }

    public default boolean hasDistinctOn() {
        return true;
    }

    public static interface QueryExecution<V> {

        V call() throws DataAccessException;
    }
}
