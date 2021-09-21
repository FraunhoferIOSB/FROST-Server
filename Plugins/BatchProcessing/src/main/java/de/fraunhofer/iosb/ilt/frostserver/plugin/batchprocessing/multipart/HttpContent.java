package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.ContentIdPair;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Request;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class HttpContent extends Request implements MultipartContent {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpContent.class);
    private static final String COMMAND_REGEX = "^(GET|PATCH|POST|PUT|DELETE) ([^ ]+)( HTTP/[0-9]\\.[0-9])?";
    private static final Pattern COMMAND_PATTERN = Pattern.compile(COMMAND_REGEX);

    /**
     * The different states the parser can have.
     */
    private enum State {
        PREHEADERS,
        COMMAND,
        HEADERS,
        DATA
    }

    private State parseState = State.PREHEADERS;

    private String statusLine;

    public HttpContent(Version batchVersion) {
        this(batchVersion, false);
    }

    public HttpContent(Version batchVersion, boolean requireContentId) {
        super(batchVersion, requireContentId);
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

    private void parseCommand(String line) {
        Matcher commandMatcher = COMMAND_PATTERN.matcher(line);
        if (!commandMatcher.find()) {
            LOGGER.error("{}Not a command: {}", logIndent, line);
            return;
        }
        method = HttpMethod.fromString(commandMatcher.group(1));
        parseUrl(commandMatcher.group(2));
        LOGGER.debug("{}Found command: {}, version: {}, path: {}", logIndent, method, version, path);
    }

    /**
     * Get the path part of the http request.
     *
     * @return the URL part of the http request.
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * Get the data in the http request. This does not include the outer
     * headers, command, nor inner headers.
     *
     * @return The data in http request.
     */
    @Override
    public String getData() {
        return data.toString();
    }

    @Override
    public void addData(String data) {
        this.data.append(data);
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public Id getContentIdValue() {
        return contentIdValue;
    }

    @Override
    public void setContentIdValue(Id contentIdValue) {
        this.contentIdValue = contentIdValue;
    }

    @Override
    public void updateUsingContentIds(List<ContentIdPair> contentIds) {
        for (ContentIdPair pair : contentIds) {
            path = path.replace(pair.key, pair.value.getUrl());
            final String quotedKey = '"' + pair.key + '"';
            final String valueJson = pair.value.getJson();
            int keyIndex = 0;
            while ((keyIndex = data.indexOf(quotedKey, keyIndex)) != -1) {
                data.replace(keyIndex, keyIndex + quotedKey.length(), valueJson);
                keyIndex += valueJson.length();
            }
        }
        LOGGER.debug("{}Using replaced path and data with content ids {}: {}, {}", logIndent, contentIds, path, data);
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    @Override
    public void setStatus(int code, String text) {
        setStatusLine(HeaderUtils.generateStatusLine(code, text));
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

}
