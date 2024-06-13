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
package de.fraunhofer.iosb.ilt.frostserver.auth.basic;

import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

class RequestWrapper extends HttpServletRequestWrapper {

    private final PrincipalExtended userPrincipal;
    private final HttpServletRequest wrapped;

    RequestWrapper(HttpServletRequest wrapped, PrincipalExtended userPrincipal) {
        super(wrapped);
        this.wrapped = wrapped;
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal == null ? wrapped.getUserPrincipal() : userPrincipal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return userPrincipal.getRoles().contains(role);
    }

}
