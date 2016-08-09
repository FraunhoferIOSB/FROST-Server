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
package de.fraunhofer.iosb.ilt.sta.util;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class UrlHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlHelper.class);

    private UrlHelper() {

    }

    public static String generateNextLink(ResourcePath path, Query query) {
        int oldSkip = 0;
        if (query.getSkip().isPresent()) {
            oldSkip = query.getSkip().get();
        }
        int top = query.getTopOrDefault();
        int newSkip = oldSkip + top;
        query.setSkip(newSkip);
        String nextLink = path.toString() + "?" + query.toString(false);
        query.setSkip(oldSkip);
        return nextLink;
    }

    public static String generateSelfLink(ResourcePath path, Entity entity) {
        StringBuilder sb = new StringBuilder(path.getServiceRootUrl());
        sb.append('/');
        sb.append(entity.getEntityType().plural);
        sb.append('(')
                .append(entity.getId().getValue().toString())
                .append(')');
        return sb.toString();
    }

    /**
     *
     * @param path
     * @param parent
     * @param entity
     * @param absolute
     * @return
     */
    public static String generateNavLink(ResourcePath path, Entity parent, Entity entity, boolean absolute) {
        String result = generateSelfLink(path, parent) + "/" + entity.getEntityType().name;
        if (!absolute) {
            String curPath = path.getServiceRootUrl() + path.getPathUrl();
            result = getRelativePath(result, curPath);
        }
        return result;
    }

    /**
     *
     * @param path
     * @param parent
     * @param es
     * @param absolute
     * @return
     */
    public static String generateNavLink(ResourcePath path, Entity parent, EntitySet es, boolean absolute) {
        String result = generateSelfLink(path, parent) + "/" + es.getEntityType().plural;
        if (!absolute) {
            String curPath = path.getServiceRootUrl() + path.getPathUrl();
            result = getRelativePath(result, curPath);
        }
        return result;
    }

    public static String getRelativePath(final String targetPath, final String basePath) {
        final String pathSeparator = "/";
        boolean targetIsDir = targetPath.endsWith(pathSeparator);
        boolean baseIsDir = basePath.endsWith(pathSeparator);
        //  We need the -1 argument to split to make sure we get a trailing
        //  "" token if the base ends in the path separator and is therefore
        //  a directory. We require directory paths to end in the path
        //  separator -- otherwise they are indistinguishable from files.
        String[] base = basePath.split(Pattern.quote(pathSeparator), -1);
        String[] target = targetPath.split(Pattern.quote(pathSeparator), 0);

        //  First get all the common elements. Store them as a string,
        //  and also count how many of them there are.
        String common = "";
        int commonIndex = 0;
        int baseLength = baseIsDir ? base.length : base.length - 1;
        for (int i = 0; i < target.length && i < baseLength; i++) {
            if (target[i].equals(base[i])) {
                common += target[i] + pathSeparator;
                commonIndex++;
            } else {
                break;
            }
        }

        if (commonIndex == 0) {
            return targetPath;
        }

        String relative = "";
        if (base.length == commonIndex) {
            //  Comment this out if you prefer that a relative path not start with ./
            relative = "." + pathSeparator;
        } else {
            int numDirsUp = base.length - commonIndex - (targetIsDir ? 0 : 1);
            //  The number of directories we have to backtrack is the length of
            //  the base path MINUS the number of common path elements, minus
            //  one because the last element in the path isn't a directory.

            for (int i = 1; i <= (numDirsUp); i++) {
                relative += ".." + pathSeparator;
            }
        }
        //if we are comparing directories then we
        if (targetPath.length() > common.length()) {
            //it's OK, it isn't a directory
            relative += targetPath.substring(common.length());
        }

        return relative;
    }
}
