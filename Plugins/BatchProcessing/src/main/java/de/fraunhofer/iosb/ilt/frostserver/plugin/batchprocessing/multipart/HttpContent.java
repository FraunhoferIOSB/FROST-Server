package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;

/**
 *
 * @author scf
 */
public class HttpContent implements Content {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpContent.class);
    private static final String COMMAND_REGEX = "^(GET|PATCH|POST|PUT|DELETE) ([^ ]+)( HTTP/[0-9]\\.[0-9])?";
    private static final Pattern COMMAND_PATTERN = Pattern.compile(COMMAND_REGEX);
    private static final String VERSION_REGEX = "/v[0-9]\\.[0-9](/|$)";
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX);

    /**
     * The different states the parser can have.
     */
    private enum State {
        PREHEADERS,
        COMMAND,
        HEADERS,
        DATA
    }

    private String logIndent = "";

    private State parseState = State.PREHEADERS;
    private HttpMethod method;
    private String version;
    private String path;

    private final Map<String, String> headersOuter = new HashMap<>();
    private final Map<String, String> headersInner = new HashMap<>();

    /**
     * Flag indicating there is a problem with the syntax of the multipart
     * content. If this is a changeSet, then the entire changeSet will be
     * discarded.
     */
    private boolean parseFailed = false;
    private boolean executeFailed = false;
    private final List<String> errors = new ArrayList<>();

    private final boolean requireContentId;
    private String contentId;
    private Id contentIdValue;
    private final StringBuilder data = new StringBuilder();
    private String statusLine;
    private final Version batchVersion;

    public HttpContent(Version batchVersion) {
        this(batchVersion, false);
    }

    public HttpContent(Version batchVersion, boolean requireContentId) {
        this.batchVersion = batchVersion;
        this.requireContentId = requireContentId;
    }

    @Override
    public void parseLine(String line) {
        switch (parseState) {
            case PREHEADERS:
                parsePreHeaderLine(line);
                break;

            case COMMAND:
                parseCommandLine(line);
                break;

            case HEADERS:
                parseHeaderLine(line);
                break;

            case DATA:
                data.append(line).append('\n');
                break;

            default:
                LOGGER.warn("Unknow parseState: {}", parseState);
                break;
        }
    }

    private void parsePreHeaderLine(String line) {
        if (line.trim().isEmpty()) {
            parseState = State.COMMAND;
            if (requireContentId) {
                contentId = headersOuter.get("content-id");
                if (StringHelper.isNullOrEmpty(contentId)) {
                    parseFailed = true;
                    errors.add("All Changeset parts must have a valid content-id header.");
                }
            }
        } else {
            HeaderUtils.addHeader(line, headersOuter, logIndent);
        }
    }

    private void parseCommandLine(String line) {
        if (line.trim().isEmpty()) {
            LOGGER.warn("{}Found extra empty line before command.", logIndent);
        } else {
            parseCommand(line);
            parseState = State.HEADERS;
        }
    }

    private void parseHeaderLine(String line) {
        if (line.trim().isEmpty()) {
            parseState = State.DATA;
        } else {
            HeaderUtils.addHeader(line, headersInner, logIndent);
        }
    }

    @Override
    public boolean isParseFailed() {
        return parseFailed;
    }

    public boolean isExecuteFailed() {
        return executeFailed;
    }

    public void setExecuteFailed(boolean executeFailed) {
        this.executeFailed = executeFailed;
    }

    @Override
    public List<String> getErrors() {
        return errors;
    }

    private void parseCommand(String line) {
        Matcher commandMatcher = COMMAND_PATTERN.matcher(line);
        if (!commandMatcher.find()) {
            LOGGER.error("{}Not a command: {}", logIndent, line);
            return;
        }
        method = HttpMethod.fromString(commandMatcher.group(1));
        String fullUrl = commandMatcher.group(2);
        Matcher versionMatcher = VERSION_PATTERN.matcher(fullUrl);
        if (versionMatcher.find()) {
            int versionStart = versionMatcher.start() + 1;
            int versionEnd = versionMatcher.end();
            if ("/".equals(versionMatcher.group(1))) {
                version = fullUrl.substring(versionStart, versionEnd - 1);
                path = fullUrl.substring(versionEnd - 1);
            } else {
                version = fullUrl.substring(versionStart, versionEnd);
                path = "/";
            }
        } else {
            version = batchVersion.urlPart;
            path = "/" + fullUrl;
        }
        LOGGER.debug("{}Found command: {}, version: {}, path: {}", logIndent, method, version, path);
    }

    /**
     * Get the path part of the http request.
     *
     * @return the URL part of the http request.
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the data in the http request. This does not include the outer
     * headers, command, nor inner headers.
     *
     * @return The data in http request.
     */
    public String getData() {
        return data.toString();
    }

    public void addData(String data) {
        this.data.append(data);
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public Id getContentIdValue() {
        return contentIdValue;
    }

    public void setContentIdValue(Id contentIdValue) {
        this.contentIdValue = contentIdValue;
    }

    public void updateUsingContentIds(List<ContentIdPair> contentIds) {
        for (ContentIdPair pair : contentIds) {
            path = path.replace(pair.key, pair.value.getUrl());
            int keyIndex = 0;
            String quotedKey = '"' + pair.key + '"';
            String value = pair.value.getJson();
            while ((keyIndex = data.indexOf(quotedKey, keyIndex)) != -1) {
                data.replace(keyIndex, keyIndex + quotedKey.length(), value);
                keyIndex += value.length();
            }
        }
        LOGGER.debug("{}Using replaced path and data with content ids {}: {}, {}", logIndent, contentIds, path, data);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    /**
     * Get the headers of the http request. These are not the same as the
     * multipart-headers.
     *
     * @return the headers of the http request.
     */
    public Map<String, String> getHttpHeaders() {
        return headersInner;
    }

    @Override
    public String getContent(boolean allHeaders) {
        StringBuilder content = new StringBuilder();
        if (allHeaders) {
            content.append("Content-Type: application/http\n");
            if (contentId != null) {
                content.append("Content-ID: ").append(contentId).append('\n');
            }
            content.append('\n');
        }
        if (statusLine != null) {
            content.append(statusLine).append('\n');
        }
        for (Map.Entry<String, String> entry : headersInner.entrySet()) {
            content.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
        content.append('\n');
        content.append(data);
        return content.toString();
    }

    @Override
    public Map<String, String> getHeaders() {
        return headersOuter;
    }

    @Override
    public void stripLastNewline() {
        int lastIdx = data.length() - 1;
        if (lastIdx < 0) {
            LOGGER.debug("{}No content to strip the last newline from.", logIndent);
            return;
        }
        if (data.charAt(lastIdx) != '\n') {
            LOGGER.error("{}Last character was not a newline, but: {}", logIndent, data.charAt(lastIdx));
            return;
        }
        data.deleteCharAt(lastIdx);
    }

    @Override
    public IsFinished isFinished() {
        return IsFinished.UNKNOWN;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void setLogIndent(String logIndent) {
        this.logIndent = logIndent;
    }

}
