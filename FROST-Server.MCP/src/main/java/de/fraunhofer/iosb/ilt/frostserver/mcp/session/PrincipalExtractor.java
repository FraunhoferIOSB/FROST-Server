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
package de.fraunhofer.iosb.ilt.frostserver.mcp.session;

import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

/**
 * Extracts the authenticated principal from each HTTP request and stores it in
 * the MCP transport context, so tool handlers can propagate it to the FROST
 * Service layer. Called by the transport on every HTTP request, including
 * initialize.
 */
public class PrincipalExtractor implements McpTransportContextExtractor<HttpServletRequest> {

    public static final String KEY_PRINCIPAL = "frost.principal";

    @Override
    public McpTransportContext extract(HttpServletRequest request) {
        final Principal userPrincipal = request.getUserPrincipal();
        PrincipalExtended principal = PrincipalExtended.fromPrincipal(userPrincipal);
        if (principal == null || principal.getName() == null || principal.getName().isEmpty()) {
            principal = PrincipalExtended.ANONYMOUS_PRINCIPAL;
        }
        return McpTransportContext.create(Map.of(KEY_PRINCIPAL, principal));
    }

    /**
     * Fetch the FROST principal from a transport context.
     *
     * @param context The transport context, may be null.
     * @return the principal, or the anonymous principal if absent.
     */
    public static PrincipalExtended principalFrom(McpTransportContext context) {
        if (context == null) {
            return PrincipalExtended.ANONYMOUS_PRINCIPAL;
        }
        final Object value = context.get(KEY_PRINCIPAL);
        if (value instanceof PrincipalExtended principal) {
            return principal;
        }
        return PrincipalExtended.ANONYMOUS_PRINCIPAL;
    }
}
