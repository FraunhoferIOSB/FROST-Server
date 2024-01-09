/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert.Phase.POST_RELATIONS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert.Phase.PRE_RELATIONS;

import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreDelete;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreUpdate;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory.JsonFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.Relation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ExpressionFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.SortingWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.util.ParserUtils;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.jooq.Binding;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 * @param <T> The exact type of the implementing class.
 */
public abstract class StaTableAbstract<T extends StaMainTable<T>> extends TableImpl<Record> implements StaMainTable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaTableAbstract.class.getName());
    private static final String DO_NOT_KNOW_HOW_TO_JOIN = "Do not know how to join ";
    private static final String UNKNOWN_UPDATE_LINK_EXISTING_FALSE = "Unknown update mode, don't know how to handle linkExisting=false";

    public static final String TYPE_JSONB = "\"pg_catalog\".\"jsonb\"";
    public static final String TYPE_GEOMETRY = "\"public\".\"geometry\"";

    private transient TableCollection tables;
    private transient ModelRegistry modelRegistry;
    private transient Map<String, Relation<T>> relations;
    private List<CustomField> customFields;
    protected transient PropertyFieldRegistry<T> pfReg;

    private final DataType<?> idType;

    private SecurityTableWrapper securityWrapper;

    private final transient SortedSet<SortingWrapper<Double, HookPreInsert>> hooksPreInsert;
    private final transient SortedSet<SortingWrapper<Double, HookPreUpdate>> hooksPreUpdate;
    private final transient SortedSet<SortingWrapper<Double, HookPreDelete>> hooksPreDelete;

    protected StaTableAbstract(DataType<?> idType, Name alias, StaTableAbstract<T> aliasedBase, Table updatedSql) {
        super(alias, null, updatedSql);
        this.idType = idType;
        if (aliasedBase == null) {
            pfReg = new PropertyFieldRegistry<>(getThis());
            relations = new HashMap<>();
            hooksPreInsert = new TreeSet<>();
            hooksPreUpdate = new TreeSet<>();
            hooksPreDelete = new TreeSet<>();
            customFields = new ArrayList<>();
        } else {
            init(aliasedBase.getModelRegistry(), aliasedBase.getTables());
            pfReg = new PropertyFieldRegistry<>(getThis(), aliasedBase.getPropertyFieldRegistry());
            relations = aliasedBase.relations;
            hooksPreInsert = aliasedBase.hooksPreInsert;
            hooksPreUpdate = aliasedBase.hooksPreUpdate;
            hooksPreDelete = aliasedBase.hooksPreDelete;
            customFields = aliasedBase.customFields;
        }
    }

    public DataType<?> getIdType() {
        return idType;
    }

    @Override
    public final int registerField(Name name, DataType type, Binding binding) {
        customFields.add(new CustomField(name, type, binding));
        TableField newField = createField(name, type, "", binding);
        return fieldsRow().indexOf(newField);
    }

    /**
     * Must be called directly after creating an alias of this table.
     *
     * @return this.
     */
    protected T initCustomFields() {
        for (CustomField customField : customFields) {
            createField(customField.name, customField.type, "", customField.binding);
        }
        return getThis();
    }

    @Override
    public void registerRelation(Relation<T> relation) {
        relations.put(relation.getName(), relation);
    }

    /**
     * Add a hook that runs pre-insert.
     *
     * @param priority The priority. Lower priority hooks run first. This is a
     * double to make sure it is always possible to squeeze in between two other
     * hooks.
     * @param hook The hook
     */
    @Override
    public void registerHookPreInsert(double priority, HookPreInsert hook) {
        hooksPreInsert.add(new SortingWrapper<>(priority, hook));
    }

    /**
     * Add a hook that runs pre-update.
     *
     * @param priority The priority. Lower priority hooks run first. This is a
     * double to make sure it is always possible to squeeze in between two other
     * hooks.
     * @param hook The hook
     */
    @Override
    public void registerHookPreUpdate(double priority, HookPreUpdate hook) {
        hooksPreUpdate.add(new SortingWrapper<>(priority, hook));
    }

    /**
     * Add a hook that runs pre-delete.
     *
     * @param priority The priority. Lower priority hooks run first. This is a
     * double to make sure it is always possible to squeeze in between two other
     * hooks.
     * @param hook The hook
     */
    @Override
    public void registerHookPreDelete(double priority, HookPreDelete hook) {
        hooksPreDelete.add(new SortingWrapper<>(priority, hook));
    }

    @Override
    public SecurityTableWrapper getSecurityWrapper() {
        return securityWrapper;
    }

    @Override
    public void setSecurityWrapper(SecurityTableWrapper securityWrapper) {
        if (securityWrapper == null) {
            return;
        }
        if (this.securityWrapper != null) {
            LOGGER.error("Overwriting security wrapper for table {}, old type: {}", getName(), this.securityWrapper.getClass().getName());
        }
        LOGGER.info("Applying security wrapper to table {} of type {}", getName(), securityWrapper.getClass().getName());
        this.securityWrapper = securityWrapper;
    }

    @Override
    public Relation<T> findRelation(String name) {
        Relation<T> relation = relations.get(name);
        if (relation == null) {
            throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN + name + " on " + getName() + " " + getEntityType() + " " + getClass().getName());
        }
        return relation;
    }

    @Override
    public TableRef createJoin(String name, QueryState<?> queryState, TableRef sourceRef) {
        return findRelation(name).join(getThis(), queryState, sourceRef);
    }

    @Override
    public PropertyFieldRegistry<T> getPropertyFieldRegistry() {
        if (pfReg == null) {
            pfReg = new PropertyFieldRegistry<>(getThis());
        }
        return pfReg;
    }

    @Override
    public Entity entityFromQuery(Record tuple, QueryState<T> state, DataSize dataSize) {
        Entity newEntity = new DefaultEntity(getEntityType());
        for (PropertyFields<T> sp : state.getSelectedProperties()) {
            sp.converter.convert(state.getMainTable(), tuple, newEntity, dataSize);
        }
        return newEntity;
    }

    @Override
    public boolean insertIntoDatabase(JooqPersistenceManager pm, Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException {
        final T thisTable = getThis();
        EntityFactories entityFactories = pm.getEntityFactories();
        EntityType entityType = entity.getEntityType();
        Map<Field, Object> insertFields = new HashMap<>();

        // First, run the pre-insert hooks in the PRE_RELATION fase.
        for (SortingWrapper<Double, HookPreInsert> hookWrapper : hooksPreInsert) {
            if (!hookWrapper.getObject().insertIntoDatabase(PRE_RELATIONS, pm, entity, insertFields)) {
                return false;
            }
        }

        // Second create/validate single-entity navigation links.
        // this must happen first so the second step can use these in validation
        for (NavigationPropertyMain<Entity> np : entityType.getNavigationEntities()) {
            if (entity.isSetProperty(np)) {
                Entity ne = entity.getProperty(np);
                entityFactories.entityExistsOrCreate(pm, ne, updateMode);
                PropertyFields<T> registry = pfReg.getSelectFieldsForProperty(np);
                registry.converter.convert(thisTable, entity, insertFields);
            }
        }

        // Third, run the pre-insert hooks in POST_RELATION fase.
        for (SortingWrapper<Double, HookPreInsert> hookWrapper : hooksPreInsert) {
            if (!hookWrapper.getObject().insertIntoDatabase(POST_RELATIONS, pm, entity, insertFields)) {
                return false;
            }
        }

        // Fourth, deal with the ID, user-defined or not
        entityFactories.insertUserDefinedId(pm, insertFields, this.getId(), entity);

        // Fifth, deal with the other properties
        Set<EntityPropertyMain> entityProperties = entityType.getEntityProperties();
        final EntityPropertyMain<Id> primaryKey = entityType.getPrimaryKey();
        for (EntityPropertyMain ep : entityProperties) {
            if (ep.equals(primaryKey)) {
                // EP_ID has already been dealt with above.
                continue;
            }
            if (entity.isSetProperty(ep)) {
                pfReg.getSelectFieldsForProperty(ep).converter.convert(thisTable, entity, insertFields);
            }
        }

        // Sixth, do the actual insert.
        DSLContext dslContext = pm.getDslContext();
        Object entityId = dslContext.insertInto(thisTable)
                .set(insertFields)
                .returningResult(thisTable.getId())
                .fetchOne(0);
        LOGGER.debug("Inserted {} with id = {}.", entityType, entityId);
        entity.setId(ParserUtils.idFromObject(entityId));

        // Seventh, deal with set-navigation links.
        for (NavigationPropertyMain<EntitySet> np : entityType.getNavigationSets()) {
            if (entity.isSetProperty(np)) {
                LOGGER.debug("  Linking {}", np);
                updateNavigationPropertySet(entity, entity.getProperty(np), pm, updateMode);
            }
        }

        return true;
    }

    /**
     * Links the entities in the given Set to the given Entity. Optionally
     * creates the linked entities.
     *
     * @param entity The entity to link to
     * @param linkedSet The set of entities to link to the given entity
     * @param pm The PersistenceManager to use for queries
     * @param updateMode Flag indicating the update is for a newly inserted
     * entity, and new entities can be created.
     *
     * @throws IncompleteEntityException If the given entity is not validate.
     * @throws NoSuchEntityException If the entity to be updated does not exist.
     * @throws IllegalStateException If something else goes wrong.
     */
    protected void updateNavigationPropertySet(Entity entity, EntitySet linkedSet, JooqPersistenceManager pm, UpdateMode updateMode) throws IncompleteEntityException, NoSuchEntityException {
        final NavigationPropertyEntitySet navProp = linkedSet.getNavigationProperty();
        final Relation relation = findRelation(navProp.getName());
        if (relation == null) {
            LOGGER.error("Unknown relation: {}", navProp.getName());
            throw new IllegalStateException("Unknown relation: " + navProp.getName());
        }
        // Sanity check.
        if (!updateMode.linkExisting) {
            LOGGER.error(UNKNOWN_UPDATE_LINK_EXISTING_FALSE);
            throw new IllegalStateException(UNKNOWN_UPDATE_LINK_EXISTING_FALSE);
        }
        if (updateMode.deepUpdate) {
            EntityFactories ef = pm.getEntityFactories();
            boolean admin = PrincipalExtended.getLocalPrincipal().isAdmin();
            for (Entity child : linkedSet) {
                if (ef.entityExists(pm, child, admin)) {
                    final PathElementEntity childPe = new PathElementEntity(child.getId(), child.getEntityType(), null);
                    pm.update(childPe, child, updateMode);
                }
            }
        }
        if (updateMode.createAndLinkNew) {
            EntityFactories ef = pm.getEntityFactories();
            for (Entity child : linkedSet) {
                final NavigationPropertyMain backLink = navProp.getInverse();
                if (!backLink.isEntitySet()) {
                    child.setProperty(backLink, entity);
                }
                ef.entityExistsOrCreate(pm, child, updateMode);
            }
        }

        if (updateMode.removeMissing) {
            relation.link(pm, entity, linkedSet, navProp);
        } else {
            for (Entity child : linkedSet) {
                relation.link(pm, entity, child, navProp);
            }
        }
    }

    @Override
    public EntityChangedMessage updateInDatabase(JooqPersistenceManager pm, Entity entity, Id entityId, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException {
        final T thisTable = getThis();
        EntityFactories entityFactories = pm.getEntityFactories();
        EntityType entityType = entity.getEntityType();
        Map<Field, Object> updateFields = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        for (SortingWrapper<Double, HookPreUpdate> hookWrapper : hooksPreUpdate) {
            hookWrapper.getObject().updateInDatabase(pm, entity, entityId);
        }

        for (NavigationPropertyMain<Entity> np : entityType.getNavigationEntities()) {
            if (entity.isSetProperty(np)) {
                Entity ne = entity.getProperty(np);
                if (!entityFactories.entityExists(pm, ne, PrincipalExtended.getLocalPrincipal().isAdmin())) {
                    throw new NoSuchEntityException("Linked " + ne.getEntityType() + " not found.");
                }
                PropertyFields<T> registry = pfReg.getSelectFieldsForProperty(np);
                registry.converter.convert(thisTable, entity, updateFields, message);
            }
        }

        Set<EntityPropertyMain> entityProperties = entityType.getEntityProperties();
        final EntityPropertyMain<Id> primaryKey = entityType.getPrimaryKey();
        for (EntityPropertyMain ep : entityProperties) {
            if (ep.equals(primaryKey)) {
                // EP_ID can not be changed.
                continue;
            }
            if (entity.isSetProperty(ep)) {
                pfReg.getSelectFieldsForProperty(ep).converter.convert(thisTable, entity, updateFields, message);
            }
        }

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!updateFields.isEmpty()) {
            count = dslContext.update(thisTable)
                    .set(updateFields)
                    .where(thisTable.getId().equal(entityId.getValue()))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating {} {} caused {} rows to change!", getEntityType(), entityId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        for (NavigationPropertyMain<EntitySet> np : entityType.getNavigationSets()) {
            if (entity.isSetProperty(np)) {
                updateNavigationPropertySet(entity, entity.getProperty(np), pm, updateMode);
            }
        }
        return message;
    }

    @Override
    public void delete(JooqPersistenceManager pm, Id entityId) throws NoSuchEntityException {
        for (SortingWrapper<Double, HookPreDelete> hookWrapper : hooksPreDelete) {
            hookWrapper.getObject().delete(pm, entityId);
        }

        final T thisTable = getThis();
        long count = pm.getDslContext()
                .delete(thisTable)
                .where(thisTable.getId().eq(entityId.getValue()))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("Entity of type " + getEntityType() + " with id " + entityId + " not found.");
        }
        LOGGER.debug("Deleted {} Entities of type {}", count, getEntityType());
    }

    @Override
    public EntitySet newSet() {
        return new EntitySetImpl(getEntityType());
    }

    @Override
    public abstract T as(Name as);

    @Override
    public final T as(String alias) {
        return as(DSL.name(alias));
    }

    public ModelRegistry getModelRegistry() {
        return modelRegistry;
    }

    public final TableCollection getTables() {
        return tables;
    }

    public final void init(ModelRegistry modelRegistry, TableCollection tables) {
        this.modelRegistry = modelRegistry;
        this.tables = tables;
    }

    @Override
    public PropertyFields<T> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final String epName = epCustomSelect.getMainEntityPropertyName();
        final EntityPropertyMain mainEntityProperty = getEntityType().getEntityProperty(epName);
        if (mainEntityProperty.hasCustomProperties) {
            PropertyFields<T> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);

            if (mainPropertyFields.jsonType) {
                ExpressionFactory<T> factory = mainPropertyFields.fields.get("j");
                if (factory == null) {
                    factory = mainPropertyFields.fields.values().iterator().next();
                }
                final Field mainField = factory.get(getThis());
                final JsonFieldWrapper jsonFactory = jsonFieldFromPath(mainField, epCustomSelect);
                return propertyFieldForJsonField(jsonFactory, epCustomSelect);
            } else {
                final ExpressionFactory<T> factory = mainPropertyFields.fields.get(epCustomSelect.getSubPath().get(0));
                if (factory == null) {
                    throw new IllegalArgumentException("No path: " + epCustomSelect);
                }
                final Field field = factory.get(getThis());
                return propertyFieldForCustom(field, epCustomSelect);
            }
        }
        return null;
    }

    public static JsonFieldWrapper jsonFieldFromPath(final Field mainField, final EntityPropertyCustomSelect epCustomSelect) {
        JsonFieldWrapper jsonFactory = new JsonFieldWrapper(mainField);
        for (String pathItem : epCustomSelect.getSubPath()) {
            jsonFactory.addToPath(pathItem);
        }
        return jsonFactory;
    }

    protected PropertyFields<T> propertyFieldForJsonField(final JsonFieldWrapper jsonFactory, final EntityPropertyCustomSelect epCustomSelect) {
        final Field<Object> deepField = jsonFactory.materialise().getJsonExpression();
        PropertyFields<T> pfs = new PropertyFields<>(
                epCustomSelect,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (tbl, tuple, entity, dataSize) -> {
                            final JsonValue jsonValue = JsonBinding.getConverterInstance().from(tuple.get(deepField));
                            dataSize.increase(jsonValue.getStringLength());
                            Object value = jsonValue.getValue(Utils.TYPE_OBJECT);
                            epCustomSelect.setOn(entity, value);
                        }, null, null));
        pfs.addField("1", t -> deepField);
        return pfs;
    }

    protected PropertyFields<T> propertyFieldForCustom(final Field field, final EntityPropertyCustomSelect epCustomSelect) {
        PropertyFields<T> pfs = new PropertyFields<>(
                epCustomSelect,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (tbl, tuple, entity, dataSize) -> epCustomSelect.setOn(entity, tuple.get(field)),
                        null,
                        null));
        pfs.addField("1", t -> field);
        return pfs;
    }
}
