/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence;

import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.core.IdLong;

/**
 *
 * @author scf
 */
public class IdManagerlong implements IdManager {

    @Override
    public Class<? extends Id> getIdClass() {
        return IdLong.class;
    }

    @Override
    public Id parseId(String input) {
        return new IdLong(Long.parseLong(input));
    }

    @Override
    public Id fromObject(Object input) {
        if (input instanceof Number) {
            return new IdLong(((Number) input).longValue());
        }
        throw new IllegalArgumentException("Can not use " + input.getClass().getName() + " (" + input + ") as a long Id");
    }

}
