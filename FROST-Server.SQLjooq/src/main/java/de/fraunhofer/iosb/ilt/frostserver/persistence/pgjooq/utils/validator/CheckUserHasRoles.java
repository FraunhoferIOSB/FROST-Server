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
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.query.PrincipalExtended;
import java.util.List;

public class CheckUserHasRoles implements ValidationCheck {

    public enum Type {
        ANY,
        ALL
    }

    @ConfigurableField(editor = EditorEnum.class,
            label = "Check Type", description = "Should the user be in ANY of the listed groups, or in ALL of the groups.")
    @EditorEnum.EdOptsEnum(sourceType = Type.class, dflt = "ANY")
    private Type vheckType;

    @ConfigurableField(editor = EditorList.class,
            label = "Groups", description = "The groups to check the user's groups against.")
    @EditorList.EdOptsList(editor = EditorString.class)
    @EditorString.EdOptsString()
    private List<String> roles;

    @Override
    public boolean check(PostgresPersistenceManager pm, Entity context) {
        final List<String> userRoles = PrincipalExtended.getLocalPrincipal().getRoles();
        if (getVheckType() == Type.ANY) {
            for (String checkRole : getRoles()) {
                if (userRoles.contains(checkRole)) {
                    return true;
                }
            }
            return false;
        } else {
            return userRoles.containsAll(getRoles());
        }
    }

    public Type getVheckType() {
        return vheckType;
    }

    public void setVheckType(Type vheckType) {
        this.vheckType = vheckType;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

}
