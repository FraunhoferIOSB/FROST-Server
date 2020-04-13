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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.regex.Pattern;

/**
 *
 * @author jab
 */
public class UrlHelper {

    private UrlHelper() {
        // Should not be instantiated.
    }

    public static String generateNextLink(ResourcePath path, Query query) {
        int oldSkip = query.getSkip(0);
        int top = query.getTopOrDefault();
        int newSkip = oldSkip + top;
        query.setSkip(newSkip);
        String nextLink = path.toString() + "?" + query.toString(false);
        query.setSkip(oldSkip);
        return nextLink;
    }

    public static String generateSelfLink(String serviceRootUrl, EntityType entityType, Object id) {
        return new StringBuilder(serviceRootUrl)
                .append('/')
                .append(entityType.plural)
                .append('(')
                .append(quoteForUrl(id))
                .append(')')
                .toString();
    }

    public static String generateSelfLink(String serviceRootUrl, EntityType entityType, Id id) {
        return new StringBuilder(serviceRootUrl)
                .append('/')
                .append(entityType.plural)
                .append('(')
                .append(id.getUrl())
                .append(')')
                .toString();
    }

    public static String generateSelfLink(String serviceRootUrl, Entity entity) {
        return generateSelfLink(serviceRootUrl, entity.getEntityType(), entity.getId());
    }

    public static String generateSelfLink(ResourcePath path, Entity entity) {
        return generateSelfLink(path.getServiceRootUrl(), entity);
    }

    /**
     * Generate a navigation link for the given entity, using the given path and
     * parent entities.
     *
     * @param path The path for the current page that relative links are
     * relative to.
     * @param parent The parent of the entity to generate a navlink for.
     * @param entity The entity to generate a navlink for.
     * @param absolute If true, the generated link is absolute.
     * @return A navigation link.
     */
    public static String generateNavLink(ResourcePath path, Entity parent, Entity entity, boolean absolute) {
        String result = generateSelfLink(path, parent) + "/" + entity.getEntityType().entityName;
        if (!absolute) {
            String curPath = path.getServiceRootUrl() + path.getPathUrl();
            result = getRelativePath(result, curPath);
        }
        return result;
    }

    /**
     * Generate a navigation link for the given EntitySet, using the given path
     * and parent entities.
     *
     * @param path The path for the current page that relative links are
     * relative to.
     * @param parent The parent of the entity to generate a navlink for.
     * @param es The EntitySet to generate a navlink for.
     * @param absolute If true, the generated link is absolute.
     * @return A navigation link.
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
        StringBuilder common = new StringBuilder();
        int commonIndex = 0;
        int baseLength = baseIsDir ? base.length : base.length - 1;
        for (int i = 0; i < target.length && i < baseLength; i++) {
            if (target[i].equals(base[i])) {
                common.append(target[i]).append(pathSeparator);
                commonIndex++;
            } else {
                break;
            }
        }

        if (commonIndex == 0) {
            return targetPath;
        }

        StringBuilder relative = new StringBuilder();
        if (base.length == commonIndex) {
            relative = new StringBuilder("." + pathSeparator);
        } else {
            int numDirsUp = base.length - commonIndex - (targetIsDir ? 0 : 1);
            //  The number of directories we have to backtrack is the length of
            //  the base path MINUS the number of common path elements, minus
            //  one because the last element in the path isn't a directory.

            for (int i = 1; i <= (numDirsUp); i++) {
                relative.append("..").append(pathSeparator);
            }
        }
        if (targetPath.length() > common.length()) {
            relative.append(targetPath.substring(common.length()));
        }

        return relative.toString();
    }

    public static String quoteForUrl(Object in) {
        if (in == null) {
            return "'null'";
        }
        if (in instanceof Number) {
            return in.toString();
        }
        return "'" + StringHelper.escapeForStringConstant(in.toString()) + "'";
    }
}
