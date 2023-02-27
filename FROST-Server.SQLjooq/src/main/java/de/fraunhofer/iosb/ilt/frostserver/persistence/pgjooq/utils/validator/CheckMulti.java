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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorEnum;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorSubclass;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import java.util.Arrays;
import java.util.List;

/**
 * A check the consists of one or more sub-checks.
 */
public class CheckMulti implements ValidationCheck {

    public enum Type {
        AND,
        OR
    }

    @ConfigurableField(editor = EditorEnum.class,
            label = "Combine Type", description = "How to combine checks. Executed as fail-fast.")
    @EditorEnum.EdOptsEnum(sourceType = Type.class, dflt = "AND")
    private Type combineType;

    @ConfigurableField(editor = EditorList.class,
            label = "Sub Checks", description = "The sub-checks to execute.")
    @EditorList.EdOptsList(editor = EditorSubclass.class)
    @EditorSubclass.EdOptsSubclass(iface = ValidationCheck.class, merge = true, nameField = "@class")
    private List<ValidationCheck> subChecks;

    @Override
    public boolean check(JooqPersistenceManager pm, Entity context) {
        if (getCombineType() == Type.AND) {
            for (ValidationCheck check : getSubChecks()) {
                if (!check.check(pm, context)) {
                    return false;
                }
            }
            return true;
        } else {
            for (ValidationCheck check : getSubChecks()) {
                if (check.check(pm, context)) {
                    return true;
                }
            }
            return false;
        }
    }

    public Type getCombineType() {
        return combineType;
    }

    public CheckMulti setCombineType(Type combineType) {
        this.combineType = combineType;
        return this;
    }

    public List<ValidationCheck> getSubChecks() {
        return subChecks;
    }

    public CheckMulti setSubChecks(List<ValidationCheck> subChecks) {
        this.subChecks = subChecks;
        return this;
    }

    @Override
    public String toString() {
        return "CheckMulti: " + combineType + " " + Arrays.toString(subChecks.toArray());
    }

}
