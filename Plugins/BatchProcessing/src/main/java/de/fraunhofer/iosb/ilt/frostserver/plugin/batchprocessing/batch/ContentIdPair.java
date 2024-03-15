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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;

/**
 *
 * @author scf
 */
public final class ContentIdPair {

    public final String key;
    public final PkValue value;
    public final EntityType type;

    public ContentIdPair(String key, PkValue value, EntityType type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public PkValue getValue() {
        return value;
    }

    public EntityType getType() {
        return type;
    }

    public String keyToUrl() {
        return UrlHelper.quoteForUrl(type.getPrimaryKey(), value);
    }

    @Override
    public String toString() {
        return "ContentIdPair [key=" + key + ", value=" + value + "]";
    }
}
