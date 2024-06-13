/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper;

import de.fraunhofer.iosb.ilt.frostserver.model.loader.PropertyPersistenceMapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import java.util.Map;

/**
 * Interface for classes that set up the mapping between database fields and
 * data mode properties.
 */
public interface FieldMapper extends PropertyPersistenceMapper {

    public void registerField(JooqPersistenceManager ppm, StaMainTable staTable);

    public <T extends StaMainTable<T>> void registerMapping(JooqPersistenceManager ppm, T staTable);

    /**
     * Get the field this mapper operates on, with their types.
     *
     * @return the map of field names and types.
     */
    public Map<String, String> getFieldTypes();
}
