---
layout: default
title: Authentication
category: settings
order: 21
---

# Authentication and Authorisation

There are many ways to secure webservices. The easiest one is to use an external reverse proxy.
Since this can be tricky to set up and is limited in what it can do, FROST offers some simple auth* options.

Auth* has to happen in two places: in the HTTP package and in the MQTT package. To make things
flexible, authentication is handled by classes that implement an interface, just like the
persistence backends. The interface to be implemented is de.fraunhofer.iosb.ilt.sta.util.AuthProvider.

An example docker-compose file with basic auth set up can be found at: 
[docker-compose-separated-basicauth.yaml](https://github.com/FraunhoferIOSB/FROST-Server/blob/v2.x/scripts/docker-compose-separated-basicauth.yaml)

More fine-grained authorisation rules can be configured using:
* plugins with `EntityType` validators (for example [STAplus](https://github.com/securedimensions/FROST-Server-PLUS)),
* plugins with `Security Wrappers` and `Security Validators` (for example this [Demonstrator](https://github.com/hylkevds/FROST-Server.Plugin.SecurityTest/)),
* PostgreSQL Row-level security (see `persistence.transactionRole` setting).

Using `Security Wrappers` and `Security Validators` it is possible define in minute detail what a user is allowed to read, create, update and delete.
The rules for this can take into account what the relations are that each Entity has with other Entities, as deep as required.
Setting this up correctly is not trivial and a mistake in the authorisation may inadvertently open your data for reading or even editing.
Therefore, we strongly recommend you contact us for support if your use case requires fine-grained authorisation.


## Roles

FROST-Server asks the AuthProvider if the user has the following roles:

* **read:**  
  Can the user read, both on HTTP and MQTT.
* **create:**  
  Can the user create new entities, both on HTTP and MQTT.
* **update:**  
  Can the user update entities. This is only possible over HTTP.
* **delete:**  
  Can the user delete entities. This is only possible over HTTP.
* **admin:**  
  Can the user call the DatabaseStatus script. This is only possible over HTTP.

Currently there are two auth packages included: `FROST-Server.Auth.Basic` and `FROST-Server.Auth.Keycloak`.


## Auth settings

These are generic settings for authentication/authorisation.

* **auth.provider:**  
  The java class used to configure authentication/authorisation. Currently implemented are:
  * **`de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider`:**  
    An authentication provider that user basic authentication for HTTP, and stores user information in a database.
  * **`de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider`:**  
    An authentication provider that uses a keycloak server for authentication.
* **auth.allowAnonymousRead:**  
  If true, anonymous users are allowed to read (GET) data.
* **auth.authenticateOnly:** since 2.2.0  
  Let the auth module only handle authentication, not authorisation. If this is set the auth module attaches the user principal to each request, but no authoristion restrictions are applied. Use this when authorisation is handled by other components. default: false.
* **auth.autoUpdateDatabase:**  
  Automatically apply database updates.
* **auth.role.read:**  
  The role in the external authentication system that represents read (GET) access.
* **auth.role.create:**  
  The role in the external authentication system that represents create (POST) access.
* **auth.role.update:**  
  The role in the external authentication system that represents update (PUT/PATCH) access.
* **auth.role.delete:**  
  The role in the external authentication system that represents delete (DELETE) access.
* **auth.role.admin:**  
  The role in the external authentication system that represents admin (DatabaseStatus) access.
* **auth.maxUsernameLength:** Since 2.4.0  
  The maximum accepted length of usernames. Longer usernames are not checked and directly rejected. Default value: `128`
* **auth.maxPasswordLength:** Since 2.4.0  
  The maximum accepted length of passwords. Longer passwords are not checked and directly rejected. Default value: `128`


### Settings for the auth provider class `BasicAuthProvider`

The BasicAuthProvider needs a database to store its usernames, passwords and user roles.
This database can be the same as the database used by the persistence manager, but it can also be
a different database. The database has the same structure as described in the
[DataSourceRealm documentation of Tomcat](https://tomcat.apache.org/tomcat-8.5-doc/realm-howto.html#DataSourceRealm)
except with the table and column names in upper case, like the rest of the database.
If you let this provider create its database tables (with auth.autoUpdateDatabase) it will
create three default users:

* **read:**  
  password `read`, has only role `read`
* **write:**  
  password `write`, has roles `read`, `create` and `update`
* **admin:**  
  password `admin`, has roles `read`, `create`, `update`, `delete` and `admin`

You should change these default users! The creation only happens at the moment you
first try to authenticate, so do an authentication attempt to have tables created.

The BasicAuthProvider has the following specific settings:

* **auth.realmName:**  
  The name of the realm that the browser displays when asking for username and password.
* **auth.plainTextPassword:** since 2.1.0  
  If true (the default), passwords are stored in plain text.
  Otherwise password can use any result of the [PostgreSQL crypt function](https://www.postgresql.org/docs/current/pgcrypto.html#id-1.11.7.37.8.7),
  for example with Blowfish variant 2a (recommended):
  `INSERT INTO "USERS" VALUES ('my_user', crypt('my_password', gen_salt('bf', 12)))`.
* **auth.db.jndi.datasource:**  
  JNDI data source name, used when running in Tomcat/Wildfly. When using JNDI only set this option. When not using JNDI all other options can be used.
* **auth.db.driver:**  
  The Database driver to use. For PostgreSQL this should be: `org.postgresql.Driver`
* **auth.db.url:**  
  The database connection url. Example: `jdbc:postgresql://localhost:5432/sensorthings`
* **auth.db.username:**  
  The username to use when connecting to the database.
* **auth.db.password:**  
  The password to use when connecting to the database.
* **auth.db.conn.max:**  
  The maximum number of database connections to use.
* **auth.db.conn.idle.max:**  
  The maximum number of idle database connections to keep open.
* **auth.db.conn.idle.min:**  
  The minimum number of idle database connections to keep open.


### Settings for the auth provider class `KeycloakAuthProvider`

Keycloak requires a JSON config file. The contents of this file are provided by the Keycloak server.
There are three ways to pass this configuration to FROST:

* setting the content directly in the option `keycloakConfig`,
* saving the config in a file and passing the file path in the option `keycloakConfigFile`,
* or having FROST download it from the server, by setting the option `keycloakConfigUrl` and optionally `keycloakConfigSecret`.

When setting roles for a user, the roles need to be set explicitly for the FROST client.
Realm roles do not automatically apply to FROST.

* **auth.keycloakConfig:**  
  The json content of the keycloak json config file.
* **auth.keycloakConfigFile:**  
  The path to the keycloak json config file.
* **auth.keycloakConfigUrl:**  
  The URL on the Keycloak server that can be used to download the Keycloak config file. Usually this url is in the form
        of: `https://keycloak.example.com/auth/realms/[realm]/clients-registrations/install/[client id]`
* **auth.keycloakConfigSecret:**  
  If the client has "access-type" set to "confidential" then a secret is required to download the configuration.
        This secret can be found in the configuration itself, in Keycloak.
* **auth.registerUserLocally:** Since 2.3.0  
  If a user succesfully logs in through KeyCloak, the user account is also registered in a local database. Default value: `false`
* **auth.userRoleDecoderClass:** Since 2.4.0  
  If registerUserLocally is true, the java class used to decode Users and Roles coming from KeyCloak, and add them to a local database.
        Default value: `de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.UserRoleDecoderDflt`
* **auth.userCacheLifetime:** Since 2.4.0  
  If registerUserLocally is true, decoded users and roles are cached for this long. Must be an [ISO 8601 Duration](https://en.wikipedia.org/wiki/ISO_8601#Durations). Default value: `PT5M`
* **auth.userCacheCleanupInterval:** Since 2.4.0  
  If registerUserLocally is true, the user cache is checked this often, to remove timed-out users. Must be an [ISO 8601 Duration](https://en.wikipedia.org/wiki/ISO_8601#Durations). Default value: `PT5S`


### Settings for the UserRoleDecoder class `UserRoleDecoderDflt`

When **auth.registerUserLocally** is used in the KeyCloak Auth Provider, this is the default class used for this registration.
It checks if the username exist in the `userTable.usernameColumn` and if not, does an insert on this table/column.

The Projects Plugin comes with its own UserRoleDecoder class.

* **auth.userTable:** Since 2.3.0  
  The table to register users in. Default value: `USERS`
* **auth.usernameColumn:** Since 2.3.0  
  The column for the user name. Default value: `USER_NAME`
