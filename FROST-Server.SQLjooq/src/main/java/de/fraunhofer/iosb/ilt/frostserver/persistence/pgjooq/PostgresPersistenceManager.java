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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefNavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.PropertyPersistenceMapper;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.AbstractPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.ConnectionWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.LiquibaseHelper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapper;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.UTC;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.Meta;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public abstract class PostgresPersistenceManager<J extends Comparable> extends AbstractPersistenceManager {

    public static final Instant DATETIME_MAX_INSTANT = Instant.parse("9999-12-30T23:59:59.999Z");
    // jooq fails when year field is not 4 digits long: https://github.com/jOOQ/jOOQ/issues/8178
    // TODO: Change back to -4000 when it is fixed.
    public static final Instant DATETIME_MIN_INSTANT = Instant.parse("0001-01-02T00:00:00.000Z");
    public static final OffsetDateTime DATETIME_MAX = OffsetDateTime.ofInstant(DATETIME_MAX_INSTANT, UTC);
    public static final OffsetDateTime DATETIME_MIN = OffsetDateTime.ofInstant(DATETIME_MIN_INSTANT, UTC);

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresPersistenceManager.class.getName());
    private static final String SOURCE_NAME_FROST = "FROST-Source";

    private boolean initialised = false;

    private final IdManager idManager;
    private TableCollection<J> tableCollection;
    private EntityFactories<J> entityFactories;

    private CoreSettings settings;
    private IdGenerationType idGenerationMode;
    private ConnectionWrapper connectionProvider;
    private DSLContext dslContext;

    public PostgresPersistenceManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public void init(CoreSettings settings, TableCollection<J> tableCollection) {
        this.settings = settings;
        this.tableCollection = tableCollection;
        getTableCollection().setModelRegistry(settings.getModelRegistry());
        Settings customSettings = settings.getPersistenceSettings().getCustomSettings();
        connectionProvider = new ConnectionWrapper(customSettings, SOURCE_NAME_FROST);
        entityFactories = new EntityFactories(settings.getModelRegistry(), idManager, tableCollection);
    }

    private void init() {
        if (initialised) {
            return;
        }
        synchronized (tableCollection) {
            if (!initialised) {
                idGenerationMode = IdGenerationType.findType(settings.getPersistenceSettings().getIdGenerationMode());
                tableCollection.init(entityFactories);
                loadMapping();
                initialised = true;
            }
        }
    }

    @Override
    public CoreSettings getCoreSettings() {
        return settings;
    }

    @Override
    public IdManager getIdManager() {
        return idManager;
    }

    public TableCollection<J> getTableCollection() {
        return tableCollection;
    }

    public EntityFactories<J> getEntityFactories() {
        return entityFactories;
    }

    public DSLContext getDslContext() {
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
        init();
        PathElement element = path.getIdentifiedElement();
        if (element == null) {
            return true;
        }
        ResourcePath tempPath = new ResourcePath();
        int idCount = 0;
        while (element != null) {
            if (element instanceof PathElementEntity) {
                PathElementEntity entityPathElement = (PathElementEntity) element;
                Id id = entityPathElement.getId();
                if (id != null) {
                    idCount++;
                    if (!getEntityFactories().entityExists(this, entityPathElement.getEntityType(), id)) {
                        return false;
                    }
                }
            }
            tempPath.addPathElement(0, element);
            element = element.getParent();
        }
        if (idCount < 2) {
            return true;
        }
        QueryBuilder<J> psb = new QueryBuilder<>(this, settings, getTableCollection());
        ResultQuery<Record1<Integer>> query = psb
                .forPath(tempPath)
                .buildCount();
        Integer count = query.fetchOne().component1();
        return count == 1;
    }

    @Override
    public Entity get(EntityType entityType, Id id) {
        return get(entityType, id, false, null);
    }

    public Entity get(EntityType entityType, Id id, Query query) {
        return get(entityType, id, false, query);
    }

    /**
     * Gets the requested entity and locks the row for update. End the
     * transaction quickly to release the lock.
     *
     * @param entityType The type of entity to fetch.
     * @param id The EP_ID of the entity to fetch.
     * @param forUpdate if true, lock the entities row for update.
     * @return the requested entity.
     */
    private Entity get(EntityType entityType, Id id, boolean forUpdate, Query query) {
        init();
        QueryBuilder<J> psb = new QueryBuilder<>(this, settings, getTableCollection());
        ResultQuery sqlQuery = psb.forTypeAndId(entityType, id)
                .usingQuery(query)
                .forUpdate(forUpdate)
                .buildSelect();

        Record record = sqlQuery.fetchAny();
        if (record == null) {
            return null;
        }
        return psb.getQueryState().entityFromQuery(record, new DataSize());
    }

    @Override
    public Object get(ResourcePath path, Query query) {
        init();
        PathElement lastElement = path.getLastElement();
        if (!(lastElement instanceof PathElementEntity) && !(lastElement instanceof PathElementEntitySet)) {
            if (!query.getExpand().isEmpty()) {
                LOGGER.warn("Expand only allowed on Entities or EntitySets. Not on {}!", lastElement.getClass());
                query.getExpand().clear();
            }
            if (!query.getSelect().isEmpty()) {
                LOGGER.warn("Select only allowed on Entities or EntitySets. Not on {}!", lastElement.getClass());
                query.getSelect().clear();
            }
        }

        QueryBuilder<J> psb = new QueryBuilder<>(this, settings, getTableCollection())
                .forPath(path)
                .usingQuery(query);

        ResultBuilder<J> entityCreator = new ResultBuilder<>(this, path, query, psb);
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
        init();
        StaMainTable<J, ?> table = getTableCollection().getTableForType(entity.getEntityType());
        return table.insertIntoDatabase(this, entity);
    }

    @Override
    public EntityChangedMessage doUpdate(PathElementEntity pathElement, Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        init();
        EntityFactories<J> ef = getEntityFactories();

        entity.setId(pathElement.getId());
        J id = (J) pathElement.getId().getValue();
        if (!ef.entityExists(this, entity)) {
            throw new NoSuchEntityException("No entity of type " + pathElement.getEntityType() + " with id " + id);
        }

        StaMainTable<J, ?> table = getTableCollection().getTableForType(entity.getEntityType());
        return table.updateInDatabase(this, entity, id);
    }

    @Override
    public EntityChangedMessage doUpdate(PathElementEntity pathElement, JsonPatch patch) throws NoSuchEntityException, IncompleteEntityException {
        init();
        final EntityType entityType = pathElement.getEntityType();
        final Id id = pathElement.getId();

        Entity original = get(entityType, id, true, null);
        if (original == null) {
            throw new IllegalArgumentException("No Entity of type " + entityType.entityName + " with id " + id);
        }
        original.setEntityPropertiesSet(false, false);
        JsonNode originalNode = JsonWriter.getObjectMapper().valueToTree(original);
        LOGGER.trace("Old {}", originalNode);
        JsonNode newNode;
        try {
            newNode = patch.apply(originalNode);
        } catch (JsonPatchException ex) {
            throw new IllegalArgumentException("Failed to apply patch.", ex);
        }
        LOGGER.trace("New {}", newNode);
        Entity newEntity;
        final ModelRegistry modelRegistry = settings.getModelRegistry();
        try {
            JsonReader entityParser = new JsonReader(modelRegistry);
            newEntity = entityParser.parseEntity(original.getEntityType(), newNode.toString());
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

        StaMainTable<J, ?> table = getTableCollection().getTableForType(entityType);
        table.updateInDatabase(this, newEntity, (J) id.getValue());

        message.setEntity(newEntity);
        message.setEventType(EntityChangedMessage.Type.UPDATE);
        return message;
    }

    @Override
    public boolean doDelete(PathElementEntity pathElement) throws NoSuchEntityException {
        init();
        EntityType type = pathElement.getEntityType();
        StaMainTable<J, ?> table = getTableCollection().getTableForType(type);
        table.delete(this, (J) pathElement.getId().getValue());
        return true;
    }

    @Override
    public void doDelete(ResourcePath path, Query query) {
        init();
        query.clearSelect();
        query.addSelect(path.getMainElementType().getEntityProperty("id"));
        QueryBuilder<J> psb = new QueryBuilder<>(this, settings, getTableCollection())
                .forPath(path)
                .usingQuery(query);

        Delete sqlDelete = psb.buildDelete((PathElementEntitySet) path.getLastElement());

        long rowCount = sqlDelete.execute();
        LOGGER.debug("Deleted {} rows using query {}", rowCount, sqlDelete);
    }

    @Override
    protected boolean doCommit() {
        return connectionProvider.commit();
    }

    @Override
    protected boolean doRollback() {
        return connectionProvider.rollback();
    }

    @Override
    protected boolean doClose() {
        try {
            connectionProvider.close();
            return true;
        } catch (SQLException ex) {
            LOGGER.error("Failed to close connection.", ex);
            return false;
        }
    }

    public IdGenerationType getIdGenerationMode() {
        return idGenerationMode;
    }

    protected abstract boolean validateClientSuppliedId(Id entityId);

    /**
     * Modify the entity id.
     */
    public void modifyClientSuppliedId(Entity entity) {
        // Default does nothing.
    }

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
    public boolean useClientSuppliedId(Entity entity) throws IncompleteEntityException {
        Id entityId = entity.getId();
        switch (idGenerationMode) {
            case SERVER_GENERATED_ONLY:
                if (entityId == null || entityId.getValue() == null) {
                    LOGGER.trace("Using server generated id.");
                    return false;
                } else {
                    LOGGER.warn("idGenerationMode is '{}' but @iot.id '{}' is present. Ignoring @iot.id.", idGenerationMode, entityId);
                    return false;
                }

            case SERVER_AND_CLIENT_GENERATED:
                if (!validateClientSuppliedId(entityId)) {
                    LOGGER.debug("No valid @iot.id. Using server generated id.");
                    return false;
                }
                break;

            case CLIENT_GENERATED_ONLY:
                if (!validateClientSuppliedId(entityId)) {
                    LOGGER.error("No @iot.id and idGenerationMode is '{}'", idGenerationMode);
                    throw new IncompleteEntityException("Error: no @iot.id");
                }
                break;

            default:
                // not a valid generation mode
                LOGGER.error("idGenerationMode '{}' is not implemented.", idGenerationMode);
                throw new IllegalArgumentException("idGenerationMode '" + idGenerationMode.toString() + "' is not implemented.");
        }

        LOGGER.info("Using client generated id.");
        return true;
    }

    public String checkForUpgrades(String liquibaseChangelogFilename) {
        try {
            Settings customSettings = settings.getPersistenceSettings().getCustomSettings();
            Connection connection = ConnectionUtils.getConnection(SOURCE_NAME_FROST, customSettings);
            return LiquibaseHelper.checkForUpgrades(connection, liquibaseChangelogFilename);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            return "Failed to initialise database:\n"
                    + ex.getLocalizedMessage()
                    + "\n";
        }
    }

    public boolean doUpgrades(String liquibaseChangelogFilename, Writer out) throws UpgradeFailedException, IOException {
        Settings customSettings = settings.getPersistenceSettings().getCustomSettings();
        Connection connection;
        try {
            connection = ConnectionUtils.getConnection(SOURCE_NAME_FROST, customSettings);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            out.append("Failed to initialise database:\n");
            out.append(ex.getLocalizedMessage());
            out.append("\n");
            return false;
        }
        return LiquibaseHelper.doUpgrades(connection, liquibaseChangelogFilename, out);
    }

    @Override
    public void addModelMapping(DefModel modelDefinition) {
        tableCollection.getModelDefinitions().add(modelDefinition);
    }

    private void loadMapping() {
        final List<DefModel> modelDefinitions = tableCollection.getModelDefinitions();
        if (modelDefinitions.isEmpty()) {
            return;
        }

        ModelRegistry modelRegistry = settings.getModelRegistry();
        getDslContext();

        for (DefModel modelDefinition : modelDefinitions) {

            LOGGER.info("Reading Database Tables.");
            for (DefEntityType entityTypeDef : modelDefinition.getEntityTypes().values()) {
                final String tableName = entityTypeDef.getTable();
                LOGGER.info("  Table: {}.", tableName);
                getDbTable(tableName);
                getOrCreateTable(entityTypeDef.getEntityType(), entityTypeDef.getTable());
            }
        }

        for (DefModel modelDefinition : modelDefinitions) {
            for (DefEntityType entityTypeDef : modelDefinition.getEntityTypes().values()) {
                StaTableDynamic<J> typeStaTable = getOrCreateTable(entityTypeDef.getEntityType(), entityTypeDef.getTable());

                for (DefEntityProperty propertyDef : entityTypeDef.getEntityProperties().values()) {
                    final Property property = propertyDef.getEntityPropertyMain();
                    for (PropertyPersistenceMapper handler : propertyDef.getHandlers()) {
                        if (handler instanceof FieldMapper) {
                            ((FieldMapper) handler).registerField(this, typeStaTable, property);
                        }
                    }
                }
                for (DefNavigationProperty propertyDef : entityTypeDef.getNavigationProperties().values()) {
                    final Property property = propertyDef.getNavigationProperty(modelRegistry);
                    for (PropertyPersistenceMapper handler : propertyDef.getHandlers()) {
                        if (handler instanceof FieldMapper) {
                            ((FieldMapper) handler).registerField(this, typeStaTable, property);
                        }
                    }
                }
            }
        }

        for (DefModel modelDefinition : modelDefinitions) {
            for (DefEntityType entityTypeDef : modelDefinition.getEntityTypes().values()) {
                StaTableDynamic<J> orCreateTable = getOrCreateTable(entityTypeDef.getEntityType(), entityTypeDef.getTable());
                for (DefEntityProperty propertyDef : entityTypeDef.getEntityProperties().values()) {
                    for (PropertyPersistenceMapper handler : propertyDef.getHandlers()) {
                        if (handler instanceof FieldMapper) {
                            ((FieldMapper) handler).registerMapping(this, orCreateTable, propertyDef.getEntityPropertyMain());
                        }
                    }
                }
                for (DefNavigationProperty propertyDef : entityTypeDef.getNavigationProperties().values()) {
                    for (PropertyPersistenceMapper handler : propertyDef.getHandlers()) {
                        if (handler instanceof FieldMapper) {
                            ((FieldMapper) handler).registerMapping(this, orCreateTable, propertyDef.getNavigationProperty(modelRegistry));
                        }
                    }
                }
            }
        }
        // Done, release the model definitions.
        tableCollection.clearModelDefinitions();
    }

    public Table<?> getDbTable(String tableName) {
        return getDbTable(DSL.name(tableName));
    }

    public Table<?> getDbTable(Name tableName) {
        final Meta meta = dslContext.meta();
        final List<Table<?>> tables = meta.getTables(tableName);
        if (tables.isEmpty()) {
            LOGGER.error("Table {} not found. Please initialise the database!", tableName);
            throw new IllegalArgumentException("Table " + tableName + " not found.");
        }
        if (tables.size() != 1) {
            LOGGER.error("Table name {} found {} times.", tableName, tables.size());
            throw new IllegalArgumentException("Failed to initialise: Table name " + tableName + " found " + tables.size() + " times.");
        }
        return tables.get(0);
    }

    private StaTableDynamic<J> getOrCreateTable(EntityType entityType, String tableName) {
        if (entityType == null) {
            throw new IllegalArgumentException("Not implemented yet");
        }
        StaMainTable<J, ?> table = tableCollection.getTableForType(entityType);
        if (table == null) {
            LOGGER.info("  Registering StaTable {} ({})", tableName, entityType);
            StaTableDynamic<J> newTable = new StaTableDynamic<>(DSL.name(tableName), entityType, tableCollection.getIdType());
            tableCollection.registerTable(entityType, newTable);
            table = newTable;
        }
        if (table instanceof StaTableDynamic) {
            return (StaTableDynamic<J>) table;
        }
        throw new IllegalStateException("Table already exists, but is not of type dynamic.");
    }

}
