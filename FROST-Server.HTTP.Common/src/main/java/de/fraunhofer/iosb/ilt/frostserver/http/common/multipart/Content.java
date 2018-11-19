package de.fraunhofer.iosb.ilt.frostserver.http.common.multipart;

import java.util.List;
import java.util.Map;

/**
 *
 * @author scf
 */
public interface Content {

    /**
     * Indicates the parse-state of the Content.
     */
    public enum IsFinished {
        /**
         * The Content does not expect any more lines.
         */
        FINISHED,
        /**
         * The content does not know.
         */
        UNKNOWN,
        /**
         * The content expects more lines.
         */
        UNFINISHED
    }

    /**
     * Add the line to the content.
     *
     * @param line The line to add.
     */
    public void parseLine(String line);

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
     * Informs the Content that the last newline should be removed again. The
     * newline before a boundary is part of the boundary, not of the content.
     */
    public void stripLastNewline();

    /**
     * Gives the parse-state of the Content. This indicates if more content is
     * expected or not.
     *
     * @return the parse-state of the Content.
     */
    public IsFinished isFinished();

    /**
     * Sets the indentation of log messages. Since Content can be nested, this
     * makes debug output better readable.
     *
     * @param logIndent the indentation of log messages.
     */
    public void setLogIndent(String logIndent);

    /**
     * Get the String content.
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
