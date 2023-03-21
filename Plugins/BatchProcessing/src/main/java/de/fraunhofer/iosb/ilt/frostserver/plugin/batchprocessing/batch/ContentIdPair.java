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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;

/**
 *
 * @author scf
 */
public final class ContentIdPair {

    public final String key;
    public final Id value;

    public ContentIdPair(String key, Id value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Id getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ContentIdPair [key=" + key + ", value=" + value + "]";
    }
}
