/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.formatter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author hylke
 */
public interface FormatWriter {

    /**
     * Get formatted data as a String. Either this, or
     * {@link #writeFormatted(java.io.Writer)} can be called, but not both. Can
     * be called only once.
     *
     * @return The formatted data as a String.
     */
    public default String getFormatted() {
        StringWriter writer = new StringWriter();
        try {
            writeFormatted(writer);
        } catch (IOException ex) {
            // should not happen
        }
        return writer.toString();
    }

    /**
     * Write the formatted data to the target Writer. Either this, or
     * {@link #getFormatted()} can be called, but not both.
     *
     * @param target the Writer to write the formatted data to.
     *
     * @throws java.io.IOException when Writing fails.
     */
    public void writeFormatted(Writer target) throws IOException;

}
