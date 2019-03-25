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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static de.fraunhofer.iosb.ilt.frostserver.util.StringHelper.UTF8;

/**
 *
 * @author jab
 */
public class UrlHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlHelper.class);
    private static final String UTF8_NOT_SUPPORTED = "UTF-8 not supported?";

    private UrlHelper() {
        // Should not be instantiated.
    }

    /**
     * Replaces all ' in the string with ''.
     *
     * @param in The string to escape.
     * @return The escaped string.
     */
    public static String escapeForStringConstant(String in) {
        return in.replaceAll("'", "''");
    }

    public static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, UTF8.name());
        } catch (UnsupportedEncodingException exc) {
            // Should never happen, UTF-8 is build in.
            LOGGER.error(UTF8_NOT_SUPPORTED, exc);
            throw new IllegalStateException(UTF8_NOT_SUPPORTED, exc);
        }
    }

    /**
     * Decode the given input using UTF-8 as character set.
     *
     * @param input The input to urlDecode.
     * @return The decoded input.
     */
    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, UTF8.name());
        } catch (UnsupportedEncodingException exc) {
            // Should never happen, UTF-8 is build in.
            LOGGER.error(UTF8_NOT_SUPPORTED, exc);
            throw new IllegalStateException(UTF8_NOT_SUPPORTED, exc);
        }
    }

    /**
     * Urlencodes the given string, optionally not encoding forward slashes.
     *
     * In urls, forward slashes before the "?" must never be urlEncoded.
     * Urlencoding of slashes could otherwise be used to obfuscate phising URLs.
     *
     * @param string The string to urlEncode.
     * @param notSlashes If true, forward slashes are not encoded.
     * @return The urlEncoded string.
     */
    public static String urlEncode(String string, boolean notSlashes) {
        if (notSlashes) {
            return urlEncodeNotSlashes(string);
        }
        return urlEncode(string);
    }

    /**
     * Urlencodes the given string, except for the forward slashes.
     *
     * @param string The string to urlEncode.
     * @return The urlEncoded string.
     */
    public static String urlEncodeNotSlashes(String string) {
        String[] split = string.split("/");
        for (int i = 0; i < split.length; i++) {
            split[i] = urlEncode(split[i]);
        }
        return String.join("/", split);
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

    public static String generateSelfLink(String serviceRootUrl, Entity entity) {
        StringBuilder sb = new StringBuilder(serviceRootUrl);
        sb.append('/');
        sb.append(entity.getEntityType().plural);
        sb.append('(')
                .append(entity.getId().getUrl())
                .append(')');
        return sb.toString();
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
}
