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
package de.fraunhofer.iosb.ilt.statests.f01auth;

import static de.fraunhofer.iosb.ilt.frostclient.model.property.type.TypePrimitive.EDM_STRING;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_DESCRIPTION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_ID;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_NAME;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_THING;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_THINGS;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntityType;
import de.fraunhofer.iosb.ilt.frostclient.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostclient.models.DataModel;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.MapValue;
import java.util.Map;

/**
 *
 */
public class SensorThingsUserModel implements DataModel {

    private static final String NAME_USER = "User";
    private static final String NAME_USERS = "Users";
    private static final String NAME_ROLE = "Role";
    private static final String NAME_ROLES = "Roles";
    private static final String NAME_PROJECT = "Project";
    private static final String NAME_PROJECTS = "Projects";
    private static final String NAME_USERPROJECTROLE = "UserProjectRole";
    private static final String NAME_USERPROJECTROLES = "UserProjectRoles";

    private static final String NAME_EP_USERNAME = "username";
    private static final String NAME_EP_USERPASS = "userpass";
    private static final String NAME_EP_ROLENAME = "rolename";

    public static final EntityProperty<String> EP_USERNAME = new EntityPropertyMain<>(NAME_EP_USERNAME, EDM_STRING);
    public static final EntityProperty<String> EP_USERPASS = new EntityPropertyMain<>(NAME_EP_USERPASS, EDM_STRING);
    public static final EntityProperty<String> EP_ROLENAME = new EntityPropertyMain<>(NAME_EP_ROLENAME, EDM_STRING);

    public final NavigationPropertyEntitySet npUserRoles = new NavigationPropertyEntitySet(NAME_ROLES);
    public final NavigationPropertyEntitySet npUserUserprojectroles = new NavigationPropertyEntitySet(NAME_USERPROJECTROLES);

    public final NavigationPropertyEntitySet npRoleUsers = new NavigationPropertyEntitySet(NAME_USERS, npUserRoles);
    public final NavigationPropertyEntitySet npRoleUserprojectroles = new NavigationPropertyEntitySet(NAME_USERPROJECTROLES);

    public final NavigationPropertyEntity npUserprojectroleUser = new NavigationPropertyEntity(NAME_USER, npUserUserprojectroles);
    public final NavigationPropertyEntity npUserprojectroleRole = new NavigationPropertyEntity(NAME_ROLE, npRoleUserprojectroles);
    public final NavigationPropertyEntity npUserprojectroleProject = new NavigationPropertyEntity(NAME_PROJECT);

    public final NavigationPropertyEntitySet npProjectUserprojectroles = new NavigationPropertyEntitySet(NAME_USERPROJECTROLES, npUserprojectroleProject);
    public final NavigationPropertyEntitySet npProjectThings = new NavigationPropertyEntitySet(NAME_THINGS);

    public final NavigationPropertyEntitySet npThingProjects = new NavigationPropertyEntitySet(NAME_PROJECTS, npProjectThings);

    public final EntityType etUser = new EntityType(NAME_USER, NAME_USERS);
    public final EntityType etRole = new EntityType(NAME_ROLE, NAME_ROLES);
    public final EntityType etProject = new EntityType(NAME_PROJECT, NAME_PROJECTS);
    public final EntityType etUserProjectRole = new EntityType(NAME_USERPROJECTROLE, NAME_USERPROJECTROLES);

    public ModelRegistry mr;

    public SensorThingsUserModel() {
    }

    @Override
    public final void init(SensorThingsService service, ModelRegistry modelRegistry) {
        if (this.mr != null) {
            throw new IllegalArgumentException("Already initialised.");
        }
        this.mr = modelRegistry;
        mr.addDataModel(this);

        mr.registerEntityType(etUser);
        mr.registerEntityType(etRole);
        mr.registerEntityType(etProject);
        mr.registerEntityType(etUserProjectRole);

        etUser
                .registerProperty(EP_USERNAME)
                .registerProperty(EP_USERPASS)
                .registerProperty(npUserRoles)
                .registerProperty(npUserUserprojectroles);

        etRole
                .registerProperty(EP_ROLENAME)
                .registerProperty(EP_DESCRIPTION)
                .registerProperty(EP_PROPERTIES)
                .registerProperty(npRoleUsers)
                .registerProperty(npRoleUserprojectroles);

        etProject
                .registerProperty(EP_ID)
                .registerProperty(EP_NAME)
                .registerProperty(EP_DESCRIPTION)
                .registerProperty(EP_PROPERTIES)
                .registerProperty(npProjectThings)
                .registerProperty(npProjectUserprojectroles);

        etUserProjectRole
                .registerProperty(EP_ID)
                .registerProperty(npUserprojectroleUser)
                .registerProperty(npUserprojectroleRole)
                .registerProperty(npUserprojectroleProject);

        mr.getEntityTypeForName(NAME_THING).registerProperty(npThingProjects);
    }

    @Override
    public boolean isInitialised() {
        return mr != null;
    }

    public ModelRegistry getModelRegistry() {
        return mr;
    }

    public Entity newUser() {
        return new Entity(etUser);
    }

    public Entity newUser(String username, String password) {
        return newUser()
                .setProperty(EP_USERNAME, username)
                .setProperty(EP_USERPASS, password);
    }

    public Entity newRole() {
        return new Entity(etRole);
    }

    public Entity newRole(String rolename, String description) {
        return newRole()
                .setProperty(EP_ROLENAME, rolename)
                .setProperty(EP_DESCRIPTION, description);
    }

    public Entity newRole(String rolename, String description, MapValue properties) {
        return newRole(rolename, description)
                .setProperty(EP_PROPERTIES, properties);
    }

    public Entity newRole(String rolename, String description, Map<String, Object> properties) {
        return newRole(rolename, description, new MapValue(properties));
    }

    public Entity newProject() {
        return new Entity(etProject);
    }

    public Entity newProject(String projectname, String description) {
        return newProject()
                .setProperty(EP_NAME, projectname)
                .setProperty(EP_DESCRIPTION, description);
    }

    public Entity newProject(String rolename, String description, MapValue properties) {
        return newProject(rolename, description)
                .setProperty(EP_PROPERTIES, properties);
    }

    public Entity newProject(String rolename, String description, Map<String, Object> properties) {
        return newProject(rolename, description, new MapValue(properties));
    }

    public Entity newUserProjectRole() {
        return new Entity(etUserProjectRole);
    }

    public Entity newUserProjectRole(Object... pk) {
        return newUserProjectRole()
                .setPrimaryKeyValues(new PkValue(pk));
    }

    public Entity newUserProjectRole(Entity user, Entity project, Entity role) {
        return newUserProjectRole()
                .setProperty(npUserprojectroleUser, user)
                .setProperty(npUserprojectroleProject, project)
                .setProperty(npUserprojectroleRole, role);
    }

}
