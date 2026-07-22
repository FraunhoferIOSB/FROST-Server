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
package de.fraunhofer.iosb.ilt.frostserver.util.exception;

import java.util.function.Supplier;
import org.apache.commons.lang3.Strings;

/**
 * A collection of helper methods for throwing exceptions.
 */
public class Exceptions {

    private static final String PREDICATE_FAILED = "Predicate failed.";

    private Exceptions() {
        // Not for initialisation.
    }

    public static final void illegalArgumentIf(boolean predicate, String message, Object param1) throws IllegalArgumentException {
        if (predicate) {
            throw new IllegalArgumentException(replacePlaceholders(message, new Object[]{param1}));
        }
    }

    public static final void illegalArgumentIf(boolean predicate, String message, Object param1, Object param2) throws IllegalArgumentException {
        if (predicate) {
            throw new IllegalArgumentException(replacePlaceholders(message, new Object[]{param1, param2}));
        }
    }

    public static final void illegalArgumentIf(boolean predicate, String message, Object... params) throws IllegalArgumentException {
        if (predicate) {
            throw new IllegalArgumentException(replacePlaceholders(message, params));
        }
    }

    public static final void illegalArgumentIf(boolean predicate, Supplier<String> message) throws IllegalArgumentException {
        if (predicate) {
            throw new IllegalArgumentException(message == null ? PREDICATE_FAILED : message.get());
        }
    }

    public static final IllegalArgumentException illegalArgument(String message, Object... params) {
        return new IllegalArgumentException(replacePlaceholders(message, params));
    }

    public static final void invalidSelfLinkIf(boolean predicate, String message, Object param1) throws InvalidSelfLinkException {
        if (predicate) {
            throw new InvalidSelfLinkException(replacePlaceholders(message, new Object[]{param1}));
        }
    }

    public static final void invalidSelfLinkIf(boolean predicate, String message, Object param1, Object param2) throws InvalidSelfLinkException {
        if (predicate) {
            throw new InvalidSelfLinkException(replacePlaceholders(message, new Object[]{param1, param2}));
        }
    }

    public static final void invalidSelfLinkIf(boolean predicate, String message, Object... params) throws InvalidSelfLinkException {
        if (predicate) {
            throw new InvalidSelfLinkException(replacePlaceholders(message, params));
        }
    }

    public static final void invalidSelfLinkIf(boolean predicate, Supplier<String> message) throws InvalidSelfLinkException {
        if (predicate) {
            throw new InvalidSelfLinkException(message == null ? PREDICATE_FAILED : message.get());
        }
    }

    public static final InvalidSelfLinkException invalidSelfLink(String message, Object... params) {
        return new InvalidSelfLinkException(replacePlaceholders(message, params));
    }

    public static final void unregisteredExpressionIf(boolean predicate, String message, Object param1) throws UnregisteredExpressionException {
        if (predicate) {
            throw new UnregisteredExpressionException(replacePlaceholders(message, new Object[]{param1}));
        }
    }

    public static final void unregisteredExpressionIf(boolean predicate, String message, Object param1, Object param2) throws UnregisteredExpressionException {
        if (predicate) {
            throw new UnregisteredExpressionException(replacePlaceholders(message, new Object[]{param1, param2}));
        }
    }

    public static final void unregisteredExpressionIf(boolean predicate, String message, Object... params) throws UnregisteredExpressionException {
        if (predicate) {
            throw new UnregisteredExpressionException(replacePlaceholders(message, params));
        }
    }

    public static final void unregisteredExpressionIf(boolean predicate, Supplier<String> message) throws UnregisteredExpressionException {
        if (predicate) {
            throw new UnregisteredExpressionException(message == null ? PREDICATE_FAILED : message.get());
        }
    }

    public static final UnregisteredExpressionException unregisteredExpression(String message, Object... params) {
        return new UnregisteredExpressionException(replacePlaceholders(message, params));
    }

    public static final void badPathIf(boolean predicate, String message, Object param1) throws BadPathException {
        if (predicate) {
            throw new BadPathException(replacePlaceholders(message, new Object[]{param1}));
        }
    }

    public static final void badPathIf(boolean predicate, String message, Object param1, Object param2) throws BadPathException {
        if (predicate) {
            throw new BadPathException(replacePlaceholders(message, new Object[]{param1, param2}));
        }
    }

    public static final void badPathIf(boolean predicate, String message, Object... params) throws BadPathException {
        if (predicate) {
            throw new BadPathException(replacePlaceholders(message, params));
        }
    }

    public static final void badPathIf(boolean predicate, Supplier<String> message) throws BadPathException {
        if (predicate) {
            throw new BadPathException(message == null ? PREDICATE_FAILED : message.get());
        }
    }

    public static final BadPathException badPath(String message, Object... params) {
        return new BadPathException(replacePlaceholders(message, params));
    }

    public static final String replacePlaceholders(String line, Object... params) {
        StringBuilder replaced = new StringBuilder();
        int idx = 0;
        for (var param : params) {
            int found = Strings.CS.indexOf(line, "{}", idx);
            if (found == -1) {
                break;
            }
            replaced.append(line.substring(idx, found))
                    .append(param);
            idx = found + 2;
        }
        if (idx < line.length()) {
            replaced.append(line.substring(idx));
        }
        return replaced.toString();
    }
}
