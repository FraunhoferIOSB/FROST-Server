---
layout: default
title: Authentication
category: settings
order: 21
---

# Authentication and Authorisation

There are many ways to secure webservices. The most flexible one is to use an external reverse proxy.
Since this can be tricky to set up, FROST offers some simple auth* options.

Auth* has to happen in two places: in the HTTP package and in the MQTT package. To make things
flexible, authentication is handled by classes that implement an interface, just like the
persistence backends. The interface to be implemented is de.fraunhofer.iosb.ilt.sta.util.AuthProvider.


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


### Settings for the auth provider class `BasicAuthProvider`

The BasicAuthProvider needs a database to store its usernames, passwords and user roles.
This database can be the same as the database used by the persistence manager, but it can also be
a different database. The database has the same structure as described in the
https://tomcat.apache.org/tomcat-8.5-doc/realm-howto.html#DataSourceRealm[DataSourceRealm documentation of Tomcat]
except with the table and column names in upper case, like the rest of the database.
If you let this provider create its database tables (with auth.autoUpdateDatabase) it will
create three default users:

* **read:**  
  password `read`, has only role `read`
* **write:**  
  password `write`, has roles `read`, `create` and `update`
* **admin:**  
  password `admin`, has roles `read`, `write`, `update`, `delete` and `admin`

You should change these default users!

The BasicAuthProvider has the following specific settings:

* **auth.realmName:**  
  The name of the realm that the browser displays when asking for username and password.
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
There are three ways to pass this configuration to FROST: setting the content directly in the option `keycloakConfig`,
saving the config in a file and passing the file path in the option `keycloakConfigFile`, or having FROST download it
from the server, by setting the option `keycloakConfigUrl` and optionally `keycloakConfigSecret`.

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

