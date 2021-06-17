/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.statests.c04batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author hylke
 */
public class BatchResponseJson {

    private List<ResponsePart> responses = new ArrayList<>();

    /**
     * @return the responses
     */
    public List<ResponsePart> getResponses() {
        return responses;
    }

    /**
     * @param responses the responses to set
     */
    public void setResponses(List<ResponsePart> responses) {
        this.responses = responses;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.responses);
        return hash;
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
        final BatchResponseJson other = (BatchResponseJson) obj;
        return Objects.equals(this.responses, other.responses);
    }

    public static class ResponsePart {

        private String id;
        private int status;
        private Map<String, Object> body;
        private String location;

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the status
         */
        public int getStatus() {
            return status;
        }

        /**
         * @param status the status to set
         */
        public void setStatus(int status) {
            this.status = status;
        }

        /**
         * @return the body
         */
        public Map<String, Object> getBody() {
            return body;
        }

        /**
         * @param body the body to set
         */
        public void setBody(Map<String, Object> body) {
            this.body = body;
        }

        /**
         * @return the location
         */
        public String getLocation() {
            return location;
        }

        /**
         * @param location the location to set
         */
        public void setLocation(String location) {
            this.location = location;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + Objects.hashCode(id);
            hash = 79 * hash + status;
            if (status >= 200 && status < 300) {
                hash = 79 * hash + Objects.hashCode(body);
            }
            hash = 79 * hash + Objects.hashCode(location);
            return hash;
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
            final ResponsePart other = (ResponsePart) obj;
            if (status != other.status) {
                return false;
            }
            if (!Objects.equals(id, other.id)) {
                return false;
            }
            if (!Objects.equals(location, other.location)) {
                return false;
            }
            if (status >= 200 && status < 300) {
                if (!Objects.equals(body, other.body)) {
                    return false;
                }
            }
            return true;
        }

    }

}
