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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart;

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class HeaderUtils {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderUtils.class);
    private static final String CONTENT_ID_NAME = "content-id";
    private static final String CONTENT_ID_REGEX = "^[0-9a-zA-Z.~_-]+$";
    private static final String HOST_NAME = "host";
    private static final String HOST_REGEX = "^[0-9a-zA-Z:._-]+$";
    private static final String CONTENT_TYPE_NAME = "content-type";
    private static final String CONTENT_TYPE_REGEX = "^[0-9a-zA-Z/_-]+$";
    private static final String CHARSET_NAME = "charset";
    private static final String CHARSET_REGEX = "^[0-9a-zA-Z._-]+$";
    private static final String CONTENT_LENGTH_NAME = "content-length";
    private static final String CONTENT_LENGTH_REGEX = "^[0-9]+$";

    private static final Map<String, Validator> VALIDATORS = new HashMap<>();

    static {
        VALIDATORS.put(CONTENT_ID_NAME, new ValidatorRegex(CONTENT_ID_REGEX));
        VALIDATORS.put(HOST_NAME, new ValidatorRegex(HOST_REGEX));
        VALIDATORS.put(CONTENT_TYPE_NAME, new ValidatorRegex(CONTENT_TYPE_REGEX));
        VALIDATORS.put(CHARSET_NAME, new ValidatorRegex(CHARSET_REGEX));
        VALIDATORS.put(CONTENT_LENGTH_NAME, new ValidatorRegex(CONTENT_LENGTH_REGEX));
    }

    private HeaderUtils() {
    }

    public static void addHeader(String line, Map<String, String> headers) {
        addHeader(line, headers, "");
    }

    public static void addHeader(String line, Map<String, String> headers, String logIndent) {
        Matcher matcher = MixedContent.HEADER_PATTERN.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1).trim().toLowerCase();
            String value = matcher.group(2).trim();
            if (validateHeader(name, value)) {
                headers.put(name, value);
                LOGGER.debug("{}Found header '{}' : '{}'", logIndent, name, value);
            } else {
                LOGGER.debug("{}Header '{}' has invalid value: '{}'", logIndent, name, value);
            }
        } else {
            LOGGER.error("{}Found non-header line in headers: '{}'", logIndent, line);
            return;
        }
        String rest = line.substring(matcher.end());
        if (StringHelper.isNullOrEmpty(rest)) {
            return;
        }
        Matcher subHeaderMatcher = MixedContent.SUB_HEADER_PATTERN.matcher(rest);
        while (subHeaderMatcher.find()) {
            String subName = subHeaderMatcher.group(1).trim().toLowerCase();
            String subValue = subHeaderMatcher.group(2).trim();
            if (validateHeader(subName, subValue)) {
                headers.put(subName, subValue);
                LOGGER.debug("{}  Found subheader '{}' : '{}'", logIndent, subName, subValue);
            } else {
                LOGGER.warn("{}Header '{}' has invalid value: '{}'", logIndent, subName, subValue);
            }
        }
    }

    public static boolean validateHeader(String name, String value) {
        Validator validator = VALIDATORS.get(name);
        if (validator == null) {
            LOGGER.warn("No validation rules for header {}.", name);
            return true;
        }
        return validator.validate(value);
    }

    public static String generateStatusLine(int statusCode, String statusPhrase) {
        return "http/1.1 " + statusCode + " " + statusPhrase;
    }

    /**
     * The interface for validating headers.
     */
    private static interface Validator {

        public boolean validate(String value);
    }

    /**
     * A Validator that uses regular expression matching.
     */
    private static final class ValidatorRegex implements Validator {

        private final Pattern pattern;

        public ValidatorRegex(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public boolean validate(String value) {
            return pattern.matcher(value).matches();
        }

    }

}
