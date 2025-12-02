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

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.NOT_IMPLEMENTED_MULTI_VALUE_PK;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.*;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory.JsonFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.*;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionHandler;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.Any;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import java.util.*;
import net.time4j.Moment;
import org.apache.commons.lang3.NotImplementedException;
import org.jooq.Condition;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for the specific implementations to form the database-dialect
 * specific SQL-Queries.
 */
public class ExpressionHelper implements ExpressionHandlers.JooqExpHlpr {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionHelper.class);

    private final CoreSettings settings;
    private final QueryBuilder queryBuilder;
    private QueryState queryState;
    private int maxCustomLinkDepth = -1;

    public ExpressionHelper(CoreSettings settings, QueryBuilder queryBuilder) {
        this.settings = settings;
        this.queryBuilder = queryBuilder;
        this.queryState = queryBuilder.getQueryState();
        final Settings experimentalSettings = settings.getExtensionSettings();
        if (experimentalSettings.getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class)) {
            maxCustomLinkDepth = experimentalSettings.getInt(CoreSettings.TAG_CUSTOM_LINKS_RECURSE_DEPTH, CoreSettings.class);
        }
    }

    @Override
    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    @Override
    public QueryState getQueryState() {
        return queryState;
    }

    @Override
    public void setQueryState(QueryState queryState) {
        this.queryState = queryState;
    }

    @Override
    public FieldWrapper handle(Expression<?> e) {
        if (!e.hasHandler()) {
            final ExpressionHandler handler = settings.getFunctionRegistry().getExpression(e.getClass()).getHandler();
            if (handler == null) {
                LOGGER.error("No handler found for {}", e);
            }
            e.setHandler(handler);
        }
        return e.handle(this);

    }

    public Condition addFilterToWhere(Expression<?> filter, Condition sqlWhere) {
        FieldWrapper filterField = handle(filter);
        if (filterField.isCondition()) {
            return sqlWhere.and(filterField.getCondition());

        }
        if (filterField instanceof FieldListWrapper listExpression) {
            for (Field expression : listExpression.getExpressions().values()) {
                if (Boolean.class.isAssignableFrom(expression.getType())) {
                    Field<Boolean> predicate = expression;
                    return sqlWhere.and(predicate);
                }
            }
        }
        LOGGER.error("Filter is not a predicate but a {}.", filterField.getClass().getName());
        throw new IllegalArgumentException("Filter is not a predicate but a " + filterField.getClass().getName());
    }

    public void addOrderbyToQuery(OrderBy orderBy, Utils.SortSelectFields orderFields) {
        FieldWrapper resultExpression = handle(orderBy.getExpression());
        if (resultExpression instanceof StaTimeIntervalWrapper timeInterval) {
            addToQuery(orderBy, timeInterval.getStart(), orderFields);
            addToQuery(orderBy, timeInterval.getEnd(), orderFields);
            return;
        }
        if (resultExpression instanceof StaDurationWrapper duration) {
            addToQuery(orderBy, duration.getDuration(), orderFields);
            return;
        }
        if (resultExpression instanceof StaDateTimeWrapper dateTime) {
            addToQuery(orderBy, dateTime.getDateTime(), orderFields);
            return;
        }
        if (resultExpression instanceof JsonFieldWrapper jfw) {
            jfw.materialise();
        }
        if (resultExpression instanceof FieldListWrapper fieldListWrapper) {
            for (Field sqlExpression : fieldListWrapper.getExpressionsForOrder().values()) {
                addToQuery(orderBy, sqlExpression, orderFields);
            }
            return;
        }
        Field field = resultExpression.getDefaultField();
        addToQuery(orderBy, field, orderFields);
    }

    public void addToQuery(OrderBy orderBy, Field field, Utils.SortSelectFields orderFields) {
        orderFields.add(field, orderBy.getType());
    }

    @Override
    public void walkPath(PathState state, int startIdx, Path path) throws IllegalArgumentException {
        for (state.curIndex = startIdx; state.curIndex < state.elements.size() && !state.finished; state.curIndex++) {
            Property element = state.elements.get(state.curIndex);
            if (element instanceof EntityPropertyCustom) {
                handleCustomProperty(state, path);

            } else if (element instanceof EntityPropertyCustomLink) {
                handleCustomProperty(state, path);

            } else if (element instanceof EntityPropertyMain entityPropertyMain) {
                handleEntityProperty(state, path, entityPropertyMain);

            } else if (element instanceof NavigationPropertyMain navigationPropertyMain) {
                handleNavigationProperty(state, path, navigationPropertyMain);
            }
        }
        if (state.finalExpression == null) {
            throw new IllegalArgumentException("Path does not end in an EntityProperty: " + path);
        }
        if (state.finalExpression instanceof Field field && Moment.class.isAssignableFrom(field.getType())) {
            Field<Moment> dateTimePath = (Field<Moment>) state.finalExpression;
            state.finalExpression = new StaDateTimeWrapper(dateTimePath);
        }
    }

    private void handleCustomProperty(PathState state, Path path) {
        if (state.finalExpression == null) {
            throw new IllegalArgumentException("CustomProperty must follow an EntityProperty: " + path);
        }
        // generate finalExpression::jsonb#>>'{x,y,z}'
        JsonFieldFactory.JsonFieldWrapper jsonFactory;
        if (state.finalExpression instanceof JsonFieldFactory.JsonFieldWrapper jsonFieldWrapper) {
            jsonFactory = jsonFieldWrapper;
        } else {
            jsonFactory = new JsonFieldFactory.JsonFieldWrapper(state.finalExpression);
        }
        for (; state.curIndex < state.elements.size(); state.curIndex++) {
            final Property property = state.elements.get(state.curIndex);
            String name = property.getName();
            if (property instanceof EntityPropertyCustomLink epcl) {
                int maxDepth = state.curIndex + maxCustomLinkDepth;
                if (state.curIndex <= maxDepth) {
                    handleCustomLink(epcl, jsonFactory, name, state);
                    return;
                } else {
                    jsonFactory.addToPath(name);
                }
            } else {
                jsonFactory.addToPath(name);
            }
        }
        state.finalExpression = jsonFactory.materialise();
        state.finished = true;
    }

    private void handleCustomLink(final EntityPropertyCustomLink epcl, JsonFieldFactory.JsonFieldWrapper jsonFactory, String name, PathState state) {
        JsonFieldFactory.JsonFieldWrapper sourceIdFieldWrapper = jsonFactory.addToPath(name + SpecialNames.AT_IOT_ID).materialise();
        state.pathTableRef = queryEntityType(epcl, state.pathTableRef, sourceIdFieldWrapper);
        state.finalExpression = null;
    }

    private void handleEntityProperty(PathState state, Path path, EntityPropertyMain element) {
        if (state.finalExpression != null) {
            throw new IllegalArgumentException("EntityProperty can not follow an other EntityProperty: " + path);
        }
        Map<String, Field> pathExpressions = state.pathTableRef.getTable()
                .getPropertyFieldRegistry()
                .getAllFieldsForProperty(element, new LinkedHashMap<>());
        if (pathExpressions.size() == 1) {
            final Field field = pathExpressions.values().stream().iterator().next();
            Field optimisedField = state.pathTableRef.getJoinEqual(field);
            state.finalExpression = WrapperHelper.wrapField(optimisedField);
        } else {
            state.finalExpression = getSubExpression(state, pathExpressions);
        }
    }

    private void handleNavigationProperty(PathState state, Path path, NavigationPropertyMain np) {
        if (state.finalExpression != null) {
            throw new IllegalArgumentException("NavigationProperty can not follow an EntityProperty: " + path);
        }
        state.pathTableRef = queryEntityType(np, state.pathTableRef);
    }

    private FieldWrapper getSubExpression(PathState state, Map<String, Field> pathExpressions) {
        int nextIdx = state.curIndex + 1;
        if (state.elements.size() > nextIdx) {
            Property subProperty = state.elements.get(nextIdx);
            // If the subProperty is unknown, and the expression can be of type JSON,
            // then we assume JSON.
            if (!pathExpressions.containsKey(subProperty.getName()) && pathExpressions.containsKey("j")) {
                return new SimpleFieldWrapper(pathExpressions.get("j"));
            }
            // We can not accept json, so the subProperty must be a known direction.
            state.finished = true;
            return WrapperHelper.wrapField(pathExpressions.get(subProperty.getName()));
        }
        if (pathExpressions.containsKey(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START)
                && pathExpressions.containsKey(StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END)) {
            return new StaTimeIntervalWrapper(pathExpressions);
        }
        return new FieldListWrapper(pathExpressions);
    }

    /**
     * Queries the given entity type, as relation to the given table reference
     * and returns a new table reference. Effectively, this generates a join.
     *
     * @param np The NavigationProperty to query
     * @param last The table the requested entity is related to.
     * @return The table reference of the requested entity.
     */
    public TableRef queryEntityType(NavigationProperty np, TableRef last) {
        if (queryState == null) {
            throw new IllegalStateException("QueryState should not be null");
        }
        if (last == null) {
            throw new IllegalStateException("last result should not be null");
        }

        TableRef existingJoin = last.getJoin(np);
        if (existingJoin != null) {
            return existingJoin;
        }

        return last.createJoin(np.getName(), queryState);
    }

    /**
     * Directly query an entity type. Used for custom linking.
     *
     * @param epcl the custom link.
     * @param sourceRef The source table ref.
     * @param sourceIdFieldWrapper The source ID FieldWrapper.
     * @return A new table ref with the target entity type table joined.
     */
    public TableRef queryEntityType(EntityPropertyCustomLink epcl, TableRef sourceRef, JsonFieldFactory.JsonFieldWrapper sourceIdFieldWrapper) {
        final EntityType targetEntityType = epcl.getEntityType();
        final StaMainTable<?> target = queryBuilder.getTableCollection().getTablesByType().get(targetEntityType);
        final StaMainTable<?> targetAliased = target.asSecure(queryState.getNextAlias(), queryBuilder.getPersistenceManager());
        final List<Field> targetField = targetAliased.getPkFields();
        if (targetField.size() > 1) {
            throw new NotImplementedException(NOT_IMPLEMENTED_MULTI_VALUE_PK);
        }
        Field<Object> targetConverted = sourceIdFieldWrapper.otherToJson(targetField.get(0));
        queryState.setSqlFrom(queryState.getSqlFrom().leftJoin(targetAliased).on(targetConverted.eq(sourceIdFieldWrapper.getJsonExpression())));
        TableRef newRef = new TableRef(targetAliased);
        sourceRef.addJoin(epcl, newRef);
        return newRef;
    }

    @Override
    public Field[] findPair(FieldWrapper p1, FieldWrapper p2) {
        Field[] result = new Field[2];

        result[0] = p1.getFieldAsType(Number.class, true);
        result[1] = p2.getFieldAsType(Number.class, true);
        if (result[0] != null && result[1] != null) {
            return result;
        }

        result[0] = p1.getFieldAsType(Boolean.class, true);
        result[1] = p2.getFieldAsType(Boolean.class, true);
        if (result[0] != null && result[1] != null) {
            return result;
        }

        // If both are strings, use strings.
        result[0] = p1.getFieldAsType(String.class, true);
        result[1] = p2.getFieldAsType(String.class, true);
        if (result[0] != null && result[1] != null) {
            return result;
        }

        result[0] = p1.getDefaultField();
        result[1] = p2.getDefaultField();
        if (!result[0].getDataType().equals(result[1].getDataType())) {
            LOGGER.warn("Could not match types for {} and {}", p1, p2);
        }
        return result;
    }

    @Override
    public QueryState walkAnyPath(final QueryState<?> parentQueryState, Any node, final TableCollection tc) throws IllegalArgumentException {
        final StaMainTable parentMainTable = parentQueryState.getMainTable();
        final EntityType parentEntityType = parentMainTable.getEntityType();
        final Path path = node.getCollection();
        final List<Property> elements = path.getElements();
        QueryState existsQueryState = null;
        TableRef lastJoin = null;
        Property firstElement = elements.get(0);
        int startIdx = 0;
        if (firstElement instanceof PropertyReference) {
            startIdx = 1;
        }
        for (int idx = elements.size() - 1; idx >= startIdx; idx--) {
            Property element = elements.get(idx);
            if ((lastJoin == null)) {
                if (element instanceof NavigationPropertyMain.NavigationPropertyEntitySet npes) {
                    // Last entry in the path: the target collection.
                    EntityType finalType = npes.getEntityType();
                    final StaMainTable<?> tableForType = tc.getTableForType(finalType).asSecure(parentQueryState.getNextAlias(), parentQueryState.getPersistenceManager());
                    existsQueryState = new QueryState(tableForType, parentQueryState, node.getLambdaName());
                    lastJoin = existsQueryState.getTableRef();
                } else {
                    throw new IllegalArgumentException("Path before any() MUST end in an EntitySet. Found: " + element);
                }
            }
            if (element instanceof NavigationPropertyMain npm) {
                var inverse = npm.getInverse();
                if (idx == startIdx) {
                    // First entry in the path: Link to the main table!
                    if (inverse.getEntityType() != parentEntityType) {
                        throw new IllegalArgumentException("path of any() did not track back to main entity type. Expected " + parentEntityType + " got " + inverse.getEntityType());
                    }
                    lastJoin.createSemiJoin(inverse.getName(), parentMainTable, existsQueryState);

                } else {
                    TableRef existingJoin = lastJoin.getJoin(inverse);
                    if (existingJoin != null) {
                        lastJoin = existingJoin;
                    }
                    lastJoin = lastJoin.createJoin(inverse.getName(), existsQueryState);
                }

            } else if (element instanceof EntityPropertyCustomLink) {
                throw new IllegalArgumentException("Path before any() should not contain Custom Links. Found: " + element);
            } else if (element instanceof EntityPropertyCustom) {
                throw new IllegalArgumentException("Path before any() should not contain EntityProperties. Found: " + element);
            } else if (element instanceof EntityPropertyMain) {
                throw new IllegalArgumentException("Path before any() should not contain EntityProperties. Found: " + element);
            } else {
                throw new IllegalArgumentException("Path before any() contains unknown element. Found: " + element);
            }
        }
        return existsQueryState;
    }

}
