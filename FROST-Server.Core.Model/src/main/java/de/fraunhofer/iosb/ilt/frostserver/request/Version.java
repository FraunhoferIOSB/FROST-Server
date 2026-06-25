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
package de.fraunhofer.iosb.ilt.frostserver.request;

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NAVIGATION_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NEXT_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;

import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import java.util.Map;
import java.util.TreeMap;

/**
 * The version definition.
 */
public class Version {

    public static final Version INTERNAL = builder()
            .setUrlPart("int")
            .setCountName(AT_IOT_COUNT)
            .setIdName(AT_IOT_ID)
            .setSelfLinkName(AT_IOT_SELF_LINK)
            .setNextLinkName(AT_IOT_NEXT_LINK)
            .setNavLinkName(AT_IOT_NAVIGATION_LINK)
            .setCreateFeatures(EditFeatures.NONE)
            .setUpdateFeatures(EditFeatures.NONE)
            .registerSytheticProperty(StandardProperties.EP_SELFLINK)
            .setMqttNotifyDelete(false)
            .setMqttFullUrls(false)
            .build();

    public final String urlPart;
    public final SyntheticPropertyRegistry syntheticPropertyRegistry;
    public final Map<CannedResponseType, CannedResponse> responses;
    public final String countName;
    public final String idName;
    public final String navLinkName;
    public final String nextLinkName;
    public final String selfLinkName;
    public final EditFeatures createFeatures;
    public final EditFeatures updateFeatures;
    public final boolean mqttNotifyDelete;
    public final boolean mqttFullUrls;

    public static class Builder {

        private String urlPart;
        private String countName;
        private String idName;
        private String navLinkName;
        private String nextLinkName;
        private String selfLinkName;
        private EditFeatures createFeatures;
        private EditFeatures updateFeatures;
        private SyntheticPropertyRegistry spr = new SyntheticPropertyRegistry();
        private Map<CannedResponseType, CannedResponse> responses = new TreeMap<>();
        private boolean mqttNotifyDelete;
        private boolean mqttFullUrls;

        public Builder setUrlPart(String urlPart) {
            this.urlPart = urlPart;
            return this;
        }

        public Builder setSyntheticPropertyRegistry(SyntheticPropertyRegistry spr) {
            this.spr = spr;
            return this;
        }

        public Builder registerSytheticProperty(EntityPropertyMain<?> property) {
            spr.registerProperty(property);
            return this;
        }

        public Builder registerSytheticProperty(String externalName, EntityPropertyMain<?> property) {
            spr.registerProperty(externalName, property);
            return this;
        }

        public Builder addResponse(CannedResponseType type, CannedResponse response) {
            responses.put(type, response);
            return this;
        }

        public Builder setCountName(String countName) {
            this.countName = countName;
            return this;
        }

        public Builder setIdName(String idName) {
            this.idName = idName;
            return this;
        }

        public Builder setMqttNotifyDelete(boolean mqttNotifyDelete) {
            this.mqttNotifyDelete = mqttNotifyDelete;
            return this;
        }

        public Builder setMqttFullUrls(boolean mqttFullUrls) {
            this.mqttFullUrls = mqttFullUrls;
            return this;
        }

        public Builder setNavLinkName(String navLinkName) {
            this.navLinkName = navLinkName;
            return this;
        }

        public Builder setNextLinkName(String nextLinkName) {
            this.nextLinkName = nextLinkName;
            return this;
        }

        public Builder setSelfLinkName(String selfLinkName) {
            this.selfLinkName = selfLinkName;
            return this;
        }

        public Builder setCreateFeatures(EditFeatures createFeatures) {
            this.createFeatures = createFeatures;
            return this;
        }

        public Builder setUpdateFeatures(EditFeatures updateFeatures) {
            this.updateFeatures = updateFeatures;
            return this;
        }

        public Version build() {
            return new Version(this);
        }
    }

    public static Builder builder() {
        return new Builder();
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

    private Version(Builder builder) {
        this.urlPart = builder.urlPart;
        this.syntheticPropertyRegistry = builder.spr;
        this.countName = builder.countName;
        this.idName = builder.idName;
        this.selfLinkName = builder.selfLinkName;
        this.nextLinkName = builder.nextLinkName;
        this.navLinkName = builder.navLinkName;
        this.createFeatures = builder.createFeatures;
        this.updateFeatures = builder.updateFeatures;
        this.responses = builder.responses;
        this.mqttNotifyDelete = builder.mqttNotifyDelete;
        this.mqttFullUrls = builder.mqttFullUrls;
    }

    @Override
    public String toString() {
        return urlPart;
    }

    public CannedResponse getCannedResponse(CannedResponseType type) {
        return responses.getOrDefault(type, type.dflt);
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

    public boolean getMqttCanDelete() {
        return mqttNotifyDelete;
    }

    public boolean getMqttFullUrls() {
        return mqttFullUrls;
    }

}
