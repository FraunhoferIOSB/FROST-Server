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
package de.fraunhofer.iosb.ilt.frostserver.path;

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NAVIGATION_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NEXT_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import java.util.Map;
import java.util.TreeMap;

/**
 * The versions that FROST supports.
 *
 * @author scf
 */
public class Version {

    private static final SyntheticPropertyRegistry spr = new SyntheticPropertyRegistry();
    public static final String VERSION_STA_V10_NAME = "v1.0";
    public static final String VERSION_STA_V11_NAME = "v1.1";
    public static final Version V_1_0 = new Version(VERSION_STA_V10_NAME, spr, AT_IOT_COUNT, AT_IOT_ID, AT_IOT_SELF_LINK, AT_IOT_NEXT_LINK, AT_IOT_NAVIGATION_LINK);
    public static final Version V_1_1 = new Version(VERSION_STA_V11_NAME, spr, AT_IOT_COUNT, AT_IOT_ID, AT_IOT_SELF_LINK, AT_IOT_NEXT_LINK, AT_IOT_NAVIGATION_LINK);

    static {
        spr.registerProperty(ModelRegistry.EP_SELFLINK);
    }

    public final String urlPart;
    public final SyntheticPropertyRegistry syntheticPropertyRegistry;
    public final Map<CannedResponseType, CannedResponse> responses = new TreeMap<>();
    public final String countName;
    public final String idName;
    public final String navLinkName;
    public final String nextLinkName;
    public final String selfLinkName;

    public Version(String urlPart, String countName, String idName, String navLinkName, String nextLinkName, String selfLinkName) {
        this(urlPart, new SyntheticPropertyRegistry(), countName, idName, selfLinkName, nextLinkName, navLinkName);
    }

    public Version(String urlPart, SyntheticPropertyRegistry spr, String countName, String idName, String selfLinkName, String nextLinkName, String navLinkName) {
        this.urlPart = urlPart;
        this.syntheticPropertyRegistry = spr;
        this.countName = countName;
        this.idName = idName;
        this.selfLinkName = selfLinkName;
        this.nextLinkName = nextLinkName;
        this.navLinkName = navLinkName;
    }

    @Override
    public String toString() {
        return urlPart;
    }

    public CannedResponse getCannedResponse(CannedResponseType type) {
        return responses.getOrDefault(type, type.dflt);
    }

    public enum CannedResponseType {
        NOTHING_FOUND(new CannedResponse(404, "Not Found"));

        public final CannedResponse dflt;

        private CannedResponseType(CannedResponse dflt) {
            this.dflt = dflt;
        }
    }

    public static class CannedResponse {

        public final int code;
        public final String message;

        public CannedResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public String getCountName() {
        return countName;
    }

    public String getIdName() {
        return idName;
    }

    public String getNavLinkName() {
        return navLinkName;
    }

    public String getNextLinkName() {
        return nextLinkName;
    }

    public String getSelfLinkName() {
        return selfLinkName;
    }

}
