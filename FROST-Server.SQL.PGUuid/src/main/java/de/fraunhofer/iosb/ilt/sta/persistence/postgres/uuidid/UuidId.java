/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid;

import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author scf
 */
public class UuidId implements Id {

    private UUID value;

    public UuidId(UUID value) {
        this.value = value;
    }

    public UuidId(String value) {
        this.value = UUID.fromString(value);
    }

    @Override
    public BasicPersistenceType getBasicPersistenceType() {
        return BasicPersistenceType.BYTEARRAY;
    }

    @Override
    public Object asBasicPersistenceType() {
        return value;
    }

    @Override
    public void fromBasicPersitenceType(Object data) {
        if (data instanceof UUID) {
            value = (UUID) data;
        } else {
            value = UUID.fromString(data.toString());
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UuidId other = (UuidId) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public UUID getValue() {
        return value;
    }

    @Override
    public String getUrl() {
        return "'" + value.toString() + "'";
    }

    @Override
    public String toString() {
        if (value == null) {
            return "null";
        }
        return value.toString();
    }

}
