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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;

public class CsdlPropertyNavigation implements CsdlProperty {

    @JsonProperty("$Kind")
    public String kind = "NavigationProperty";

    @JsonProperty("$Collection")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public Boolean collection;

    @JsonProperty("$Partner")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String partner;

    @JsonProperty("$Type")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String type;

    @JsonProperty("$Nullable")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public Boolean nullable;

    public CsdlPropertyNavigation generateFrom(String nameSpace, CoreSettings settings, EntityType et, NavigationPropertyMain np) {
        type = nameSpace + "." + np.getType().getName();
        final NavigationPropertyMain inverse = np.getInverse();
        if (inverse != null) {
            partner = inverse.getName();
        }
        if (np.isEntitySet()) {
            collection = true;
        }
        if (et.isRequired(np)) {
            nullable = false;
        }
        return this;
    }

}
