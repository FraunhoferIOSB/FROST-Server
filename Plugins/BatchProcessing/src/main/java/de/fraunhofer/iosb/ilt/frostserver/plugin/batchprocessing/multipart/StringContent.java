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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Content with no content type.
 *
 * @author scf
 */
public class StringContent implements MultipartContent {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StringContent.class);

    private StringBuilder content;
    private String logIndent = "";

    @Override
    public void parseLine(String line) {
        content.append(line).append('\n');
    }

    @Override
    public boolean isParseFailed() {
        return false;
    }

    @Override
    public List<String> getErrors() {
        return Collections.emptyList();
    }

    @Override
    public void stripLastNewline() {
        int lastIdx = content.length() - 1;
        if (lastIdx < 0) {
            LOGGER.debug("{}No content to strip the last newline from.", logIndent);
        }
        if (content.charAt(lastIdx) != '\n') {
            LOGGER.error("{}Last character was not a newline, but: {}", logIndent, content.charAt(lastIdx));
            return;
        }
        content.deleteCharAt(lastIdx);
    }

    @Override
    public IsFinished isFinished() {
        return IsFinished.UNKNOWN;
    }

    @Override
    public void setLogIndent(String logIndent) {
        this.logIndent = logIndent;
    }

    @Override
    public String getContent(boolean allHeaders) {
        return content.toString();
    }

    public void setContent(StringBuilder content) {
        this.content = content;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return Collections.emptyMap();
    }

}
