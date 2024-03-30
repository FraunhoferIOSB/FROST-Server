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
package de.fraunhofer.iosb.ilt.statests.util.model;

import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Hylke van der Schaaf
 */
public class Query {

    /**
     * The value of the count option, or null if not set.
     */
    private Boolean count;
    /**
     * The value of the top option, or null if not set.
     */
    private Long top;
    /**
     * The value of the skip option, or null if not set.
     */
    private Long skip;
    /**
     * The value of the expand option, empty if not set.
     */
    private final List<Expand> expand = new ArrayList<>();
    /**
     * The value of the select option, empty if not set.
     */
    private final List<String> select = new ArrayList<>();
    /**
     * The value of the orderBy option, or null if not set.
     */
    private String orderBy;
    /**
     * The value of the filter option, or null if not set.
     */
    private String filter;
    /**
     * The expand this query is part of.
     */
    private Expand parent;

    public void setParent(Expand parent) {
        this.parent = parent;
    }

    /**
     * creates the url version of the query.
     *
     * @param inExpand flag to indicate the query is part of an Expand option.
     * @return the Query as a string to be used in a URL.
     */
    public String toString(boolean inExpand) {
        char separator = inExpand ? ';' : '&';
        boolean isCollection = parent.isCollection();

        StringBuilder sb = new StringBuilder();
        addSkipTopToString(isCollection, sb, separator);
        addSelectToString(sb, separator);
        addFilterToString(isCollection, sb, separator, inExpand);
        addExpandToString(sb, separator);
        if (orderBy != null && isCollection) {
            sb.append(separator).append("$orderby=").append(orderBy);
        }
        if (count != null && isCollection) {
            sb.append(separator).append("$count=").append(count);
        }
        if (sb.length() > 0) {
            return sb.substring(1);
        }
        return "";
    }

    private void addSkipTopToString(boolean isCollection, StringBuilder sb, char separator) {
        if (top != null && isCollection) {
            sb.append(separator).append("$top=").append(top);
        }
        if (skip != null && isCollection) {
            sb.append(separator).append("$skip=").append(skip);
        }
    }

    private void addSelectToString(StringBuilder sb, char separator) {
        if (!select.isEmpty()) {
            sb.append(separator).append("$select=");
            boolean firstDone = false;
            for (String property : select) {
                if (firstDone) {
                    sb.append(",");
                } else {
                    firstDone = true;
                }
                sb.append(property);
            }
        }
    }

    private void addFilterToString(boolean isCollection, StringBuilder sb, char separator, boolean inExpand) {
        if (filter != null && isCollection) {
            sb.append(separator).append("$filter=");
            String filterString = filter;
            if (!inExpand) {
                filterString = Utils.urlEncode(filterString);
            }
            sb.append(filterString);
        }
    }

    private void addExpandToString(StringBuilder sb, char separator) {
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
                sb.append(expandUrl);
            }
        }
    }

    /**
     * Properly nests the expands. Removes duplicates.
     */
    public void reNestExpands() {
        List<Expand> newExpands = new ArrayList<>();
        Map<EntityType, Expand> expandMap = new EnumMap<>(EntityType.class);
        for (Expand oldExpand : expand) {
            Expand reNest = oldExpand.reNest();
            EntityType entityType = reNest.getEntityType();
            if (expandMap.containsKey(entityType)) {
                Expand existing = expandMap.get(entityType);
                existing.getQuery().addExpand(reNest.getQuery().getExpand());
                existing.getQuery().reNestExpands();
            } else {
                newExpands.add(reNest);
                expandMap.put(entityType, reNest);
            }
        }
        expand.clear();
        expand.addAll(newExpands);
    }

    public boolean isEmpty() {
        return count == null
                && top == null
                && skip == null
                && expand.isEmpty()
                && select.isEmpty()
                && orderBy == null
                && filter == null;
    }

    /**
     * The value of the count option, or null if not set.
     *
     * @return the count
     */
    public Boolean getCount() {
        return count;
    }

    /**
     * The value of the count option, or null if not set.
     *
     * @param count the count to set
     * @return this Query;
     */
    public Query setCount(Boolean count) {
        this.count = count;
        return this;
    }

    /**
     * The value of the top option, or null if not set.
     *
     * @return the top
     */
    public Long getTop() {
        return top;
    }

    /**
     * Set the value of the top option.
     *
     * @param top the top to set
     * @return this Query;
     */
    public Query setTop(Long top) {
        this.top = top;
        return this;
    }

    /**
     * The value of the skip option, or null if not set.
     *
     * @return the skip
     */
    public Long getSkip() {
        return skip;
    }

    /**
     * Set the value of the skip option.
     *
     * @param skip the skip to set
     * @return this Query;
     */
    public Query setSkip(Long skip) {
        this.skip = skip;
        return this;
    }

    /**
     * The value of the expand option, empty if not set.
     *
     * @return the expand
     */
    public List<Expand> getExpand() {
        return expand;
    }

    /**
     * Add an expand option.
     *
     * @param expand the expand to add.
     * @return this Query;
     */
    public Query addExpand(Expand expand) {
        this.expand.add(expand);
        return this;
    }

    /**
     * Add a list of expand options.
     *
     * @param expand the expands to add
     * @return this Query;
     */
    public Query addExpand(List<Expand> expand) {
        this.expand.addAll(expand);
        return this;
    }

    /**
     * The value of the select option, empty if not set.
     *
     * @return the select
     */
    public List<String> getSelect() {
        return select;
    }

    /**
     * The value of the select option, empty if not set.
     *
     * @param select the select to add
     * @return this Query;
     */
    public Query addSelect(String select) {
        this.select.add(select);
        return this;
    }

    /**
     * The value of the filter option, or null if not set.
     *
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * The value of the filter option, or null if not set.
     *
     * @param filter the filter to set
     * @return this Query;
     */
    public Query setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Duplicate the source into this query by making a deep copy.
     *
     * @param source the source to duplicate.
     */
    public void duplicate(Query source) {
        count = source.count;
        filter = source.filter;
        orderBy = source.orderBy;
        skip = source.skip;
        top = source.top;
        for (Expand item : source.expand) {
            expand.add(item.clone());
        }
        for (String item : source.select) {
            select.add(item);
        }
    }
}
