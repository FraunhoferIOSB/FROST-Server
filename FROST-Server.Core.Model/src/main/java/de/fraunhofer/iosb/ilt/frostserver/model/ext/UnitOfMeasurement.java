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
package de.fraunhofer.iosb.ilt.frostserver.model.ext;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.util.Objects;

/**
 * Model class for UnitOfMeasurement. This is not a first class entity in STA.
 *
 * @author jab
 */
public class UnitOfMeasurement {

    private String name;
    private String symbol;
    private String definition;

    public UnitOfMeasurement() {
    }

    public UnitOfMeasurement(
            String name,
            String symbol,
            String definition) {
        this.name = name;
        this.symbol = symbol;
        this.definition = definition;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param name the name to set
     * @return this
     */
    public UnitOfMeasurement setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param symbol the symbol to set
     * @return this
     */
    public UnitOfMeasurement setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    /**
     * @param definition the definition to set
     * @return this
     */
    public UnitOfMeasurement setDefinition(String definition) {
        this.definition = definition;
        return this;
    }

    @Override
    public String toString() {
        try {
            return SimpleJsonMapper.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            return super.toString();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, symbol, definition);
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
        final UnitOfMeasurement other = (UnitOfMeasurement) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.symbol, other.symbol)) {
            return false;
        }
        return Objects.equals(this.definition, other.definition);
    }
}
