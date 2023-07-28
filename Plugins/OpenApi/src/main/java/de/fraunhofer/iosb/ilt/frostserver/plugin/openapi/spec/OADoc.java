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
package de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An OpenAPI v 3.0.2 document.
 *
 * @author scf
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class OADoc {

    private String openapi = "3.0.2";
    private OADocInfo info;
    private final List<OAServer> servers = new ArrayList<>();
    private final Map<String, OAPath> paths = new TreeMap<>();
    private final OAComponents components = new OAComponents();

    /**
     * @return the openapi
     */
    public String getOpenapi() {
        return openapi;
    }

    /**
     * @param openapi the openapi to set
     */
    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }

    /**
     * @return the info
     */
    public OADocInfo getInfo() {
        return info;
    }

    /**
     * @param info the OADocInfo to set
     */
    public void setInfo(OADocInfo info) {
        this.info = info;
    }

    /**
     * Gets the paths defined in the document.
     *
     * @return the paths
     */
    public Map<String, OAPath> getPaths() {
        return paths;
    }

    public void addServer(OAServer server) {
        servers.add(server);
    }

    public List<OAServer> getServers() {
        return servers;
    }

    /**
     * @return the components
     */
    public OAComponents getComponents() {
        return components;
    }

}
