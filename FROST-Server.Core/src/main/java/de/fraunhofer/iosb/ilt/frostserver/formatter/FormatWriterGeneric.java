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
import java.io.Writer;

/**
 * A generic FormatWriter that takes pre-formatted data.
 */
public class FormatWriterGeneric implements FormatWriter {

    private final String data;

    public FormatWriterGeneric(String data) {
        this.data = data;
    }

    @Override
    public String getFormatted() {
        return data;
    }

    @Override
    public void writeFormatted(Writer target) throws IOException {
        target.write(data);
    }
}
