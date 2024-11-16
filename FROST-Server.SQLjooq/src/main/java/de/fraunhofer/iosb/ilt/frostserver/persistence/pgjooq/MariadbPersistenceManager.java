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

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.TAG_DB_SCHEMA_PRIORITY;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.TAG_DB_URL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.LiquibaseHelper.CHANGE_SET_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_PERSISTENCE;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.NOT_IMPLEMENTED_MULTI_VALUE_PK;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.VALUE_ID_TYPE_LONG;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.VALUE_ID_TYPE_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.VALUE_ID_TYPE_UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReaderDefault;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PrimaryKey;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefNavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefPmHook;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.PmHook;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.PropertyPersistenceMapper;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.AbstractPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostDelete;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPostUpdate;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreDelete;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreUpdate;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.Relation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaLinkTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.ConnectionWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.LiquibaseHelper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.HookValidator;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.SecurityModel.SecurityEntry;
import de.fraunhofer.iosb.ilt.frostserver.util.SecurityWrapper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.IOException;
import java.io.Writer;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.time4j.Moment;
import net.time4j.format.expert.Iso8601Format;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Delete;
import org.jooq.Meta;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 */
public class MariadbPersistenceManager extends AbstractPersistenceManager implements JooqPersistenceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MariadbPersistenceManager.class.getName());
    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/core.xml";

    public static final String DATETIME_MAX_INSTANT = "9999-12-30T23:59:59.999Z";
    // jooq fails when year field is not 4 digits long: https://github.com/jOOQ/jOOQ/issues/8178
    // TODO: Change back to -4000 when it is fixed.
    public static final String DATETIME_MIN_INSTANT = "0001-01-02T00:00:00.000Z";
    public static final Moment DATETIME_MAX = parseMoment(DATETIME_MAX_INSTANT);
    public static final Moment DATETIME_MIN = parseMoment(DATETIME_MIN_INSTANT);

    static final Moment parseMoment(String value) {
        try {
            return Iso8601Format.EXTENDED_DATE_TIME_OFFSET.parse(value);
        } catch (ParseException ex) {
            LOGGER.error("Failed to parse Moment: {}", value);
            return null;
        }
    }

    private static final String SOURCE_NAME_FROST = "FROST-Source";
    private static final String ID_TYPE = "idType-";

    private static final Map<CoreSettings, TableCollection> tableCollections = new HashMap<>();

    private boolean initialised = false;

    private TableCollection tableCollection;
    private EntityFactories entityFactories;

    private CoreSettings settings;
    private PersistenceSettings persistenceSettings;
    private ConnectionWrapper connectionProvider;
    private String connectionName;
    private DSLContext dslContext;
    private String schemaPriority;

    private final Map<Name, Table<?>> tableCache = new HashMap<>();

    /**
     * Tracker for the amount of data fetched form the DB by this PM.
     */
    private DataSize dataSize;

    private static TableCollection getTableCollection(CoreSettings settings) {
        return tableCollections.computeIfAbsent(settings, t -> new TableCollection().setModelRegistry(t.getModelRegistry()));
    }

    @Override
    public InitResult init(CoreSettings settings) {
        this.settings = settings;
        tableCollection = getTableCollection(settings);
        persistenceSettings = settings.getPersistenceSettings();
        getTableCollection().setModelRegistry(settings.getModelRegistry());
        final Settings customSettings = persistenceSettings.getCustomSettings();
        final String connectionUrl = customSettings.get(TAG_DB_URL, ConnectionUtils.class);
        if (StringHelper.isNullOrEmpty(connectionUrl)) {
            connectionName = SOURCE_NAME_FROST;
        } else {
            connectionName = connectionUrl;
        }
        connectionProvider = new ConnectionWrapper(customSettings, connectionName);
        entityFactories = new EntityFactories(settings.getModelRegistry(), tableCollection);
        dataSize = new DataSize(settings.getDataSizeMax());
        schemaPriority = customSettings.get(TAG_DB_SCHEMA_PRIORITY, ConnectionUtils.class);
        return InitResult.INIT_OK;
    }

    private void init() {
        if (initialised) {
            return;
        }
        synchronized (tableCollection) {
            if (!initialised) {
                if (tableCollection.init(this)) {
                    loadMapping();
                    validateMappings();
                }
                initialised = true;
            }
        }
    }

    @Override
    public CoreSettings getCoreSettings() {
        return settings;
    }

    @Override
    public TableCollection getTableCollection() {
        return tableCollection;
    }

    @Override
    public EntityFactories getEntityFactories() {
        return entityFactories;
    }

    @Override
    public DSLContext getDslContext() {
        if (dslContext == null) {
            dslContext = DSL.using(connectionProvider.get(), SQLDialect.MARIADB);
        }
        return dslContext;
    }

    @Override
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
            if (element instanceof PathElementEntity entityPathElement) {
                PkValue pkValues = entityPathElement.getPkValues();
                if (pkValues != null) {
                    idCount++;
                    final boolean userIsAdmin = PrincipalExtended.getLocalPrincipal().isAdmin();
                    if (!entityFactories.entityExists(this, entityPathElement.getEntityType(), pkValues, userIsAdmin)) {
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
        QueryBuilder psb = new QueryBuilder(this);
        ResultQuery<Record1<Integer>> query = psb
                .forPath(tempPath)
                .buildCount();
        Integer count = query.fetchOne().component1();
        return count == 1;
    }

    @Override
    public Entity get(EntityType entityType, PkValue pk) {
        return get(entityType, pk, false, null);
    }

    @Override
    public Entity get(EntityType entityType, PkValue pk, Query query) {
        return get(entityType, pk, false, query);
    }

    /**
     * Gets the requested entity and locks the row for update. End the
     * transaction quickly to release the lock.
     *
     * @param entityType The type of entity to fetch.
     * @param pk The Primary Key of the entity to fetch.
     * @param forUpdate if true, lock the entities row for update.
     * @return the requested entity.
     */
    private Entity get(EntityType entityType, PkValue pk, boolean forUpdate, Query query) {
        init();
        QueryBuilder queryBuilder = new QueryBuilder(this);
        ResultQuery sqlQuery = queryBuilder.forTypeAndId(entityType, pk)
                .usingQuery(query)
                .forUpdate(forUpdate)
                .buildSelect();

        Record result = sqlQuery.fetchAny();
        if (result == null) {
            return null;
        }
        return queryBuilder.getQueryState().entityFromRecord(result, dataSize, query);
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

        QueryBuilder queryBuilder = new QueryBuilder(this)
                .forPath(path)
                .usingQuery(query);

        ResultBuilder entityCreator = new ResultBuilder(this, path, query, queryBuilder, dataSize);
        try {
            lastElement.visit(entityCreator);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException("Failed to convert result to entity.", ex);
        }
        Object entity = entityCreator.getEntity();

        if (entity instanceof Map map) {
            if (path.isEntityProperty() && map.get(entityCreator.getEntityName()) == null) {
                return null;
            }
            if (path.isValue()) {
                entity = map.get(entityCreator.getEntityName());
            }
        }

        return entity;
    }

    @Override
    public Entity doInsert(Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException {
        init();
        StaMainTable<?> table = getTableCollection().getTableForType(entity.getEntityType());
        return table.insertIntoDatabase(this, entity, updateMode, dataSize);
    }

    @Override
    public EntityChangedMessage doUpdate(PathElementEntity pathElement, Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException {
        init();
        final EntityFactories ef = getEntityFactories();
        final PkValue id = pathElement.getPkValues();
        entity.setPrimaryKeyValues(id);
        final boolean userIsAdmin = PrincipalExtended.getLocalPrincipal().isAdmin();
        if (!ef.entityExists(this, entity, userIsAdmin)) {
            throw new NoSuchEntityException("No entity of type " + pathElement.getEntityType() + " with id " + id);
        }

        StaMainTable<?> table = getTableCollection().getTableForType(entity.getEntityType());
        return table.updateInDatabase(this, entity, id, updateMode, dataSize);
    }

    @Override
    public EntityChangedMessage doUpdate(PathElementEntity pathElement, JsonPatch patch) throws NoSuchEntityException, IncompleteEntityException {
        init();
        final EntityType entityType = pathElement.getEntityType();
        final PkValue id = pathElement.getPkValues();

        Entity original = get(entityType, id, true, null);
        if (original == null) {
            throw new IllegalArgumentException("No Entity of type " + entityType.entityName + " with id " + id);
        }
        original.setEntityPropertiesSet(false, false);
        original.setQuery(settings.getModelRegistry().getMessageQueryGenerator().getQueryFor(entityType));
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
            JsonReaderDefault entityParser = new JsonReaderDefault(modelRegistry, PrincipalExtended.getLocalPrincipal());
            newEntity = entityParser.parseEntity(original.getEntityType(), newNode.toString());
            // Make sure the id is not changed by the patch.
            newEntity.setPrimaryKeyValues(id);
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

        StaMainTable<?> table = getTableCollection().getTableForType(entityType);
        table.updateInDatabase(this, newEntity, id, UpdateMode.UPDATE_ODATA_40, dataSize);

        message.setEntity(newEntity);
        message.setEventType(EntityChangedMessage.Type.UPDATE);
        return message;
    }

    @Override
    public void deleteRelation(PathElementEntity source, NavigationPropertyMain np, PathElementEntity target) throws IncompleteEntityException, NoSuchEntityException {
        if (!np.isEntitySet() && np.isRequired()) {
            throw new IncompleteEntityException("Deleting a required relation is not allowed. Delete the entity instead.");
        }
        NavigationPropertyMain inverse = np.getInverse();
        if (inverse != null && !inverse.isEntitySet() && inverse.isRequired()) {
            throw new IncompleteEntityException("Deleting a required relation is not allowed. Delete the entity instead.");
        }
        final boolean userIsAdmin = PrincipalExtended.getLocalPrincipal().isAdmin();
        final StaMainTable<?> sourceTable = getTableCollection().getTableForType(source.getEntityType());
        final Relation<?> relation = sourceTable.findRelation(np.getName());
        final Entity sourceEntity = EntityFactories.entityFromId(source.getEntityType(), source.getPkValues());
        if (!entityFactories.entityExists(this, sourceEntity, userIsAdmin)) {
            throw new NoSuchEntityException("Source entity not found: " + source.getEntityType() + "(" + source.getPkValues() + ")");
        }
        final Entity targetEntity = EntityFactories.entityFromId(target.getEntityType(), target.getPkValues());
        if (!entityFactories.entityExists(this, targetEntity, userIsAdmin)) {
            throw new NoSuchEntityException("Source entity not found: " + target.getEntityType() + "(" + target.getPkValues() + ")");
        }
        relation.unLink(this, sourceEntity, targetEntity, np);
    }

    @Override
    public boolean doDelete(PathElementEntity pathElement) throws NoSuchEntityException {
        init();
        EntityType type = pathElement.getEntityType();
        StaMainTable<?> table = getTableCollection().getTableForType(type);
        table.delete(this, pathElement.getPkValues());
        return true;
    }

    @Override
    public void doDelete(ResourcePath path, Query query) {
        init();
        query.clearSelect();
        query.addSelect(path.getMainElementType().getEntityProperty("id"));
        QueryBuilder psb = new QueryBuilder(this)
                .forPath(path)
                .usingQuery(query);

        Delete sqlDelete = psb.buildDelete((PathElementEntitySet) path.getLastElement());

        long rowCount = sqlDelete.execute();
        LOGGER.debug("Deleted {} rows using query {}", rowCount, sqlDelete);
    }

    @Override
    public void setRole(Principal user) {
        if (settings.getPersistenceSettings().isTransactionRole()) {
            getDslContext()
                    .setLocal(DSL.name("ROLE"), DSL.val(user == null ? "anonymous" : user.getName()))
                    .execute();
        }
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

    protected boolean validateClientSuppliedId(PkValue entityId) {
        return entityId != null && entityId.isFullySet();
    }

    /**
     * Modify the entity id.
     *
     * @param entity the Entity to modify the Id for.
     */
    @Override
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
    @Override
    public boolean useClientSuppliedId(Entity entity) throws IncompleteEntityException {
        final PkValue entityId = entity.getPrimaryKeyValues();
        final EntityType entityType = entity.getEntityType();
        final IdGenerationType typeIdGenerationMode = (IdGenerationType) entityType.getIdGenerationMode();
        switch (typeIdGenerationMode) {
            case SERVER_GENERATED_ONLY:
                if (entityId.isFullyUnSet()) {
                    LOGGER.trace("Using server generated id.");
                    return false;
                } else {
                    LOGGER.warn("idGenerationMode is '{}' but @iot.id '{}' is present. Ignoring @iot.id.", typeIdGenerationMode, entityId);
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
                    LOGGER.error("No @iot.id and idGenerationMode is '{}'", typeIdGenerationMode);
                    throw new IncompleteEntityException("Error: no @iot.id");
                }
                break;

            default:
                // not a valid generation mode
                LOGGER.error("idGenerationMode '{}' is not implemented.", typeIdGenerationMode);
                throw new IllegalArgumentException("idGenerationMode '" + typeIdGenerationMode.toString() + "' is not implemented.");
        }

        LOGGER.debug("Using client generated id.");
        return true;
    }

    @Override
    public String checkForUpgrades(String liquibaseChangelogFilename, Map<String, Object> params) {
        LOGGER.info("Checking for upgrades in {}", liquibaseChangelogFilename);
        try {
            final Settings customSettings = persistenceSettings.getCustomSettings();
            final Connection connection = ConnectionUtils.getConnection(connectionName, customSettings);
            return LiquibaseHelper.checkForUpgrades(connection, liquibaseChangelogFilename, params);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            return "Failed to initialise database:\n"
                    + ex.getLocalizedMessage()
                    + "\n";
        }
    }

    @Override
    public boolean doUpgrades(String liquibaseChangelogFilename, Map<String, Object> params, Writer out) throws UpgradeFailedException, IOException {
        LOGGER.info("Applying upgrades in {}", liquibaseChangelogFilename);
        final Settings customSettings = persistenceSettings.getCustomSettings();
        final Connection connection;
        try {
            connection = ConnectionUtils.getConnection(connectionName, customSettings);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            out.append("Failed to initialise database:\n");
            out.append(ex.getLocalizedMessage());
            out.append("\n");
            return false;
        }
        return LiquibaseHelper.doUpgrades(connection, liquibaseChangelogFilename, params, out);
    }

    @Override
    public void addModelMapping(DefModel modelDefinition) {
        tableCollection.getModelDefinitions().add(modelDefinition);
    }

    @Override
    public void addSecurityDefinition(SecurityEntry entry) {
        String tableName = entry.getTableName();
        List<SecurityWrapper> wrappers = entry.getWrappers();
        for (SecurityWrapper wrapper : wrappers) {
            if (wrapper instanceof SecurityTableWrapper stw) {
                tableCollection.addSecurityWrapper(tableName, stw);
            } else if (wrapper instanceof HookValidator hv) {
                tableCollection.addSecurityValidator(tableName, hv);
            } else {
                LOGGER.error("Unknown SecurityWrapper type: {}", wrapper);
            }
        }
    }

    private void loadMapping() {
        final List<DefModel> modelDefinitions = tableCollection.getModelDefinitions();
        if (modelDefinitions.isEmpty()) {
            return;
        }
        LOGGER.info("Loading Database Mappings...");

        getDslContext();
        final ModelRegistry modelRegistry = settings.getModelRegistry();

        LOGGER.info("Reading Database Tables.");
        for (DefModel modelDefinition : modelDefinitions) {
            for (DefEntityType entityTypeDef : modelDefinition.getEntityTypes()) {
                final String tableName = entityTypeDef.getTable();
                if (!StringHelper.isNullOrEmpty(tableName)) {
                    LOGGER.info("  Table: {}.", tableName);
                    getDbTable(tableName);
                    StaMainTable mainTable = getOrCreateMainTable(entityTypeDef.getEntityType(modelRegistry), entityTypeDef.getTable());
                    tableCollection.initSecurityWrapper(mainTable);
                    tableCollection.initSecurityValidators(mainTable, this);
                }
            }
        }

        for (DefModel modelDefinition : modelDefinitions) {
            registerModelFields(modelDefinition);
        }

        for (DefModel modelDefinition : modelDefinitions) {
            registerModelMappings(modelDefinition);
        }

        for (DefModel modelDefinition : modelDefinitions) {
            registerHooks(modelDefinition);
        }
        // Done, release the model definitions.
        tableCollection.clearModelDefinitions();
    }

    private void validateMappings() {
        // Validate
        LOGGER.info("Validating Database Mappings...");
        final ModelRegistry modelRegistry = settings.getModelRegistry();
        for (EntityType entityType : modelRegistry.getEntityTypes(true)) {
            final StaMainTable<?> tableForType = tableCollection.getTableForType(entityType);
            final PropertyFieldRegistry<?> pfReg = tableForType.getPropertyFieldRegistry();
            for (Property property : entityType.getPropertySet()) {
                PropertyFieldRegistry.PropertyFields<?> pf = pfReg.getSelectFieldsForProperty(property);
                if (pf == null || pf.converter == null) {
                    LOGGER.error("Property {} is not backed by table {}.", property.getName(), tableForType.getName());
                }
            }
            final IdGenerationType idGenMode = IdGenerationType.findType(persistenceSettings.getIdGenerationMode(entityType));
            entityType.setIdGenerationMode(idGenMode);
        }
    }

    private void registerModelFields(DefModel modelDefinition) {
        for (DefEntityType entityTypeDef : modelDefinition.getEntityTypes()) {
            final EntityType entityType = entityTypeDef.getEntityType(settings.getModelRegistry());
            StaMainTable typeStaTable = getOrCreateMainTable(entityType, entityTypeDef.getTable());
            for (DefEntityProperty propertyDef : entityTypeDef.getEntityProperties()) {
                registerFieldsForEntityProperty(propertyDef, typeStaTable);
            }
            for (DefNavigationProperty propertyDef : entityTypeDef.getNavigationProperties()) {
                registerFieldsForNavProperty(propertyDef, typeStaTable);
            }
        }
    }

    private void registerFieldsForEntityProperty(DefEntityProperty propertyDef, StaMainTable typeStaTable) {
        for (PropertyPersistenceMapper handler : propertyDef.getHandlers()) {
            maybeRegisterField(handler, typeStaTable);
        }
    }

    private void registerFieldsForNavProperty(DefNavigationProperty propertyDef, StaMainTable typeStaTable) {
        for (PropertyPersistenceMapper handler : propertyDef.getHandlers()) {
            maybeRegisterField(handler, typeStaTable);
        }
    }

    private void maybeRegisterField(PropertyPersistenceMapper handler, StaMainTable typeStaTable) {
        if (handler instanceof FieldMapper fieldMapper) {
            fieldMapper.registerField(this, typeStaTable);
        }
    }

    private void registerModelMappings(DefModel modelDefinition) {
        for (DefEntityType entityTypeDef : modelDefinition.getEntityTypes()) {
            final EntityType entityType = entityTypeDef.getEntityType(settings.getModelRegistry());
            final StaMainTable table = getOrCreateMainTable(entityType, entityTypeDef.getTable());
            for (DefEntityProperty propertyDef : entityTypeDef.getEntityProperties()) {
                registerMappingForEntityProperties(propertyDef, table);
            }
            for (DefNavigationProperty propertyDef : entityTypeDef.getNavigationProperties()) {
                registerMappingForNavProperties(propertyDef, table);
            }
        }
    }

    private void registerHooks(DefModel modelDefinition) {
        for (DefEntityType entityTypeDef : modelDefinition.getEntityTypes()) {
            final EntityType entityType = entityTypeDef.getEntityType(settings.getModelRegistry());
            final StaMainTable table = getOrCreateMainTable(entityType, entityTypeDef.getTable());
            for (DefPmHook hookDef : entityTypeDef.getHooks()) {
                PmHook hook = hookDef.getHook();
                if (hook instanceof HookPreInsert h) {
                    table.registerHookPreInsert(hookDef.getPriority(), h);
                }
                if (hook instanceof HookPostInsert h) {
                    table.registerHookPostInsert(hookDef.getPriority(), h);
                }
                if (hook instanceof HookPreUpdate h) {
                    table.registerHookPreUpdate(hookDef.getPriority(), h);
                }
                if (hook instanceof HookPostUpdate h) {
                    table.registerHookPostUpdate(hookDef.getPriority(), h);
                }
                if (hook instanceof HookPreDelete h) {
                    table.registerHookPreDelete(hookDef.getPriority(), h);
                }
                if (hook instanceof HookPostDelete h) {
                    table.registerHookPostDelete(hookDef.getPriority(), h);
                }
            }
        }
    }

    private void registerMappingForEntityProperties(DefEntityProperty propertyDef, StaMainTable orCreateTable) {
        for (PropertyPersistenceMapper handler : propertyDef.getHandlers()) {
            maybeRegisterMapping(handler, orCreateTable);
        }
    }

    private void registerMappingForNavProperties(DefNavigationProperty propertyDef, StaMainTable orCreateTable) {
        for (PropertyPersistenceMapper handler : propertyDef.getHandlers()) {
            maybeRegisterMapping(handler, orCreateTable);
        }
    }

    private void maybeRegisterMapping(PropertyPersistenceMapper handler, StaMainTable orCreateTable) {
        if (handler instanceof FieldMapper fieldMapper) {
            fieldMapper.registerMapping(this, orCreateTable);
        }
    }

    @Override
    public Table<?> getDbTable(String tableName) {
        return getDbTable(DSL.name(tableName));
    }

    @Override
    public Table<?> getDbTable(Name tableName) {
        return tableCache.computeIfAbsent(tableName, t -> readDbTableFromDb(tableName));
    }

    public Table<?> readDbTableFromDb(Name tableName) {
        final Meta meta = dslContext.meta();
        final List<Table<?>> tables = meta.getTables(tableName);
        if (tables.isEmpty()) {
            LOGGER.error("Table {} not found. Please initialise the database!", tableName);
            throw new IllegalArgumentException("Table " + tableName + " not found.");
        }
        if (tables.size() != 1) {
            String[] schemas = StringUtils.split(schemaPriority, ',');
            for (String schema : schemas) {
                for (Table<?> table : tables) {
                    final Schema tableSchema = table.getSchema();
                    if (tableSchema != null && schema.trim().equalsIgnoreCase(tableSchema.getName())) {
                        LOGGER.warn("Table name {} found {} times, using version from schema {}.", tableName, tables.size(), schema);
                        return table;
                    }
                }
            }
            LOGGER.error("Table name {} found {} times, none in schemas '{}'. Use setting {}.{} to specify schema priority.",
                    tableName, tables.size(), schemaPriority, PREFIX_PERSISTENCE, TAG_DB_SCHEMA_PRIORITY);
            throw new IllegalArgumentException("Failed to initialise: Table name " + tableName + " found " + tables.size() + " times.");
        }
        return tables.get(0);
    }

    private StaMainTable getOrCreateMainTable(EntityType entityType, String tableName) {
        if (entityType == null) {
            throw new IllegalArgumentException("Not implemented yet");
        }
        StaMainTable<?> table = tableCollection.getTableForType(entityType);
        if (table == null) {
            final PrimaryKey primaryKey = entityType.getPrimaryKey();
            if (primaryKey.size() > 1) {
                throw new NotImplementedException(NOT_IMPLEMENTED_MULTI_VALUE_PK);
            }
            LOGGER.info("  Registering StaTable {} ({})", tableName, entityType);
            final DataType<?> pkDataType = getDataTypeFor(primaryKey.getKeyProperty(0).getType().getName());
            StaTableDynamic newTable = new StaTableDynamic(DSL.name(tableName), entityType, pkDataType);
            tableCollection.registerTable(entityType, newTable);
            table = newTable;
        }
        return table;
    }

    @Override
    public StaLinkTableDynamic getOrCreateLinkTable(String tableName) {
        StaTable<?> table = tableCollection.getTableForName(tableName);
        if (table == null) {
            LOGGER.info("  Registering StaLinkTable {}", tableName);
            StaLinkTableDynamic newTable = new StaLinkTableDynamic(DSL.name(tableName));
            tableCollection.registerTable(newTable);
            table = newTable;
        }
        if (table instanceof StaLinkTableDynamic staLinkTableDynamic) {
            return staLinkTableDynamic;
        }
        throw new IllegalStateException("Table " + tableName + " already exists, yet is not of type StaLinkTableDynamic but " + table.getClass().getName());
    }

    @Override
    public DataType<?> getDataTypeFor(String type) {
        switch (type.toUpperCase()) {
            case "EDM.INT64":
            case VALUE_ID_TYPE_LONG:
                return SQLDataType.BIGINT;

            case "EDM.STRING":
            case VALUE_ID_TYPE_STRING:
                return SQLDataType.VARCHAR;

            case "EDM.GUID":
            case VALUE_ID_TYPE_UUID:
                return SQLDataType.UUID;

            default:
                throw new IllegalArgumentException("Unknown data type: " + type);
        }
    }

    @Override
    public void generateLiquibaseVariables(Map<String, Object> target, String entity, String type) {
        target.put("id-" + entity, type);
        target.put("idTypeLong", "BIGINT");
        switch (type) {
            case "LONG":
                target.put(ID_TYPE + entity, "BIGINT");
                break;

            case "STRING":
                target.put(ID_TYPE + entity, "VARCHAR(36)");
                target.put("defaultValueComputed-" + entity, "uuid_generate_v1mc()");
                break;

            case "UUID":
                target.put(ID_TYPE + entity, "uuid");
                target.put("defaultValueComputed-" + entity, "uuid_generate_v1mc()");
                break;
            default:
                throw new IllegalArgumentException("Unknown ID type: " + type);
        }
    }

    @Override
    public ExpressionHandler createExpressionHandler(QueryBuilder queryBuilder) {
        return new MariadbExpressionHandler(settings, queryBuilder);
    }

    @Override
    public String checkForUpgrades() {
        Map<String, Object> props = CollectionsHelper.LinkedHashMapBuilder()
                .addProperty(CHANGE_SET_NAME, "MariadbPersistenceManager")
                .build();
        return checkForUpgrades(LIQUIBASE_CHANGELOG_FILENAME, props);
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        Map<String, Object> props = CollectionsHelper.LinkedHashMapBuilder()
                .addProperty(CHANGE_SET_NAME, "MariadbPersistenceManager")
                .build();
        return doUpgrades(LIQUIBASE_CHANGELOG_FILENAME, props, out);
    }

    @Override
    public boolean hasUpdateReturning() {
        return false;
    }

}
