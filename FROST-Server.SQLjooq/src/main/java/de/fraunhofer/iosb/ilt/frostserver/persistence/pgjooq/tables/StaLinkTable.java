/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

/**
 *
 * @author hylke
 * @param <J> The type of the ID fields.
 * @param <T> The exact type of the implementing class.
 */
public abstract class StaLinkTable<J extends Comparable, T extends StaLinkTable<J, T>> extends TableImpl<Record> implements StaTable<J, T> {

    private final DataType<J> idType;

    protected StaLinkTable(DataType<J> idType, Name alias, StaLinkTable<J, T> aliased) {
        super(alias, null, aliased);
        this.idType = idType;
    }

    public DataType<J> getIdType() {
        return idType;
    }

    @Override
    public abstract StaLinkTable<J, T> as(Name as);

    @Override
    public abstract StaLinkTable<J, T> as(String alias);

}
