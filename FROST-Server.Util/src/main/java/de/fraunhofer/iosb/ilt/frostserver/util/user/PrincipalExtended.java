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
package de.fraunhofer.iosb.ilt.frostserver.util.user;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

/**
 * A user Principal with role information.
 */
public class PrincipalExtended implements Principal {

    private static final ThreadLocal<PrincipalExtended> LOCAL_PRINCIPAL = new ThreadLocal<>();

    public static final String USER_NAME_ANONYMOUS = "anonymous";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_DELETE = "delete";
    public static final String ROLE_UPDATE = "update";
    public static final String ROLE_CREATE = "create";
    public static final String ROLE_READ = "read";
    public static final PrincipalExtended ANONYMOUS_PRINCIPAL = new PrincipalExtended(USER_NAME_ANONYMOUS, false, Collections.emptyList());
    public static final PrincipalExtended INTERNAL_ADMIN_PRINCIPAL = new PrincipalExtended("admin", true, Collections.emptyList());

    private final String name;
    private final boolean admin;
    private final List<String> roles;

    public PrincipalExtended(String name, boolean admin, List<String> roles) {
        this.name = name;
        this.admin = admin;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isAdmin() {
        return admin;
    }

    public List<String> getRoles() {
        return roles;
    }

    /**
     * Turns the given principal into a PrincipalExtended. A null value will
     * turn into an anonymous principal extended.
     *
     * @param principal the principal to check, or null.
     * @return A principalExtended.
     */
    public static final PrincipalExtended fromPrincipal(Principal principal) {
        if (principal == null) {
            return ANONYMOUS_PRINCIPAL;
        }
        if (principal instanceof PrincipalExtended principalExtended) {
            return principalExtended;
        }
        return new PrincipalExtended(principal.getName(), false, Collections.emptyList());
    }

    public static PrincipalExtended getLocalPrincipal() {
        return LOCAL_PRINCIPAL.get();
    }

    public static void setLocalPrincipal(PrincipalExtended localPrincipal) {
        LOCAL_PRINCIPAL.set(localPrincipal);
    }

    public static void removeLocalPrincipal() {
        LOCAL_PRINCIPAL.remove();
    }
}
