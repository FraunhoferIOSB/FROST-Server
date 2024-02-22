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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for userName, userPass and userRoles.
 */
public class UserData {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserData.class);
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MAX_USERNAME_LENGTH = 128;

    public final String userName;
    public final String userPass;
    public final Set<String> roles = new LinkedHashSet<>();

    /**
     * Create a new UserData with the standard maximum username and password
     * lengths.
     *
     * @param userName the user name to use.
     * @param userPass the password to use.
     */
    public UserData(String userName, String userPass) {
        this(userName, MAX_USERNAME_LENGTH, userPass, MAX_PASSWORD_LENGTH);
    }

    /**
     * Create a new UserData with the given maximum username and password
     * lengths.
     *
     * @param userName the user name to use.
     * @param maxNameLength the maximum length of the username to check for.
     * @param userPass the password to use.
     * @param maxPassLength the maximum length of the password to check for.
     */
    public UserData(String userName, int maxNameLength, String userPass, int maxPassLength) {
        if (userName != null && userName.length() > maxNameLength) {
            LOGGER.error("Password too long, aborting.");
            throw new IllegalArgumentException("Password too long.");
        }
        if (userPass != null && userPass.length() > maxPassLength) {
            LOGGER.error("Password too long, aborting.");
            throw new IllegalArgumentException("Password too long.");
        }
        this.userName = userName;
        this.userPass = userPass;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isEmpty() {
        return userName == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserData other = (UserData) obj;
        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }
        return Objects.equals(this.userPass, other.userPass);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.userName);
        hash = 71 * hash + Objects.hashCode(this.userPass);
        return hash;
    }

}
