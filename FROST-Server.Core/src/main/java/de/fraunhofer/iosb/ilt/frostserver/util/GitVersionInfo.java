/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class GitVersionInfo {

    private GitVersionInfo() {
        // Utility class, not to be instantiated.
    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GitVersionInfo.class);

    /**
     * A type reference for Map&lt;String, String&gt;.
     */
    public static final TypeReference TYPE_MAP_STRING_STRING = new TypeReference<Map<String, String>>() {
        // Empty on purpose.
    };
    public static final String PACKAGE_NAME = "FROST-Server.Core";

    private static Map<String, String> gitData;

    /**
     * Outputs the git version info to the log.
     */
    public static void logGitInfo() {
        init();
        LOGGER.info("{} Version: {}", PACKAGE_NAME, gitData.get("git.commit.id.describe"));
    }

    private static void init() {
        if (gitData == null) {
            gitData = readInfo();
        }
    }

    private static Map<String, String> readInfo() {
        ClassLoader classLoader = GitVersionInfo.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("git.json");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map;
        try {
            map = mapper.readValue(inputStream, TYPE_MAP_STRING_STRING);
        } catch (IOException exc) {
            LOGGER.error("Failed to read git info file.", exc);
            map = new HashMap<>();
        }
        return map;
    }
}
