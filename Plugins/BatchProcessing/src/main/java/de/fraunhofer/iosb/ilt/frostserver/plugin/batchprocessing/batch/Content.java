package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch;

import java.util.List;
import java.util.Map;

public interface Content {

    /**
     * Flag indicating there were errors parsing the content.
     *
     * @return true if there were parse errors.
     */
    public boolean isParseFailed();

    /**
     * Get the list of error messages generating during parsing.
     *
     * @return A list of error messages generating during parsing.
     */
    public List<String> getErrors();

    /**
     * Sets the indentation of log messages. Since Content can be nested, this
     * makes debug output better readable.
     *
     * @param logIndent the indentation of log messages.
     */
    public void setLogIndent(String logIndent);

    /**
     * Get the String content for response.
     *
     * @param allHeaders flag indicating all headers should be included. If the
     * content is going to be added to a HttpServletResponse, the headers need
     * to be set separately.
     * @return The content.
     */
    public String getContent(boolean allHeaders);

    /**
     * Get the headers. This will include the Content-Type header.
     *
     * @return the headers.
     */
    public Map<String, String> getHeaders();
}
