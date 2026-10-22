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
package de.fraunhofer.iosb.ilt.frostserver.mcp.tool;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * A FROST MCP tool: knows its tool name and builds its own sync tool
 * registration. Implemented by frost_get/-query/-create/-update/-delete;
 * FrostMcpServer uses it to filter registrations on mcp.allowTools.
 */
public interface FrostTool {

    /**
     * The full tool name, e.g. "frost_get".
     *
     * @return the tool name.
     */
    String toolName();

    /**
     * The sync tool registration.
     *
     * @return the sync tool specification.
     */
    McpServerFeatures.SyncToolSpecification specification();
}
