/*
 * Copyright (C) 2017 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.json.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.persistence.AbstractPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.ConnectionUtils.ConnectionWrapper;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactory;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.settings.Settings;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.sta.util.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public abstract class PostgresPersistenceManager<J> extends AbstractPersistenceManager {

    public static final Instant DATETIME_MAX_INSTANT = Instant.parse("9999-12-31T23:59:59.999Z");
    public static final Instant DATETIME_MIN_INSTANT = Instant.parse("-4000-01-01T00:00:00.000Z");
    public static final OffsetDateTime DATETIME_MAX = OffsetDateTime.ofInstant(DATETIME_MAX_INSTANT, CoreSettings.UTC);
    public static final OffsetDateTime DATETIME_MIN = OffsetDateTime.ofInstant(DATETIME_MIN_INSTANT, CoreSettings.UTC);

    /**
     * The logger for this class.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(PostgresPersistenceManager.class);

    private CoreSettings settings;
    private ConnectionWrapper connectionProvider;
    private DSLContext dslContext;

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings customSettings = settings.getPersistenceSettings().getCustomSettings();
        connectionProvider = new ConnectionWrapper(customSettings);
    }

    @Override
    public CoreSettings getCoreSettings() {
        return settings;
    }

    public abstract PropertyResolver<J> getPropertyResolver();

    public abstract EntityFactories<J> getEntityFactories();

    public abstract IdGenerationHandler createIdGenerationHanlder(Entity e);

    public abstract String getLiquibaseChangelogFilename();

    public DSLContext createDdslContext() {
        if (dslContext == null) {
            dslContext = DSL.using(connectionProvider.get(), SQLDialect.POSTGRES);
        }
        return dslContext;
    }

    public ConnectionWrapper getConnectionProvider() {
        return connectionProvider;
    }

    @Override
    public boolean validatePath(ResourcePath path) {
        ResourcePathElement element = path.getIdentifiedElement();
        if (element == null) {
            return true;
        }
        ResourcePath tempPath = new ResourcePath();
        while (element != null) {
            tempPath.addPathElement(0, element);
            element = element.getParent();
        }
        return true;
        // TODO: Fix me.
        //return count(tempPath) == 1;
    }

    @Override
    public Entity get(EntityType entityType, Id id) {
        return get(entityType, id, false);
    }

    /**
     * Gets the requested entity and locks the row for update. End the
     * transaction quickly to release the lock.
     *
     * @param entityType The type of entity to fetch.
     * @param id The ID of the entity to fetch.
     * @param forUpdate if true, lock the entities row for update.
     * @return the requested entity.
     */
    private Entity get(EntityType entityType, Id id, boolean forUpdate) {
        QueryBuilder psb = new QueryBuilder(this, settings.getPersistenceSettings(), getPropertyResolver());
        ResultQuery sqlQuery = psb.forTypeAndId(entityType, id)
                .forUpdate(forUpdate)
                .buildSelect();

        Record record = sqlQuery.fetchAny();

        EntityFactory<? extends Entity, J> factory;
        factory = getEntityFactories().getFactoryFor(entityType);
        return factory.create(record, null, new DataSize());
    }

    @Override
    public Object get(ResourcePath path, Query query) {
        ResourcePathElement lastElement = path.getLastElement();
        if (!(lastElement instanceof EntityPathElement) && !(lastElement instanceof EntitySetPathElement)) {
            if (!query.getExpand().isEmpty()) {
                LOGGER.warn("Expand only allowed on Entities or EntitySets. Not on {}!", lastElement.getClass());
                query.getExpand().clear();
            }
            if (!query.getSelect().isEmpty()) {
                LOGGER.warn("Select only allowed on Entities or EntitySets. Not on {}!", lastElement.getClass());
                query.getSelect().clear();
            }
        }

        QueryBuilder psb = new QueryBuilder(this, settings.getPersistenceSettings(), getPropertyResolver())
                .forPath(path)
                .usingQuery(query);

        ResultBuilder entityCreator = new ResultBuilder(this, path, query, psb);
        lastElement.visit(entityCreator);
        Object entity = entityCreator.getEntity();

        if (path.isEntityProperty() && entity instanceof Map) {
            Map map = (Map) entity;
            if (map.get(entityCreator.getEntityName()) == null) {
                return null;
            }
        }
        if (path.isValue() && entity instanceof Map) {
            Map map = (Map) entity;
            entity = map.get(entityCreator.getEntityName());
        }

        return entity;
    }

    @Override
    public boolean doInsert(Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        EntityFactories<J> ef = getEntityFactories();
        EntityFactory<Entity, J> factory = ef.getFactoryFor(entity.getEntityType());
        factory.insert(this, entity);
        return true;
    }

    @Override
    public EntityChangedMessage doUpdate(EntityPathElement pathElement, Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        EntityFactories<J> ef = getEntityFactories();

        entity.setId(pathElement.getId());
        J id = (J) pathElement.getId().getValue();
        if (!ef.entityExists(this, entity)) {
            throw new NoSuchEntityException("No entity of type " + pathElement.getEntityType() + " with id " + id);
        }

        EntityFactory<Entity, J> factory = ef.getFactoryFor(entity.getEntityType());
        return factory.update(this, entity, id);
    }

    @Override
    public EntityChangedMessage doUpdate(EntityPathElement pathElement, JsonPatch patch) throws NoSuchEntityException, IncompleteEntityException {
        final EntityType entityType = pathElement.getEntityType();
        final Id id = pathElement.getId();

        Entity original = get(entityType, id, true);
        original.setEntityPropertiesSet(false, false);
        JsonNode originalNode = EntityFormatter.getObjectMapper().valueToTree(original);
        LOGGER.info("Old {}", originalNode);
        JsonNode newNode;
        try {
            newNode = patch.apply(originalNode);
        } catch (JsonPatchException ex) {
            throw new IllegalArgumentException("Failed to apply patch.", ex);
        }
        LOGGER.info("New {}", newNode);
        Entity newEntity;
        try {
            EntityParser entityParser = new EntityParser(getIdManager().getIdClass());
            newEntity = entityParser.parseEntity(original.getClass(), newNode);
            // Make sure the id is not changed by the patch.
            newEntity.setId(id);
        } catch (IOException ex) {
            LOGGER.error("Failed to parse JSON after patch.");
            throw new IllegalArgumentException("Exception", ex);
        }

        EntityChangedMessage message = new EntityChangedMessage();
        newEntity.setEntityPropertiesSet(original, message);
        if (message.getEpFields().isEmpty() && message.getNpFields().isEmpty()) {
            LOGGER.warn("Patch did not change anything.");
            throw new IllegalArgumentException("Patch did not change anything.");
        }
        EntityFactories<J> ef = getEntityFactories();
        EntityFactory<Entity, J> factory = ef.getFactoryFor(entityType);
        factory.update(this, newEntity, (J) id.getValue());

        message.setEntity(newEntity);
        message.setEventType(EntityChangedMessage.Type.UPDATE);
        return message;
    }

    @Override
    public boolean doDelete(EntityPathElement pathElement) throws NoSuchEntityException {
        EntityFactories<J> ef = getEntityFactories();
        EntityType type = pathElement.getEntityType();
        EntityFactory<Entity, J> factory = ef.getFactoryFor(type);
        factory.delete(this, (J) pathElement.getId().getValue());
        return true;
    }

    @Override
    public void doDelete(ResourcePath path, Query query) {
        query.setSelect(Arrays.asList(EntityProperty.ID));
        QueryBuilder psb = new QueryBuilder(this, settings.getPersistenceSettings(), getPropertyResolver())
                .forPath(path)
                .usingQuery(query);

        Delete sqlDelete = psb.buildDelete((EntitySetPathElement) path.getLastElement());

        long rowCount = sqlDelete.execute();
        LOGGER.debug("Deleted {} rows using query {}", rowCount, sqlDelete);
    }

    @Override
    protected boolean doCommit() {
        return connectionProvider.doCommit();
    }

    @Override
    protected boolean doRollback() {
        return connectionProvider.doRollback();
    }

    @Override
    protected boolean doClose() {
        return connectionProvider.doClose();
    }

    @Override
    public String checkForUpgrades() {
        try {
            Settings customSettings = settings.getPersistenceSettings().getCustomSettings();
            Connection connection = ConnectionUtils.getConnection("FROST-Source", customSettings);
            String liquibaseChangelogFilename = getLiquibaseChangelogFilename();
            return LiquibaseHelper.checkForUpgrades(connection, liquibaseChangelogFilename);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            return "Failed to initialise database:\n"
                    + ex.getLocalizedMessage()
                    + "\n";
        }
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        Settings customSettings = settings.getPersistenceSettings().getCustomSettings();
        Connection connection;
        try {
            connection = ConnectionUtils.getConnection("FROST-Source", customSettings);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            out.append("Failed to initialise database:\n");
            out.append(ex.getLocalizedMessage());
            out.append("\n");
            return false;
        }
        String liquibaseChangelogFilename = getLiquibaseChangelogFilename();
        return LiquibaseHelper.doUpgrades(connection, liquibaseChangelogFilename, out);
    }

}
