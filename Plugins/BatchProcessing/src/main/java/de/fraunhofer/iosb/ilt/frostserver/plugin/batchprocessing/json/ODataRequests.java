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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json;

import java.util.List;

/**
 * OData Batch request.
 */
public class ODataRequests {

    public class ODataRequest {

        public final String id;

        public final String atomicityGroup;

        public final String method;

        public final String url;

        public final Object body;

        public ODataRequest(String id, String atomicityGroup, String method, String url, Object body) {
            this.id = id;
            this.atomicityGroup = atomicityGroup;
            this.method = method;
            this.url = url;
            this.body = body;
        }
    }

    public final List<ODataRequest> requests;

    public ODataRequests(List<ODataRequest> requests) {
        super();
        this.requests = requests;
    }

}
