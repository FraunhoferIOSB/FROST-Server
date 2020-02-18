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

import de.fraunhofer.iosb.ilt.frostserver.path.CustomPropertyPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import de.fraunhofer.iosb.ilt.frostserver.path.PropertyPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author jab
 */
public class Query {

    private CoreSettings settings;
    private Optional<Integer> top;
    private Optional<Integer> skip;
    private Optional<Boolean> count;
    private Set<Property> select;
    private Expression filter;
    private List<Expand> expand;
    private List<OrderBy> orderBy;
    private String format;

    public Query() {
        this(new CoreSettings());
    }

    public Query(CoreSettings settings) {
        this.settings = settings;
        this.top = Optional.empty();
        this.skip = Optional.empty();
        this.count = Optional.empty();
        this.orderBy = new ArrayList<>();
        this.expand = new ArrayList<>();
        this.select = new LinkedHashSet<>();
    }

    public void validate(ResourcePath path) {
        ResourcePathElement mainElement = path.getMainElement();
        if (mainElement instanceof PropertyPathElement || mainElement instanceof CustomPropertyPathElement) {
            throw new IllegalArgumentException("No queries allowed for property paths.");
        }
        EntityType entityType = path.getMainElementType();
        if (entityType == null) {
            throw new IllegalStateException("Unkown ResourcePathElementType found.");
        }
        validate(entityType);
    }

    protected void validate(EntityType entityType) {
        Set<Property> propertySet = entityType.getPropertySet();
        Optional<Property> invalidProperty = select.stream().filter(x -> !propertySet.contains(x)).findAny();
        if (invalidProperty.isPresent()) {
            throw new IllegalArgumentException("Invalid property '" + invalidProperty.get().getName() + "' found for entity type " + entityType.entityName);
        }
        expand.forEach(x -> x.validate(entityType));
    }

    public CoreSettings getSettings() {
        return settings;
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

    public void setSelect(Set<Property> select) {
        this.select = select;
    }

    public void setSelect(List<Property> select) {
        this.select.clear();
        this.select.addAll(select);
    }

    public Set<Property> getSelect() {
        return select;
    }

    public Expression getFilter() {
        return filter;
    }

    public String getFormat() {
        return format;
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

    public void setExpand(List<Expand> expand) {
        this.expand = expand;
    }

    public void addExpand(Expand expand) {
        this.expand.add(expand);
    }

    public void setOrderBy(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, skip, count, select, filter, format, expand, orderBy);
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
                && Objects.equals(this.filter, other.filter)
                && Objects.equals(this.format, other.format)
                && Objects.equals(this.expand, other.expand)
                && Objects.equals(this.orderBy, other.orderBy);
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

        addSelectToUrl(sb, separator);

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
            if (!inExpand) {
                filterUrl = StringHelper.urlEncode(filterUrl);
            }
            sb.append(filterUrl);
        }
    }

    private void addSelectToUrl(StringBuilder sb, char separator) {
        if (!select.isEmpty()) {
            sb.append(separator).append("$select=");
            boolean firstDone = false;
            for (Property property : select) {
                if (firstDone) {
                    sb.append(",");
                } else {
                    firstDone = true;
                }
                sb.append(StringHelper.urlEncode(property.getName()));
            }
        }
    }

}
