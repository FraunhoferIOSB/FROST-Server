/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author hylke
 */
public class QueryDefaults {

    /**
     * Root URL of the service. Excluding the version number.
     */
    private String serviceRootUrl;

    /**
     * Flag indicating generated URLs should be absolute.
     */
    private boolean useAbsoluteNavigationLinks;

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

    public QueryDefaults(boolean absNavLinks, boolean countDefault, int topDefault, int topMax) {
        this.useAbsoluteNavigationLinks = absNavLinks;
        this.countDefault = countDefault;
        this.topDefault = topDefault;
        this.topMax = topMax;
    }

    public final QueryDefaults setServiceRootUrl(String serviceRootUrl) {
        this.serviceRootUrl = serviceRootUrl;
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
     */
    public void setCountDefault(boolean countDefault) {
        this.countDefault = countDefault;
    }

    /**
     * @return the topDefault
     */
    public int getTopDefault() {
        return topDefault;
    }

    /**
     * @param topDefault the topDefault to set
     */
    public void setTopDefault(int topDefault) {
        this.topDefault = topDefault;
    }

    /**
     * @return the topMax
     */
    public int getTopMax() {
        return topMax;
    }

    /**
     * @param topMax the topMax to set
     */
    public void setTopMax(int topMax) {
        this.topMax = topMax;
    }

    /**
     * @return the useAbsoluteNavigationLinks
     */
    public boolean useAbsoluteNavigationLinks() {
        return useAbsoluteNavigationLinks;
    }

    /**
     * @param useAbsoluteNavigationLinks the useAbsoluteNavigationLinks to set
     */
    public void setUseAbsoluteNavigationLinks(boolean useAbsoluteNavigationLinks) {
        this.useAbsoluteNavigationLinks = useAbsoluteNavigationLinks;
    }

}
