package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart;

import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Content;

/**
 *
 * @author scf
 */
public interface MultipartContent extends Content {

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

}
