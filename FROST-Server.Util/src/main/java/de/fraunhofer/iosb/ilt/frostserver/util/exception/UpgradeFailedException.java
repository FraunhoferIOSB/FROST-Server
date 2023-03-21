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
 * The exception that should be thrown by a PersistenceManager when upgrading
 * the database fails.
 *
 * @author Hylke van der Schaaf
 */
public class UpgradeFailedException extends Exception {

    public UpgradeFailedException() {
    }

    public UpgradeFailedException(String message) {
        super(message);
    }

    public UpgradeFailedException(Throwable cause) {
        super(cause);
    }

    public UpgradeFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
