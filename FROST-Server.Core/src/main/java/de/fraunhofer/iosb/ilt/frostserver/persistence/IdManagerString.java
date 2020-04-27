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
package de.fraunhofer.iosb.ilt.frostserver.persistence;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdString;

/**
 *
 * @author scf
 */
public class IdManagerString implements IdManager<String> {

    @Override
    public Class<? extends Id> getIdClass() {
        return IdString.class;
    }

    @Override
    public Id parseId(String input) {
        if (input.startsWith("'")) {
            String idString = input.substring(1, input.length() - 1);
            idString = idString.replace("''", "'");
            return new IdString(idString);
        }
        return new IdString(input);
    }

    @Override
    public Id fromObject(String input) {
        return new IdString(input);
    }

}
