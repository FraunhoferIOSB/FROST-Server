/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.path;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;

/**
 * The versions that FROST supports.
 *
 * @author scf
 */
public class Version {

    private static final SyntheticPropertyRegistry spr = new SyntheticPropertyRegistry();
    public static final Version V_1_0 = new Version("v1.0", spr);
    public static final Version V_1_1 = new Version("v1.1", spr);

    static {
        spr.registerProperty(ModelRegistry.EP_SELFLINK);
    }

    public final String urlPart;
    public final SyntheticPropertyRegistry syntheticPropertyRegistry;

    public Version(String urlPart) {
        this.urlPart = urlPart;
        syntheticPropertyRegistry = new SyntheticPropertyRegistry();
    }

    public Version(String urlPart, SyntheticPropertyRegistry syntheticPropertyRegistry) {
        this.urlPart = urlPart;
        this.syntheticPropertyRegistry = syntheticPropertyRegistry;
    }

    @Override
    public String toString() {
        return urlPart;
    }

}
