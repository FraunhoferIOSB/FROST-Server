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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PkSingle implements PrimaryKey {

    private final List<EntityPropertyMain> keyProperties;

    public PkSingle(EntityPropertyMain keyProperty) {
        this.keyProperties = Collections.unmodifiableList(Arrays.asList(keyProperty));
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public List<EntityPropertyMain> getKeyProperties() {
        return keyProperties;
    }

    @Override
    public EntityPropertyMain getKeyProperty(int idx) {
        return keyProperties.get(idx);
    }

    @Override
    public PkValue parsePrimaryKey(String pk) {
        Object value = keyProperties.get(0).getType().parseFromUrl(pk);
        return PkValue.of(value);
    }

}
