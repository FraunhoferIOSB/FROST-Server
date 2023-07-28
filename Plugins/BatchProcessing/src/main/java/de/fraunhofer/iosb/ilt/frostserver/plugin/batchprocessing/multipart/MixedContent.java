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

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Batch;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Part;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for multipart/mixed mime type stringContent, since Tomcat only handles
 * multipart/form-data.
 *
 * @author scf
 */
public class MixedContent extends Batch<MultipartContent> implements MultipartContent {

    public static final String BOUNDARY_REGEX = "boundary=[\"]?([A-Za-z0-9'()+_,-./:=?]+)[\"]?";
    public static final Pattern BOUNDARY_PATTERN = Pattern.compile(BOUNDARY_REGEX);
    public static final String HEADER_REGEX = "^([-A-Za-z]+):([^;]+)(;[ ]*)?";
    public static final Pattern HEADER_PATTERN = Pattern.compile(HEADER_REGEX);
    public static final String SUB_HEADER_REGEX = "([-A-Za-z]+)=([^;]+)(;[ ]*)?";
    public static final Pattern SUB_HEADER_PATTERN = Pattern.compile(SUB_HEADER_REGEX);
    private static final char[] BOUNDARY_CHARS = "-_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    /**
     * The different states the parser can have.
     */
    private enum State {
        PREAMBLE,
        PARTCONTENT,
        PARTDONE,
        EPILOGUE
    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MixedContent.class);
    private static final Random RAND = new Random();

    private String boundary;
    private String boundaryPart;
    private String boundaryEnd;

    private State state = State.PREAMBLE;
    private IsFinished finished = IsFinished.UNFINISHED;
    private MixedPart currentPart;

    public MixedContent(Version batchVersion, CoreSettings settings, boolean isChangeSet) {
        super(batchVersion, settings, isChangeSet);
    }

    @Override
    public boolean parse(ServiceRequest request) {
        String contentType = request.getContentType();
        Matcher matcher = BOUNDARY_PATTERN.matcher(contentType);
        if (!matcher.find()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}Could not find boundary in content type: {}", logIndent, StringHelper.cleanForLogging(contentType));
            }
            return false;
        }
        String boundaryHeader = matcher.group(1);
        setBoundaryHeader(boundaryHeader);
        try (BufferedReader reader = new BufferedReader(request.getContentReader())) {
            String line;
            while (finished != IsFinished.FINISHED && (line = reader.readLine()) != null) {
                parseLine(line);
            }
            return true;
        } catch (IOException exc) {
            LOGGER.error("Failed to read data.", exc);
            return false;
        }
    }

    public MixedContent setBoundaryHeader(String boundaryHeader) {
        boundary = boundaryHeader;
        boundaryPart = "--" + boundaryHeader;
        boundaryEnd = boundaryPart + "--";
        return this;
    }

    @Override
    public void parseLine(String line) {
        try {
            parseLineInternal(line);
        } catch (IllegalArgumentException exc) {
            LOGGER.info("Parse error on multipart content.", exc);
            parseFailed = true;
            errors.add(exc.getMessage());
        }
    }

    public void parseLineInternal(String line) {
        LOGGER.trace("{}Read line: {}", logIndent, line);
        switch (state) {
            case PREAMBLE:
                parsePreamble(line);
                break;

            case PARTCONTENT:
                parsePartContent(line);
                break;

            case PARTDONE:
                parsePartDone(line);
                break;

            case EPILOGUE:
                LOGGER.debug("{}Epilogue line: {}", logIndent, line);
                break;

            default:
                LOGGER.warn("{}Uhandled state: {}.", logIndent, state);
                break;
        }
    }

    private void parsePreamble(String line) {
        if (boundaryPart.equals(line.trim())) {
            setState(State.PARTCONTENT);
            currentPart = new MixedPart(batchVersion, settings, isChangeSet, logIndent + "  ");
        }
    }

    private void parsePartContent(String line) {
        if (currentPart == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}Content without part: {}", logIndent, StringHelper.cleanForLogging(line));
            }
            return;
        }
        boolean checkBoundary = currentPart.isFinished() != IsFinished.UNFINISHED;
        if (checkBoundary && boundaryPart.equals(line.trim())) {
            LOGGER.debug("{}Found new part", logIndent);
            currentPart.stripLastNewline();
            parts.add(currentPart);
            currentPart = new MixedPart(batchVersion, settings, isChangeSet, logIndent + "  ");
            setState(State.PARTCONTENT);

        } else if (checkBoundary && boundaryEnd.equals(line.trim())) {
            LOGGER.debug("{}Found end of multipart content", logIndent);
            currentPart.stripLastNewline();
            parts.add(currentPart);
            currentPart = null;
            finishParsing();

        } else {
            currentPart.appendLine(line);
            if (currentPart.isFinished() == IsFinished.FINISHED) {
                LOGGER.debug("{}Part declared done", logIndent);
                parts.add(currentPart);
                currentPart = null;
                setState(State.PARTDONE);
            }
        }
    }

    private void parsePartDone(String line) {
        if (boundaryPart.equals(line.trim())) {
            LOGGER.debug("{}Found new part", logIndent);
            currentPart = new MixedPart(batchVersion, settings, isChangeSet, logIndent + "  ");
            setState(State.PARTCONTENT);
        } else if (boundaryEnd.equals(line.trim())) {
            LOGGER.debug("{}Found end of multipart content", logIndent);
            finishParsing();
        } else if (!StringHelper.isNullOrEmpty(line) && LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}Ignoring line: {}", logIndent, StringHelper.cleanForLogging(line));
        }
    }

    private void finishParsing() {
        setState(State.EPILOGUE);
        finished = IsFinished.FINISHED;
        for (Part part : parts) {
            if (part.getContent().isParseFailed()) {
                parseFailed = true;
                errors.addAll(part.getContent().getErrors());
            }
        }
        LOGGER.debug("{}Found {} parts", logIndent, parts.size());
    }

    @Override
    public void stripLastNewline() {
        // Do nothing.
    }

    private void setState(State state) {
        LOGGER.debug("{}Now in state: {}", logIndent, state);
        this.state = state;
    }

    @Override
    public IsFinished isFinished() {
        return finished;
    }

    @Override
    public String getContent(boolean allHeaders) {
        if (boundary == null) {
            generateBoundary();
        }
        StringBuilder content = new StringBuilder();
        if (allHeaders) {
            content.append("Content-Type: multipart/mixed; boundary=").append(boundary).append('\n');
            content.append('\n');
        }

        for (Part<MultipartContent> part : parts) {
            content.append(boundaryPart).append('\n');
            MultipartContent partContent = part.getContent();
            content.append(partContent.getContent(true));
            content.append('\n');
        }

        content.append(boundaryEnd);
        return content.toString();
    }

    @Override
    public Map<String, String> getHeaders() {
        if (boundary == null) {
            generateBoundary();
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "multipart/mixed; boundary=" + boundary);
        return headers;
    }

    private void generateBoundary() {
        StringBuilder retval = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            retval.append(BOUNDARY_CHARS[RAND.nextInt(BOUNDARY_CHARS.length)]);
        }
        boundary = retval.toString();
        boundaryPart = "--" + boundary;
        boundaryEnd = boundaryPart + "--";
    }
}
