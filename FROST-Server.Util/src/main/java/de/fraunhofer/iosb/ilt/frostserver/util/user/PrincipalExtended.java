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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A user Principal with role information.
 */
public class PrincipalExtended implements Principal {

    public static final String USER_NAME_ANONYMOUS = "anonymous";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_DELETE = "delete";
    public static final String ROLE_UPDATE = "update";
    public static final String ROLE_CREATE = "create";
    public static final String ROLE_READ = "read";
    public static final PrincipalExtended ANONYMOUS_PRINCIPAL = new PrincipalExtended(USER_NAME_ANONYMOUS, false, Collections.emptySet());
    public static final PrincipalExtended INTERNAL_ADMIN_PRINCIPAL = new PrincipalExtended(ROLE_ADMIN, true, Collections.emptySet());

    private static final ThreadLocal<PrincipalExtended> LOCAL_PRINCIPAL = ThreadLocal.withInitial(() -> ANONYMOUS_PRINCIPAL);

    private final String name;
    private final boolean admin;
    private final Set<String> roles;
    private Map<String, Object> context;

    public PrincipalExtended(String name, boolean admin, Set<String> roles) {
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

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return name + " (" + admin + ")";
    }

    /**
     * Get the current context map.
     *
     * @return the current context map.
     */
    public Map<String, Object> getContext() {
        return context;
    }

    /**
     * Add an item to the context, or replace an existing item if an item with
     * the given key already exists.
     *
     * @param key The key to store the item under.
     * @param value The value to associate with the given key.
     * @return this.
     */
    public PrincipalExtended addContextItem(String key, Object value) {
        if (context == null) {
            context = new HashMap<>();
        }
        context.put(key, value);
        return this;
    }

    /**
     * Check if the context has an item for the given key.
     *
     * @param key the key to check.
     * @return true if there is a context item for the given key, false
     * otherwise.
     */
    public boolean hasContextItem(String key) {
        if (context == null) {
            return false;
        }
        return context.containsKey(key);
    }

    /**
     * Get the context item with the given key, or dflt if there is no such
     * item.
     *
     * @param key The key to get the item for.
     * @param dflt The value to return if there is no value for the given key.
     * @return The value for the given key, or dflt if there is no such item.
     */
    public Object getContextItem(String key, Object dflt) {
        if (context == null) {
            return dflt;
        }
        Object value = context.get(key);
        if (value == null) {
            return dflt;
        }
        return value;
    }

    /**
     * Get the context item with the given key, or null if there is no such
     * item.
     *
     * @param key The key to get the item for.
     * @return The value for the given key, or null if there is no such item.
     */
    public Object getContextItem(String key) {
        return getContextItem(key, null);
    }

    /**
     * Replace the current context with the given value.
     *
     * @param context The context map to replace the current context with.
     * @return this
     */
    public PrincipalExtended setContext(Map<String, Object> context) {
        this.context = context;
        return this;
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
        return new PrincipalExtended(principal.getName(), false, Collections.emptySet());
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
