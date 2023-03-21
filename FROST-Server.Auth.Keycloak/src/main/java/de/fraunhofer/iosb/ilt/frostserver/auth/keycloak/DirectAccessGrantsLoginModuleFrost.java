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
package de.fraunhofer.iosb.ilt.frostserver.auth.keycloak;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.jaas.DirectAccessGrantsLoginModule;

/**
 * Creates its KeycloakDeployment from the CoreSettings instead of a file.
 *
 * @author scf
 */
public class DirectAccessGrantsLoginModuleFrost extends DirectAccessGrantsLoginModule {

    private final CoreSettings coreSettings;

    public DirectAccessGrantsLoginModuleFrost(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }

    @Override
    protected KeycloakDeployment resolveDeployment(String keycloakConfigFile) {
        return Utils.resolveDeployment(coreSettings);
    }

}
