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
package de.fraunhofer.iosb.ilt.sta.multipart;

import com.google.common.base.Strings;
import de.fraunhofer.iosb.ilt.sta.multipart.Content.IsFinished;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class Part {

    /**
     * The different states the parser can have.
     */
    private enum State {
        INITIAL,
        DATA,
        DONE
    }
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Part.class);
    private Map<String, String> headers = new HashMap<>();

    private State parseState = State.INITIAL;

    private Content content;

    private String logIndent = "";

    private final boolean inChangeSet;

    /**
     * Creates a new Part.
     *
     * @param inChangeSet flag indicating the Part is part of a ChangeSet, and
     * thus if the part itself can be a ChangeSet.
     */
    public Part(boolean inChangeSet) {
        this.inChangeSet = inChangeSet;
    }

    /**
     * Gives the parse-state of the Content in this Part. This indicates if more
     * content is expected or not.
     *
     * @return the parse-state of the Content.
     */
    public IsFinished isFinished() {
        if (content == null) {
            return Content.IsFinished.UNKNOWN;
        }
        return content.isFinished();
    }

    /**
     * Get the value of the header with the given name.
     *
     * @param name The name of the header to get.
     * @return The value of the header with the given name.
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    private void addHeader(String line) {
        Matcher matcher = MixedContent.HEADER_PATTERN.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1).trim().toLowerCase();
            String value = matcher.group(2).trim();
            headers.put(name, value);
            LOGGER.debug("{}Found header '{}' : '{}'", logIndent, name, value);
        } else {
            LOGGER.debug("{}Found non-header line, assuming content: '{}'", logIndent, line);
            content = new StringContent();
            content.parseLine(line);
            return;
        }
        String rest = line.substring(matcher.end());
        if (Strings.isNullOrEmpty(rest)) {
            return;
        }
        Matcher subHeaderMatcher = MixedContent.SUB_HEADER_PATTERN.matcher(rest);
        while (subHeaderMatcher.find()) {
            String subName = subHeaderMatcher.group(1).trim().toLowerCase();
            String subValue = subHeaderMatcher.group(2).trim();
            headers.put(subName, subValue);
            LOGGER.debug("{}  Found subheader '{}' : '{}'", logIndent, subName, subValue);
        }
    }

    /**
     * Parse the given line and add it to the Part.
     *
     * @param line the line to parse.
     */
    public void appendLine(String line) {
        switch (parseState) {
            case INITIAL:
                // The first line must state the content-type
                addHeader(line);
                determineType();
                break;

            case DATA:
                content.parseLine(line);
                if (content.isFinished() == IsFinished.FINISHED) {
                    setParseState(State.DONE);
                }
                break;

            case DONE:
                LOGGER.debug("{}Epilogue line: {}", logIndent, line);
                break;

            default:
                LOGGER.warn("{}Uhandled state: {}.", logIndent, parseState);
                break;

        }
    }

    private void determineType() {
        String contentType = getHeader("content-type");
        if ("multipart/mixed".equalsIgnoreCase(contentType)) {
            if (inChangeSet) {
                throw new IllegalArgumentException("ChangeSets not allowed in ChangeSets.");
            }
            LOGGER.debug("{}Found multipart content", logIndent);
            content = new MixedContent(true)
                    .setBoundaryHeader(getHeader("boundary"));
        } else if ("application/http".equalsIgnoreCase(contentType)) {
            LOGGER.debug("{}Found Http content", logIndent);
            content = new HttpContent(inChangeSet);
        } else {
            LOGGER.error("{}No or unknown content-type: {}", logIndent, contentType);
            if (inChangeSet) {
                throw new IllegalArgumentException("Only application/http content allowed in ChangeSets.");
            }
            if (content == null) {
                // We probably already have StringContent, when the first line was no header.
                content = new StringContent();
            }
        }
        content.setLogIndent(logIndent + "  ");
        setParseState(State.DATA);
    }

    private void setParseState(State parseState) {
        this.parseState = parseState;
        LOGGER.debug("{}Now in state: {}", logIndent, parseState);
    }

    /**
     * Informs the Content that the last newline should be removed again. The
     * newline before a boundary is part of the boundary, not of the content.
     */
    public void stripLastNewline() {
        if (content == null) {
            return;
        }
        content.stripLastNewline();
    }

    /**
     * Get the Content of this Part.
     *
     * @return the Content of this Part.
     */
    public Content getContent() {
        return content;
    }

    /**
     * Set the Content of this Part.
     *
     * @param content the Content of this Part.
     * @return this.
     */
    public Part setContent(Content content) {
        this.content = content;
        return this;
    }

    /**
     * Sets the indentation of log messages. Since Content can be nested, this
     * makes debug output better readable.
     *
     * @param logIndent the indentation of log messages.
     * @return this.
     */
    public Part setLogIndent(String logIndent) {
        this.logIndent = logIndent;
        return this;
    }

}
