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
package de.fraunhofer.iosb.ilt.frostserver.query;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
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

    private static final Set<EntityPropertyMain> refSelect = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ModelRegistry.EP_SELFLINK)));

    private final QueryDefaults settings;
    private final ModelRegistry modelRegistry;
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
    private List<Expand> expand;
    private List<OrderBy> orderBy;
    private String format;
    private Metadata metadata = Metadata.DEFAULT;

    public Query(ModelRegistry modelRegistry, QueryDefaults settings, ResourcePath path) {
        this.modelRegistry = modelRegistry;
        this.path = path;
        this.settings = settings;
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
        validate(pathEntityType);
        return this;
    }

    public Query validate(EntityType entityType) {
        if (this.entityType == null) {
            this.entityType = entityType;
        }
        selectEntityPropMain = null;
        for (PropertyPlaceholder pp : rawSelect) {
            Property property = entityType.getProperty(pp.getName());
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
            e.validate(entityType);
        }
        reNestExpands();

        if (filter != null) {
            filter.validate(modelRegistry.getParserHelper(), entityType);
        }
        for (OrderBy order : orderBy) {
            order.getExpression().validate(modelRegistry.getParserHelper(), entityType);
        }
        return this;
    }

    public Version getVersion() {
        return path.getVersion();
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

    public Query addSelect(Collection<PropertyPlaceholder> properties) {
        if (!select.isEmpty()) {
            throw new IllegalStateException("Either add PropertyPlaceholder or Property instances, not both.");
        }
        rawSelect.addAll(properties);
        return this;
    }

    public Query addSelect(Property property) {
        if (!rawSelect.isEmpty()) {
            throw new IllegalStateException("Either add PropertyPlaceholder or Property instances, not both.");
        }
        select.add(property);
        return this;
    }

    public Query addSelect(Property... properties) {
        select.addAll(Arrays.asList(properties));
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
        if (path.isRef()) {
            selectEntityPropMain = refSelect;
            selectNavProp = new HashSet<>();
            return refSelect;
        }
        Set<EntityPropertyMain> selectedEntityPropMain = new HashSet<>();
        selectNavProp = new HashSet<>();
        if (select.isEmpty()) {
            if (entityType == null) {
                validate();
            }
            selectedEntityPropMain.addAll(entityType.getEntityProperties());
            if (!inExpand) {
                selectNavProp.addAll(entityType.getNavigationEntities());
                selectNavProp.addAll(entityType.getNavigationSets());
            }
        } else {
            for (Property s : select) {
                if (s instanceof EntityPropertyMain) {
                    selectedEntityPropMain.add((EntityPropertyMain) s);
                } else if (s instanceof EntityPropertyCustomSelect) {
                    EntityPropertyCustomSelect epcs = (EntityPropertyCustomSelect) s;
                    selectedEntityPropMain.add(entityType.getEntityProperty(epcs.getMainEntityPropertyName()));
                } else if (s instanceof NavigationPropertyMain) {
                    selectNavProp.add((NavigationPropertyMain) s);
                }
            }
        }
        selectEntityPropMain = selectedEntityPropMain;
        return selectedEntityPropMain;
    }

    public Expression getFilter() {
        return filter;
    }

    public String getFormat() {
        return format;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<Expand> getExpand() {
        return expand;
    }

    public List<OrderBy> getOrderBy() {
        return orderBy;
    }

    public void setTop(int top) {
        if (top <= settings.getTopMax()) {
            this.top = Optional.of(top);
        } else {
            this.top = Optional.of(settings.getTopMax());
        }
    }

    public void setSkip(int skip) {
        this.skip = Optional.of(skip);
    }

    public void setCount(boolean count) {
        this.count = Optional.of(count);
    }

    public void setFilter(Expression filter) {
        this.filter = filter;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setMetadata(Metadata metadata) {
        Objects.requireNonNull(metadata);
        this.metadata = metadata;
    }

    public void setExpand(List<Expand> expand) {
        this.expand = expand;
        for (Expand e : expand) {
            e.setParentQuery(this);
        }
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

    public void setOrderBy(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, skip, count, select, filter, format, expand, orderBy, path, selectDistinct);
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
                && Objects.equals(this.format, other.format)
                && Objects.equals(this.expand, other.expand)
                && Objects.equals(this.orderBy, other.orderBy)
                && Objects.equals(this.path, other.path);
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

}
