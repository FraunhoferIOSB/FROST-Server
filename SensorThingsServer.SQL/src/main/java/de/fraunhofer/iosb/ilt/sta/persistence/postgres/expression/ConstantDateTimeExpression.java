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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.expression;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.DateTimeExpression;
import java.sql.Timestamp;
import javax.annotation.Nullable;

/**
 *
 * @author scf
 */
public class ConstantDateTimeExpression extends DateTimeExpression<Timestamp> {

    /**
     * Flag indicating that the original time given was in utc.
     */
    private boolean utc = true;

    /**
     *
     * @param ts
     * @param utc Flag indicating that the original time given was in utc.
     */
    public ConstantDateTimeExpression(final Timestamp ts, boolean utc) {
        super(ConstantImpl.create(ts));
        this.utc = utc;
    }

    /**
     * @return Flag indicating that the original time given was in utc.
     */
    public boolean isUtc() {
        return utc;
    }

    @Override
    @Nullable
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit((Constant<Timestamp>) mixin, context);
    }

}
