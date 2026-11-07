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
package de.fraunhofer.iosb.ilt.frostserver.mcp.subscription;

/**
 * A subscription failure that is not a parse error: a session over its
 * subscription cap, or an entity subscription whose target cannot be read.
 * Carries an HTTP-like code and message for the tool layer to render as an
 * isError tool result.
 */
public class FrostSubscriptionException extends Exception {

    private final int code;

    /**
     * Creates a new exception.
     *
     * @param code The HTTP-like status code (429, 404, 403).
     * @param message The human-readable message.
     */
    public FrostSubscriptionException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * The HTTP-like status code.
     *
     * @return the code.
     */
    public int getCode() {
        return code;
    }

}
