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
package de.fraunhofer.iosb.ilt.frostserver.path;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.Constant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.ConstantFactory;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.Equal;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.GreaterThan;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.LessThan;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.Or;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class UrlHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlHelper.class.getName());

    private UrlHelper() {
        // Should not be instantiated.
    }

    public static String generateNextLink(ResourcePath path, Query query) {
        return generateNextLink(path, query, query.getTopOrDefault());
    }

    public static String generateNextLink(ResourcePath path, Query query, int resultCount) {
        int oldSkip = query.getSkip(0);
        int newSkip = oldSkip + resultCount;
        query.setSkip(newSkip);
        String nextLink = path.toString() + "?" + query.toString(false);
        query.setSkip(oldSkip);
        return nextLink;
    }

    private static class SkipFilterGenerator {

        final Entity last;
        final Entity next;

        private boolean done = false;
        private boolean failed = false;

        Expression skipFilter = null;
        And lastAnd = null;

        public SkipFilterGenerator(Entity last, Entity next) {
            this.last = last;
            this.next = next;
        }

        public boolean isFailed() {
            return failed || skipFilter == null;
        }

        private boolean setFailed() {
            failed = true;
            return setDone();
        }

        public boolean isDone() {
            return done;
        }

        private boolean setDone() {
            done = true;
            return done;
        }

        public Expression getSkipFilter() {
            return skipFilter;
        }

        public boolean addOrderBy(OrderBy orderby) {
            if (done) {
                return done;
            }
            Expression orderExpression = orderby.getExpression();
            if (!(orderExpression instanceof Path)) {
                return setFailed();
            }
            Path obPath = (Path) orderExpression;
            Object lastValue = last.getProperty(obPath);
            Object nextValue = next.getProperty(obPath);
            if (lastValue == null || nextValue == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Order expression value is null, using normal nextLink: {}", orderExpression.toUrl());
                }
                return setFailed();
            }
            Constant valueConstant = ConstantFactory.of(lastValue);
            if (valueConstant == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Order expression value can not be made a constant, using normal nextLink: {}", orderExpression.toUrl());
                }
                return setFailed();
            }
            if (lastValue.equals(nextValue)) {
                return handleValueEqual(obPath, valueConstant, orderby);
            } else {
                return handleValueDifferent(orderby, obPath, valueConstant);
            }
        }

        private boolean handleValueEqual(Path obPath, Constant valueConstant, OrderBy orderby) {
            And newAnd = new And(new Equal(obPath, valueConstant));
            Or newFilter;
            if (orderby.getType() == OrderBy.OrderType.DESCENDING) {
                newFilter = new Or(
                        new LessThan(obPath, valueConstant),
                        newAnd);
            } else {
                newFilter = new Or(
                        new GreaterThan(obPath, valueConstant),
                        newAnd);
            }
            if (lastAnd == null) {
                skipFilter = newFilter;
            } else {
                lastAnd.addParameter(newFilter);
            }
            lastAnd = newAnd;
            return done;
        }

        private boolean handleValueDifferent(OrderBy orderby, Path obPath, Constant valueConstant) {
            Expression newFilter;
            if (orderby.getType() == OrderBy.OrderType.DESCENDING) {
                newFilter = new LessThan(obPath, valueConstant);
            } else {
                newFilter = new GreaterThan(obPath, valueConstant);
            }
            if (lastAnd == null) {
                skipFilter = newFilter;
            } else {
                lastAnd.addParameter(newFilter);
            }
            LOGGER.debug("Hit value border for order, done.");
            return setDone();
        }

    }

    public static String generateNextLink(ResourcePath path, Query query, int resultCount, Entity last, Entity next) {
        final String standardNextLink = generateNextLink(path, query, resultCount);
        if (!query.isPkOrder()) {
            return standardNextLink;
        }
        SkipFilterGenerator sfg = new SkipFilterGenerator(last, next);
        for (OrderBy orderby : query.getOrderBy()) {
            if (sfg.addOrderBy(orderby)) {
                break;
            }
        }

        if (sfg.isFailed()) {
            return standardNextLink;
        }
        final String skipFilter = sfg.getSkipFilter().toUrl();
        return new StringBuilder(standardNextLink)
                .append("&$skipFilter=")
                .append(StringHelper.urlEncode(skipFilter))
                .toString();
    }

    public static String generateSelfLink(String serviceRootUrl, Version version, EntityType entityType, Object id) {
        return new StringBuilder(serviceRootUrl)
                .append('/')
                .append(version.urlPart)
                .append('/')
                .append(entityType.plural)
                .append('(')
                .append(quoteForUrl(id))
                .append(')')
                .toString();
    }

    public static String generateSelfLink(String serviceRootUrl, Version version, EntityType entityType, Id id) {
        return new StringBuilder(serviceRootUrl)
                .append('/')
                .append(version.urlPart)
                .append('/')
                .append(entityType.plural)
                .append('(')
                .append(id.getUrl())
                .append(')')
                .toString();
    }

    public static String generateSelfLink(String serviceRootUrl, Version version, Entity entity) {
        return generateSelfLink(serviceRootUrl, version, entity.getEntityType(), entity.getId());
    }

    public static String generateSelfLink(ResourcePath path, Entity entity) {
        return generateSelfLink(path.getServiceRootUrl(), path.getVersion(), entity.getEntityType(), entity.getId());
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
     * @return A navigation link or null depending on query responseMetadata.
     */
    public static String generateNavLink(ResourcePath path, Entity parent, Entity entity, boolean absolute) {
        String result = generateSelfLink(path, parent) + "/" + entity.getEntityType().entityName;
        if (!absolute) {
            String curPath = path.getServiceRootUrl() + path.getPath();
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
     * @return A navigation link or null depending on query responseMetadata.
     */
    public static String generateNavLink(ResourcePath path, Entity parent, EntitySet es, boolean absolute) {
        String result = generateSelfLink(path, parent) + "/" + es.getEntityType().plural;
        if (!absolute) {
            String curPath = path.getServiceRootUrl() + path.getPath();
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

    public static Map<String, List<String>> splitQuery(String queryString) {
        if (StringHelper.isNullOrEmpty(queryString)) {
            return new LinkedHashMap<>();
        }
        return Arrays.stream(queryString.split("&"))
                .map(UrlHelper::splitQueryParameter)
                .collect(Collectors.groupingBy(
                        AbstractMap.SimpleImmutableEntry::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private static AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf('=');
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new AbstractMap.SimpleImmutableEntry<>(
                key == null ? null : URLDecoder.decode(key, StringHelper.UTF8),
                value == null ? null : URLDecoder.decode(value, StringHelper.UTF8));
    }

    public static Map<String, String> decodePrefer(String input, Map<String, String> output) {
        if (output == null) {
            output = new HashMap<>();
        }
        if (input == null || input.isEmpty()) {
            return output;
        }
        String[] parts = StringUtils.split(input, ',');
        for (String part : parts) {
            if (part == null) {
                continue;
            }
            String[] subParts = StringUtils.split(part, "=", 2);
            switch (subParts.length) {
                case 2 -> {
                    output.put(subParts[0].trim(), subParts[1].trim());
                }

                case 1 -> {
                    output.put(subParts[0].trim(), "");
                }

                default -> {
                    // Nothing to do.
                }
            }
        }
        return output;
    }
}
