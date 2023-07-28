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
