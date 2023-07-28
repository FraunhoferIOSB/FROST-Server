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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Batch of requests or responses.
 *
 * @param <C> The type of content.
 */
public abstract class Batch<C extends Content> implements Content {

    protected final CoreSettings settings;

    protected final List<Part<C>> parts = new ArrayList<>();
    protected String logIndent = "";
    /**
     * Flag indicating there is a problem with the syntax of the content. If
     * this is a changeSet, then the entire changeSet will be discarded.
     */
    protected boolean parseFailed = false;
    protected final List<String> errors = new ArrayList<>();

    protected final boolean isChangeSet;
    protected final Version batchVersion;
    protected Principal userPrincipal;

    protected Batch(Version batchVersion, CoreSettings settings, boolean isChangeSet) {
        this.batchVersion = batchVersion;
        this.settings = settings;
        this.isChangeSet = isChangeSet;
    }

    public abstract boolean parse(ServiceRequest request);

    @Override
    public boolean isParseFailed() {
        return parseFailed;
    }

    @Override
    public List<String> getErrors() {
        return errors;
    }

    @Override
    public void setLogIndent(String logIndent) {
        this.logIndent = logIndent;
    }

    public List<Part<C>> getParts() {
        return parts;
    }

    public Batch<C> addPart(Part<C> part) {
        parts.add(part);
        return this;
    }

    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

}
