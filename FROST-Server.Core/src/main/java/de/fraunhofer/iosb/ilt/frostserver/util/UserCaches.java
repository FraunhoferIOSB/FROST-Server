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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserClientInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Various caches for auth systems.
 */
public class UserCaches {

    private final Map<String, UserClientInfo> userinfoByClientid = new ConcurrentHashMap<>();
    private final Map<String, UserClientInfo> userinfoByUsername = new ConcurrentHashMap<>();
    private final Map<Long, PrincipalExtended> principalByKey = new ConcurrentHashMap<>();

    private int maxClientsPerUser;

    public void setMaxClientsPerUser(int maxClientsPerUser) {
        this.maxClientsPerUser = maxClientsPerUser;
    }

    public UserClientInfo getOrCreateUserInfo(String userName) {
        return userinfoByUsername.computeIfAbsent(userName, t -> new UserClientInfo());
    }

    public UserClientInfo getUserInfo(String clientId) {
        return userinfoByClientid.get(clientId);
    }

    /**
     * Get the extended principal for the given clientId. If the clientId is
     * unknown, the anonymous user principal is returned.
     *
     * @param clientId the clientId of the connection to get the userPrincipal
     * for.
     * @return The userPrincipal.
     */
    public PrincipalExtended getUserPrincipal(String clientId) {
        UserClientInfo userInfo = userinfoByClientid.get(clientId);
        if (userInfo == null) {
            return PrincipalExtended.ANONYMOUS_PRINCIPAL;
        }
        return userInfo.getUserPrincipal();
    }

    public PrincipalExtended getUserPrincipal(long userKey) {
        PrincipalExtended principal = principalByKey.get(userKey);
        if (principal == null) {
            return PrincipalExtended.ANONYMOUS_PRINCIPAL;
        }
        return principal;
    }

    public void registerPrincipal(long key, PrincipalExtended userPrincipal) {
        principalByKey.put(key, userPrincipal);
    }

    public void registerClientId(UserClientInfo userInfo, String clientId) {
        String oldClientId = userInfo.addClientId(clientId, maxClientsPerUser);
        if (oldClientId != null) {
            userinfoByClientid.remove(oldClientId);
        }
        userinfoByClientid.put(clientId, userInfo);

    }
}
