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
package de.fraunhofer.iosb.ilt.frostserver.query;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PrimaryKey;
import de.fraunhofer.iosb.ilt.frostserver.path.ParserContext;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementCustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author jab
 * @author scf
 */
public class Query {

    private static final String ERROR_ADD_PLACEHOLDER_OR_PROPERTY = "Either add PropertyPlaceholder or Property instances, not both.";

    private static final Set<EntityPropertyMain> refSelect = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ModelRegistry.EP_SELFLINK)));

    private final QueryDefaults settings;
    private final ModelRegistry modelRegistry;
    private final PrincipalExtended principal;
    private ResourcePath path;
    private Expand parentExpand;
    private EntityType entityType;
    private Set<EntityPropertyMain> selectEntityPropMain;
    private Set<NavigationPropertyMain> selectNavProp;

    private Optional<Integer> top;
    private Optional<Integer> skip;
    private Optional<Boolean> count;
    private final List<PropertyPlaceholder> rawSelect;
    private final Set<Property> select;
    private boolean selectDistinct = false;
    private Expression filter;
    private Expression skipFilter;
    private List<Expand> expand;
    private List<OrderBy> orderBy;
    private String id;

    private boolean pkOrder = false;
    private String format;
    private Metadata metadata;

    /**
     * Create a Query for an anonymous user, with the given model registry,
     * settings and path.
     *
     * @param modelRegistry the model registry to use.
     * @param settings the setting to use.
     * @param path the path the query is for.
     */
    public Query(ModelRegistry modelRegistry, QueryDefaults settings, ResourcePath path) {
        this(modelRegistry, settings, path, PrincipalExtended.ANONYMOUS_PRINCIPAL);
    }

    /**
     * Create a new Query using the modelRegistry, Settings, Path and Principal
     * form the given query.
     *
     * @param expandParent The parent query to use as a basis.
     */
    public Query(Query expandParent) {
        this(expandParent.getModelRegistry(), expandParent.getSettings(), expandParent.getPath(), expandParent.getPrincipal());
    }

    /**
     * Create a Query with the given model registry, settings, path and user.
     *
     * @param modelRegistry the model registry to use.
     * @param settings the setting to use.
     * @param path the path the query is for.
     * @param principal the user principal.
     */
    public Query(ModelRegistry modelRegistry, QueryDefaults settings, ResourcePath path, PrincipalExtended principal) {
        this.modelRegistry = modelRegistry;
        this.path = path;
        this.settings = settings;
        this.principal = principal;
        this.top = Optional.empty();
        this.skip = Optional.empty();
        this.count = Optional.empty();
        this.orderBy = new ArrayList<>();
        this.expand = new ArrayList<>();
        this.rawSelect = new ArrayList<>();
        this.select = new LinkedHashSet<>();
    }

    public boolean isEmpty() {
        return top.isEmpty() && skip.isEmpty() && count.isEmpty() && select.isEmpty() && expand.isEmpty() && filter == null;
    }

    public Query validate() {
        PathElement mainElement = path.getMainElement();
        if (mainElement instanceof PathElementProperty || mainElement instanceof PathElementCustomProperty) {
            throw new IllegalArgumentException("No queries allowed for property paths.");
        }
        EntityType pathEntityType = path.getMainElementType();
        if (pathEntityType == null) {
            throw new IllegalStateException("Unkown ResourcePathElementType found.");
        }
        validate(null, pathEntityType);
        return this;
    }

    public Query validate(EntityType entityType) {
        return validate(null, entityType);
    }

    public Query validate(ParserContext context, EntityType entityType) {
        if (context == null) {
            context = new ParserContext(modelRegistry);
        }
        if (this.entityType == null) {
            this.entityType = entityType;
        }
        selectEntityPropMain = null;
        for (PropertyPlaceholder pp : rawSelect) {
            Property property = entityType.getProperty(pp.getName());
            if (property == null) {
                property = path.getVersion().syntheticPropertyRegistry.getProperty(pp.getName());
            }
            if (property == null) {
                throw new IllegalArgumentException("Invalid property '" + pp.getName() + "' found in select, for entity type " + entityType.entityName);
            }
            if (property instanceof NavigationProperty) {
                select.add(property);
            } else if (property instanceof EntityPropertyMain) {
                if (pp.hasSubPath()) {
                    select.add(new EntityPropertyCustomSelect(property.getName()).addToSubPath(pp.getSubPath()));
                } else {
                    select.add(property);
                }
            }
        }

        for (Expand e : expand) {
            e.validate(context, entityType);
        }
        reNestExpands();

        if (filter != null) {
            filter.validate(context, entityType);
        }
        if (skipFilter != null) {
            skipFilter.validate(context, entityType);
        }
        PrimaryKey primaryKey = entityType.getPrimaryKey();
        int pkCount = 0;
        for (var keyProp : primaryKey.getKeyProperties()) {
            final String pkName = keyProp.getName();
            for (OrderBy order : orderBy) {
                order.getExpression().validate(context, entityType);
                if (pkName.equals(order.getExpression().toUrl())) {
                    pkCount++;
                }
            }
        }
        pkOrder = pkCount >= primaryKey.getKeyProperties().size();
        if (settings.isAlwaysOrder() && !pkOrder && !selectDistinct && !path.isEntityProperty()) {
            for (OrderBy dfltOrder : entityType.getOrderbyDefaults()) {
                boolean found = false;
                for (OrderBy order : orderBy) {
                    if (order.getExpression().toUrl().equalsIgnoreCase(dfltOrder.getExpression().toUrl())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    orderBy.add(dfltOrder);
                }
            }
            pkOrder = true;
        }
        return this;
    }

    public Version getVersion() {
        return path.getVersion();
    }

    public ModelRegistry getModelRegistry() {
        return modelRegistry;
    }

    public QueryDefaults getSettings() {
        return settings;
    }

    public ResourcePath getPath() {
        return path;
    }

    public void setPath(ResourcePath path) {
        this.path = path;
    }

    public String getServiceRootUrl() {
        return path.getServiceRootUrl();
    }

    public boolean hasParentExpand() {
        return parentExpand != null;
    }

    public Expand getParentExpand() {
        return parentExpand;
    }

    public void setParentExpand(Expand parentExpand) {
        this.parentExpand = parentExpand;
    }

    public Optional<Integer> getTop() {
        return top;
    }

    public int getTopOrDefault() {
        if (top.isPresent()) {
            return top.get();
        }
        return settings.getTopDefault();
    }

    public Optional<Integer> getSkip() {
        return skip;
    }

    public int getSkip(int dflt) {
        if (skip.isPresent()) {
            return skip.get();
        }
        return dflt;
    }

    public Optional<Boolean> getCount() {
        return count;
    }

    public boolean isCountOrDefault() {
        if (count.isPresent()) {
            return count.get();
        }
        return settings.isCountDefault();
    }

    public Query clearSelect() {
        select.clear();
        return this;
    }

    public Query addSelect(PropertyPlaceholder property) {
        if (!select.isEmpty()) {
            throw new IllegalStateException(ERROR_ADD_PLACEHOLDER_OR_PROPERTY);
        }
        rawSelect.add(property);
        return this;
    }

    public Query addSelect(Collection<PropertyPlaceholder> properties) {
        if (!select.isEmpty()) {
            throw new IllegalStateException(ERROR_ADD_PLACEHOLDER_OR_PROPERTY);
        }
        rawSelect.addAll(properties);
        return this;
    }

    public Query addSelect(Property property) {
        if (!rawSelect.isEmpty()) {
            throw new IllegalStateException(ERROR_ADD_PLACEHOLDER_OR_PROPERTY);
        }
        select.add(property);
        return this;
    }

    public Query addSelect(Property... properties) {
        select.addAll(Arrays.asList(properties));
        return this;
    }

    public Query addSelect(List<EntityPropertyMain> properties) {
        select.addAll(properties);
        return this;
    }

    public Set<Property> getSelect() {
        return select;
    }

    public void setSelectDistinct(boolean selectDistinct) {
        this.selectDistinct = selectDistinct;
    }

    public boolean isSelectDistinct() {
        return selectDistinct;
    }

    /**
     * @param inExpand flag indicating the requested properties are used in an
     * expand.
     * @return The direct (non-deep) entity properties involved in the select.
     */
    public Set<EntityPropertyMain> getSelectMainEntityProperties(boolean inExpand) {
        if (selectEntityPropMain == null) {
            return initSelectedProperties(inExpand);
        }
        return selectEntityPropMain;
    }

    /**
     * @param inExpand flag indicating the requested properties are used in an
     * expand.
     * @return The direct (non-deep) entity properties involved in the select.
     */
    public Set<NavigationPropertyMain> getSelectNavProperties(boolean inExpand) {
        if (selectNavProp == null) {
            initSelectedProperties(inExpand);
        }
        return selectNavProp;
    }

    private Set<EntityPropertyMain> initSelectedProperties(boolean inExpand) {
        if (path != null && path.isRef()) {
            selectEntityPropMain = refSelect;
            selectNavProp = new HashSet<>();
            return refSelect;
        }
        Set<EntityPropertyMain> selectedEntityPropMain = new LinkedHashSet<>();
        selectNavProp = new LinkedHashSet<>();
        if (select.isEmpty()) {
            if (entityType == null) {
                validate();
            }
            if (getMetadata() == Metadata.FULL) {
                selectedEntityPropMain.add(ModelRegistry.EP_SELFLINK);
            }
            selectedEntityPropMain.addAll(entityType.getEntityProperties());
            if (!inExpand) {
                for (NavigationPropertyMain<Entity> np : entityType.getNavigationEntities()) {
                    if (!np.isAdminOnly() || principal.isAdmin()) {
                        selectNavProp.add(np);
                    }
                }
                for (NavigationPropertyMain<EntitySet> np : entityType.getNavigationSets()) {
                    if (!np.isAdminOnly() || principal.isAdmin()) {
                        selectNavProp.add(np);
                    }
                }
            }
        } else {
            for (Property s : select) {
                if (s instanceof EntityPropertyMain epm) {
                    selectedEntityPropMain.add(epm);
                } else if (s instanceof EntityPropertyCustomSelect epcs) {
                    selectedEntityPropMain.add(entityType.getEntityProperty(epcs.getMainEntityPropertyName()));
                } else if (s instanceof NavigationPropertyMain np && (!np.isAdminOnly() || principal.isAdmin())) {
                    selectNavProp.add(np);
                }
            }
        }
        selectEntityPropMain = selectedEntityPropMain;
        return selectedEntityPropMain;
    }

    public Expression getFilter() {
        return filter;
    }

    public Query setFilter(Expression filter) {
        this.filter = filter;
        return this;
    }

    public Expression getSkipFilter() {
        return skipFilter;
    }

    public void setSkipFilter(Expression skipFilter) {
        this.skipFilter = skipFilter;
    }

    public String getFormat() {
        return format;
    }

    public Metadata getMetadata() {
        if (metadata == null) {
            return Metadata.DEFAULT;
        }
        return metadata;
    }

    public boolean hasMetadata() {
        return metadata != null;
    }

    public List<Expand> getExpand() {
        return expand;
    }

    public List<OrderBy> getOrderBy() {
        return orderBy;
    }

    /**
     * Check if this Query is ordered by the primary key of the entity type.
     *
     * @return true if this Query is ordered by the primary key.
     */
    public boolean isPkOrder() {
        return pkOrder;
    }

    public Query setTop(int top) {
        if (top <= settings.getTopMax()) {
            this.top = Optional.of(top);
        } else {
            this.top = Optional.of(settings.getTopMax());
        }
        return this;
    }

    public Query setSkip(int skip) {
        this.skip = Optional.of(skip);
        return this;
    }

    public Query setCount(boolean count) {
        this.count = Optional.of(count);
        return this;
    }

    public Query setFormat(String format) {
        this.format = format;
        return this;
    }

    public Query setMetadata(Metadata metadata) {
        Objects.requireNonNull(metadata);
        this.metadata = metadata;
        return this;
    }

    public Query setExpand(List<Expand> expand) {
        this.expand = expand;
        for (Expand e : expand) {
            e.setParentQuery(this);
        }
        return this;
    }

    public Query addExpand(Expand expand) {
        this.expand.add(expand);
        expand.setParentQuery(this);
        return this;
    }

    private void addExpand(List<Expand> expands) {
        this.expand.addAll(expands);
        for (Expand e : expands) {
            e.setParentQuery(this);
        }
    }

    /**
     * Properly nests the expands. Removes duplicates.
     */
    public void reNestExpands() {
        List<Expand> newExpands = new ArrayList<>();
        Map<String, Expand> expandMap = new HashMap<>();
        for (Expand oldExpand : expand) {
            List<String> rawPath = oldExpand.getRawPath();
            final String first = rawPath.get(0);
            final int rawCount = rawPath.size();
            if (rawCount == 1 && expandMap.containsKey(first)) {
                Expand existing = expandMap.get(first);
                existing.getSubQuery().addExpand(oldExpand.getSubQuery().getExpand());
                existing.getSubQuery().reNestExpands();
            } else {
                newExpands.add(oldExpand);
                if (rawPath.size() == 1) {
                    expandMap.put(first, oldExpand);
                }
            }
        }
        expand.clear();
        expand.addAll(newExpands);
    }

    public Query addOrderBy(OrderBy orderBy) {
        if (this.orderBy == null) {
            this.orderBy = new ArrayList<>();
        }
        this.orderBy.add(orderBy);
        return this;
    }

    public Query setOrderBy(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, skip, count, select, filter, skipFilter, format, expand, orderBy, path, selectDistinct, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Query other = (Query) obj;
        return Objects.equals(this.count, other.count)
                && Objects.equals(this.top, other.top)
                && Objects.equals(this.skip, other.skip)
                && Objects.equals(this.select, other.select)
                && Objects.equals(this.selectDistinct, other.selectDistinct)
                && Objects.equals(this.filter, other.filter)
                && Objects.equals(this.skipFilter, other.skipFilter)
                && Objects.equals(this.format, other.format)
                && Objects.equals(this.expand, other.expand)
                && Objects.equals(this.orderBy, other.orderBy)
                && Objects.equals(this.path, other.path)
                && Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean inExpand) {
        char separator = inExpand ? ';' : '&';

        StringBuilder sb = new StringBuilder();

        addTopToUrl(sb, separator);

        addSkipToUrl(sb, separator);

        addSelectToUrl(sb, separator, inExpand);

        addFilterToUrl(sb, separator, inExpand);

        addFormatToUrl(sb, separator);

        addExpandToUrl(sb, separator, inExpand);

        addOrderbyToUrl(sb, separator, inExpand);

        addCountToUrl(sb, separator);

        if (sb.length() > 0) {
            return sb.substring(1);
        }
        return "";
    }

    private void addCountToUrl(StringBuilder sb, char separator) {
        if (count.isPresent()) {
            sb.append(separator).append("$count=").append(count.get());
        }
    }

    private void addFormatToUrl(StringBuilder sb, char separator) {
        if (format != null) {
            sb.append(separator).append("$resultFormat=").append(StringHelper.urlEncode(format));
        }
    }

    private void addSkipToUrl(StringBuilder sb, char separator) {
        if (skip.isPresent()) {
            sb.append(separator).append("$skip=").append(skip.get());
        }
    }

    private void addTopToUrl(StringBuilder sb, char separator) {
        if (top.isPresent()) {
            sb.append(separator).append("$top=").append(top.get());
        }
    }

    private void addOrderbyToUrl(StringBuilder sb, char separator, boolean inExpand) {
        if (!orderBy.isEmpty()) {
            sb.append(separator).append("$orderby=");
            boolean firstDone = false;
            for (OrderBy ob : orderBy) {
                if (firstDone) {
                    sb.append(",");
                } else {
                    firstDone = true;
                }
                String orderUrl = ob.toString();
                if (!inExpand) {
                    orderUrl = StringHelper.urlEncode(orderUrl);
                }
                sb.append(orderUrl);
            }
        }
    }

    private void addExpandToUrl(StringBuilder sb, char separator, boolean inExpand) {
        if (!expand.isEmpty()) {
            sb.append(separator).append("$expand=");
            boolean firstDone = false;
            for (Expand e : expand) {
                if (firstDone) {
                    sb.append(",");
                } else {
                    firstDone = true;
                }
                String expandUrl = e.toString();
                if (!inExpand) {
                    expandUrl = StringHelper.urlEncode(expandUrl);
                }
                sb.append(expandUrl);
            }
        }
    }

    private void addFilterToUrl(StringBuilder sb, char separator, boolean inExpand) {
        if (filter != null) {
            sb.append(separator).append("$filter=");
            String filterUrl = filter.toUrl();
            if (inExpand) {
                sb.append(filterUrl);
            } else {
                sb.append(StringHelper.urlEncode(filterUrl));
            }
        }
    }

    private void addSelectToUrl(StringBuilder sb, char separator, boolean inExpand) {
        if (!select.isEmpty()) {
            sb.append(separator).append("$select=");
            if (isSelectDistinct()) {
                sb.append("distinct:");
            }
            boolean firstDone = false;
            for (Property property : select) {
                if (firstDone) {
                    sb.append(",");
                } else {
                    firstDone = true;
                }
                if (inExpand) {
                    sb.append(property.getName());
                } else {
                    sb.append(StringHelper.urlEncode(property.getName()));
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PrincipalExtended getPrincipal() {
        return principal;
    }

}
