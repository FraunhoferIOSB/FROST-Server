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
package de.fraunhofer.iosb.ilt.frostserver.mcp;

import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.CUSTOM_PROPS;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.NULLABLE;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;

/**
 * A compact fake model for the MCP unit tests: House and Room (regular
 * entity sets) plus Safe (admin-only), mirroring the pattern of the core
 * module's TestModel, with a free-form properties property to prove
 * model-agnosticism.
 */
public class TestModelMcp implements PluginService {

    public final EntityPropertyMain<String> epName = new EntityPropertyMain<>("name", TypeSimplePrimitive.EDM_STRING, REQUIRED);
    public final EntityPropertyMain<Number> epValue = new EntityPropertyMain<>("value", TypeSimplePrimitive.EDM_DECIMAL);
    public final EntityPropertyMain<Object> epMetadata = new EntityPropertyMain<>("metadata", TypeSimplePrimitive.EDM_UNTYPED, NULLABLE, CUSTOM_PROPS);

    public final NavigationPropertyEntity npRoomHouse = new NavigationPropertyEntity("House", REQUIRED);
    public final NavigationPropertyEntitySet npHouseRooms = new NavigationPropertyEntitySet("Rooms", npRoomHouse);

    public final EntityType etHouse = new EntityType("House", "Houses");
    public final EntityType etRoom = new EntityType("Room", "Rooms");
    public final EntityType etSafe = new EntityType("Safe", "Safes", true);

    /**
     * Registers the model on the given registry.
     *
     * @param modelRegistry The registry to register the types on.
     * @param idType The id type name, e.g. Constants.VALUE_ID_TYPE_LONG.
     */
    public void initModel(ModelRegistry modelRegistry, String idType) {
        modelRegistry.registerEntityType(etHouse);
        etHouse.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(epName)
                .registerProperty(epValue)
                .registerProperty(epMetadata)
                .registerProperty(StandardProperties.EP_PROPERTIES)
                .registerProperty(npHouseRooms);
        modelRegistry.registerEntityType(etRoom);
        etRoom.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(epName)
                .registerProperty(epValue)
                .registerProperty(StandardProperties.EP_PROPERTIES)
                .registerProperty(npRoomHouse)
                .registerProperty(npHouseRooms);
        modelRegistry.registerEntityType(etSafe);
        etSafe.registerProperty(new EntityPropertyMain<>(AT_IOT_ID, modelRegistry.getPropertyType(idType)).setAliases("id"))
                .registerProperty(epName);
        Assertions.assertEquals(2, modelRegistry.getEntityTypes().size(), "Safes must be admin-only and not counted.");
    }

    @Override
    public Collection<Version> getVersions() {
        return Arrays.asList(Version.INTERNAL);
    }

    @Override
    public boolean definesVersions() {
        return true;
    }

    @Override
    public Collection<String> getVersionedUrlPaths() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getRequestTypes() {
        return Collections.emptyList();
    }

    @Override
    public String getRequestTypeFor(Version version, String path, HttpMethod method, String contentType) {
        throw new UnsupportedOperationException("Not supported in the test model.");
    }

    @Override
    public ServiceResponse execute(Service mainService, ServiceRequest request, ServiceResponse response) {
        throw new UnsupportedOperationException("Not supported in the test model.");
    }

    @Override
    public InitResult init(CoreSettings settings) {
        settings.getPluginManager().registerPlugin(this);
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
