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
package de.fraunhofer.iosb.ilt.frostserver.util.exception;

/**
 * Throwing this exception will cause FROST-Server to return a 403 Forbidden.
 * The request contained valid data and was understood by the server, but the
 * server is refusing action. This may be due to the user not having the
 * necessary permissions for a resource or needing an account of some sort, or
 * attempting a prohibited action (e.g. creating a duplicate record where only
 * one is allowed). This code is also typically used if the request provided
 * authentication by answering the WWW-Authenticate header field challenge, but
 * the server did not accept that authentication. The request should not be
 * repeated.
 *
 * @author Hylke van der Schaaf
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(Throwable cause) {
        super(cause);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

}
