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

import org.apache.commons.lang3.StringUtils;

/**
 * Class holding default values for queries.
 */
public class QueryDefaults {

    /**
     * Root URL of the service. Excluding the version number.
     */
    private String serviceRootUrl;

    /**
     * Flag indicating generated URLs should be absolute.
     */
    private boolean absNavLinks;

    /**
     * The default count to use when no specific count is set.
     */
    private boolean countDefault;

    /**
     * The default top to use when no specific top is set.
     */
    private int topDefault;

    /**
     * The maximum allowed top.
     */
    private int topMax;

    /**
     * If an automatic order by primary key is active.
     */
    private boolean alwaysOrder = false;

    public QueryDefaults(boolean absNavLinks, boolean countDefault, int topDefault, int topMax) {
        this(absNavLinks, countDefault, topDefault, topMax, false);
    }

    public QueryDefaults(boolean absNavLinks, boolean countDefault, int topDefault, int topMax, boolean alwaysOrder) {
        this.absNavLinks = absNavLinks;
        this.countDefault = countDefault;
        this.topDefault = topDefault;
        this.topMax = topMax;
        this.alwaysOrder = alwaysOrder;
    }

    /**
     * @param serviceRootUrl The service root url.
     * @return this.
     */
    public final QueryDefaults setServiceRootUrl(String serviceRootUrl) {
        this.serviceRootUrl = StringUtils.removeEnd(serviceRootUrl, "/");
        return this;
    }

    public String getServiceRootUrl() {
        return serviceRootUrl;
    }

    /**
     * @return the countDefault
     */
    public boolean isCountDefault() {
        return countDefault;
    }

    /**
     * @param countDefault the countDefault to set
     * @return this.
     */
    public QueryDefaults setCountDefault(boolean countDefault) {
        this.countDefault = countDefault;
        return this;
    }

    /**
     * @return the topDefault
     */
    public int getTopDefault() {
        return topDefault;
    }

    /**
     * @param topDefault the topDefault to set
     * @return this.
     */
    public QueryDefaults setTopDefault(int topDefault) {
        this.topDefault = topDefault;
        return this;
    }

    /**
     * @return the topMax
     */
    public int getTopMax() {
        return topMax;
    }

    /**
     * @param topMax the topMax to set
     * @return this.
     */
    public QueryDefaults setTopMax(int topMax) {
        this.topMax = topMax;
        return this;
    }

    /**
     * @return the useAbsoluteNavigationLinks
     */
    public boolean useAbsoluteNavigationLinks() {
        return absNavLinks;
    }

    /**
     * @param useAbsoluteNavigationLinks the useAbsoluteNavigationLinks to set
     * @return this.
     */
    public QueryDefaults setUseAbsoluteNavigationLinks(boolean useAbsoluteNavigationLinks) {
        this.absNavLinks = useAbsoluteNavigationLinks;
        return this;
    }

    /**
     * If an automatic order by primary key is active.
     *
     * @return the alwaysOrder value
     */
    public boolean isAlwaysOrder() {
        return alwaysOrder;
    }

    /**
     * If an automatic order by primary key is active.
     *
     * @param alwaysOrder the new alwaysOrder value
     * @return this.
     */
    public QueryDefaults setAlwaysOrder(boolean alwaysOrder) {
        this.alwaysOrder = alwaysOrder;
        return this;
    }

    /**
     * Create a copy of this QueryDefaults.
     *
     * @return A copy of this query defaults, with the same values.
     */
    public QueryDefaults copy() {
        return new QueryDefaults(absNavLinks, countDefault, topDefault, topMax, alwaysOrder)
                .setServiceRootUrl(serviceRootUrl);
    }
}
