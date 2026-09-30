# REST server for Postfix

REST API for the Postfix mail server. Uses virtual domains configuration with database backend (using the configuration files and script provided in the [postfix-database](https://github.com/lyubenblagoev/postfix-database) project. Provides a REST endpoints for administering different aspects of your mail server: virtual domains, users, aliases, automatic sender and receiver BCC.

Implemented in Java using Spring Boot.

## Requirements

You need Java 17+ installed on the server. For building the project you need [Maven](https://maven.apache.org), as described in the following section.

Out of the box it works with [PostgreSQL](https://www.postgresql.org) and [MySQL](https://www.mysql.com) based databases, but can also be used with other databases for which a suitable JDBC driver exist.

To configure your mail server for Postfix REST Server you need the configuration files and database views provided in my [postfix-database](https://github.com/lyubenblagoev/postfix-database) project.

Tested with Java 17+, PostgreSQL 10.2+ and MariaDB 10.1.14+.

## Building the application

This project uses [Maven](https://maven.apache.org) as dependency management and build system. The project can be build using the following command:

    mvn package

When the project is built you'll find the project jar file in the `target` directory.

## Configuration

This is a Spring Boot project and to configure it for your system you need to put an `application.properties` file in the directory in which you install the application jar file. The application.properties file must include `mail-server.vhosts-path` key to specify the path to the virtual hosts folder for your mail server (the default is `/var/mail/vhosts`). Take a look at the `src/main/resources/application.properties` file for other default values.

By default, the application is configured to connect to a PostgreSQL server database `servermail` running on the local machine with username `mailuser` and password `mailpassword`.

During development, you can set `spring.profiles.active` to `dev` to use an H2 database as configured in `src/main/resources/application-dev.properties`. Activating the `dev` profile also activates SQL query and debug level logging.

Database schema changes are managed by Liquibase migrations under `src/main/resources/db/changelog`. Migrations run automatically at application startup before Hibernate validates the schema. Hibernate uses `spring.jpa.hibernate.ddl-auto=validate` in both production and development; there is no longer an `updatedb` profile.

Existing installations without Liquibase migration history are upgraded from the schema itself. The initial migration creates application tables that are absent and records existing tables as already present. Later migrations add missing audit-log columns, indexes, and the backfill-state table. This lets installations from before audit logging upgrade directly to the current release without identifying their application commit. The database user configured for the application needs permission to create Liquibase's tracking tables and to create or alter application tables and indexes.

The following lines in `src/main/resources/application.properties` configure the default admin user for the server:

```properties
users.default-user.email=admin@example.com
users.default-user.password=admin
```

The default admin user will be created during application startup if no user exists yet. This is the user that will be used for authenticating the HTTP requests to the API. It is advisable to specify different email and password for the administrative user account during installation, especially when installing on production servers.

When a user successfully logs in using their email address (login) and password, a JSON Web Token will be returned along with a refresh token. When the client wants to access a protected route or resource, it should send the JWT in the `Authorization` header using the `Bearer` schema. The content of the header might look like the following:

    Authorization: Bearer eyJhbGci...<snip>...yu5CSpyHI

### Domain summaries

The aggregate domain endpoints return domain metadata together with account and alias counts without returning account, alias, recipient, or BCC records:

    GET /api/v1/domain-summaries
    GET /api/v1/domain-summaries/{domain}

Each response object has this shape:

```json
{
  "id": 123,
  "name": "example.com",
  "enabled": true,
  "created": "2026-01-01T12:00:00+0000",
  "updated": "2026-01-02T12:00:00+0000",
  "accounts": { "total": 3, "enabled": 2, "disabled": 1 },
  "aliases": { "total": 2, "healthy": 1, "needsReview": 1, "paused": 0 }
}
```

Account counts are calculated from account rows. Alias counts are calculated by distinct alias name: an alias is `healthy` when all recipients are enabled, `paused` when all are disabled, and `needsReview` when enabled and disabled recipients are mixed. Empty domains return zero counts. The collection is ordered by domain name. A missing domain returns HTTP 404. These endpoints require the same authentication as the other `/api/v1/**` endpoints.

### Audit log

Changes to domains, accounts, aliases, BCC records, and users are recorded in the audit log. Passwords and refresh tokens are never stored in the log; password changes are shown as `[REDACTED]`.

**Get the history for one resource**

    GET /api/v1/audit-logs?entityType=DOMAIN&entityId=123

The `entityType` values are `DOMAIN`, `ACCOUNT`, `ALIAS`, `INCOMING_BCC`, `OUTGOING_BCC`, and `USER`. The response is ordered newest first and contains the action, timestamp, acting user, resource identifier, and each changed field with its old and new value.

**Get recent changes**

    GET /api/v1/audit-logs?limit=50

The optional `limit` is between 1 and 100. Supplying only `entityType` or only `entityId` is also supported for broader queries.

**Get activity for a domain or account**

    GET /api/v1/audit-logs?scope=DOMAIN&scopeId=123&limit=50
    GET /api/v1/audit-logs?scope=ACCOUNT&scopeId=456&limit=50

The `DOMAIN` scope includes account, alias, incoming BCC, and outgoing BCC activity for the domain. The `ACCOUNT` scope includes account and BCC activity for the account. Each entry keeps its original entity type and entity ID.

New audit entries contain a persisted `resourceIdentifier`, so scoped retrieval does not need to load current account or alias resources. Identifiers use these formats: `DOMAIN` uses the domain name; `ACCOUNT` uses the complete mailbox address; `ALIAS` uses `alias@domain -> recipient`; and BCC entries use the configured BCC address. Created and deleted entries retain the corresponding historical identifier. For renamed resources, the updated entry contains the new identifier and its changes contain the previous value. The startup backfill populates identifiers for legacy entries when their audit snapshots contain enough historical data; otherwise `resourceIdentifier` remains null rather than being reconstructed from current resources.

Audit-log scope and resource-identifier backfills run automatically after Liquibase has applied the schema migrations. A versioned completion marker makes the backfill idempotent. Historical values are populated only when the audit snapshots contain enough information; otherwise the values remain null rather than being guessed from current resources.

### Workspace access and invitations

Domains can be allocated to multiple workspaces, and a user can be mapped to multiple workspaces. A user's domain access is the union of the domains allocated to their mapped workspaces. Domain, account, alias, domain-summary, and audit-log queries use this access automatically; clients do not select a workspace or pass a workspace filter when reading those resources. Requests for a domain outside the user's access return HTTP 401. Administrators have access to all domains.

Only administrators can create or delete workspaces, change a workspace's domain allocation, or create, rename, and delete domain records. A workspace administrator can view a workspace they belong to and manage its user mappings. Domain allocation is controlled by administrators because workspace mappings are what grant users access to the domains allocated to a workspace.

The `WORKSPACE_ADMIN` and `WORKSPACE_MEMBER` roles are stored on individual workspace mappings, rather than as global user roles. This keeps future workspace-scoped roles separate from administrator privileges. An omitted role when creating a mapping defaults to `WORKSPACE_MEMBER`.

Existing non-admin users and all current domains are mapped to a `Default` workspace during migration, preserving existing users' domain access. Administrators can then create new workspaces and change mappings and allocations. New domains are not automatically added to any workspace.

Global administrators can create time-limited invitations for `ADMIN`, `WORKSPACE_ADMIN`, or `WORKSPACE_MEMBER` roles. Workspace administrators can invite `WORKSPACE_MEMBER` users to workspaces they administer. The response contains a one-time token and its expiration time; the inviter places that token in the front-end invitation URL to send through their chosen channel. The default validity is 72 hours and can be changed with `users.invitation.validity-hours`. The token is stored as a SHA-256 hash. Invitees accept it by setting a password, then sign in normally. An `ADMIN` invitation must not include a workspace; a `WORKSPACE_ADMIN` or `WORKSPACE_MEMBER` invitation must include one.

Workspace and invitation endpoints are listed in the API Reference below. The standard API error shape is `{ "message": "...", "path": "...", "method": "..." }`.

The server will check for a valid authentication token in the Authorization header, and if it is present, the user will be allowed to access the protected resource.

For security purposes the JWTs are valid for 15 minutes. If an invalid token is sent to the server, it will respond with HTTP status code 401. Clients can respond to this event by silently obtaining a new access token (JWT) using the user's login (email address), and the refresh token received from the server after successful login. If the provided refresh token is valid the server responds by returning a new authentication and refresh tokens. By default, refresh tokens are valid for 30 days after they are issued. 

Tokens' configuration and validity can be configured with the following properties in `application.properties`:

```properties
# The JWT signing key (use a random value of at least 32 bytes)
users.jwt.secret-key=change-this-default-jwt-secret-key-before-production
# The JWT validity time in milliseconds (default: 900000 (15 minutes))
users.jwt.expire-after-millis=900000
# The refresh token validity in days (default: 30 days)
users.refresh-token.days-valid=30
```

## Running the application

The application can be run as any other jar file: 

    java -jar postfix-rest-server-0.2.2.jar

The jar file contains jdbc drivers for PostgreSQL and MySQL. To use it with different database management system you need to add the appropriate JDBC driver jar file to the classpath when running the application.

To run the application as a service you need to create a service file in your `/etc/systemd/system` directory. 

Assuming that you've installed the jar file in the /opt/postfix-rest-server directory and that you've created a simple wrapper script in the same directory called postfix-rest-server, your systemd service file will look similar to the example below:

```
[Unit]
Description=REST server for administering Postfix mail server 
After=syslog.target

[Service] 
User=postfix-rest 
ExecStart=/opt/postfix-rest-server/postfix-rest-server 
WorkingDirectory=/opt/postfix-rest-server
SuccessExitStatus=0

[Install] 
WantedBy=multi-user.target
```

Note that when you rename or delete account or domain, the corresponding directory is also renamed or deleted. Ensure that the user that is used for running the server has write privileges on your mail vhosts directory and its subdirectories.

## API Reference

Here's a short overview of the possible REST API calls:

### Authentication

**Log in using the administrative user's credentials**

    POST /api/v1/auth/signin

| Parameter    | Type   | Description                                    |
|--------------|--------|------------------------------------------------|
| login        | string | **Required** Your user's login / email address |
| password     | string | **Required** Your user's password              |

**Log Out**

    POST /api/v1/auth/signout

| Parameter    | Type   | Description                                    |
|--------------|--------|------------------------------------------------|
| login        | string | **Required** Your user's login / email address |
| refreshToken | string | **Required** Your refresh token                |

**Generate a new access token**

    POST /api/v1/auth/refresh-token

| Parameter    | Type   | Description                                    |
|--------------|--------|------------------------------------------------|
| login        | string | **Required** Your user's login / email address |
| refreshToken | string | **Required** Your refresh token                |

### Workspaces and invitations

Workspace endpoints require an authenticated request, except for accepting an invitation. Workspace creation, deletion, and domain allocation changes require the global `ADMIN` role. Workspace administrators can read and manage mappings for workspaces they administer. They can also invite `WORKSPACE_MEMBER` users to those workspaces. Only global administrators can invite `ADMIN` or `WORKSPACE_ADMIN` users.

**List workspaces (administrator only)**

    GET /api/v1/workspaces

Returns an array of workspace resources:

```json
[
  { "id": 12, "name": "Operations", "domainNames": ["example.com", "example.org"] }
]
```

**List workspaces managed by the current user**

    GET /api/v1/workspaces/managed

Returns the workspaces where the authenticated user has an enabled `WORKSPACE_ADMIN` mapping, using the same resource shape. Users with no such mappings receive an empty array. Global administrators receive all workspaces.

**List workspaces accessible to the current user**

    GET /api/v1/workspaces/accessible

Returns workspaces where the authenticated user has an enabled mapping of any workspace role.
Global administrators receive all workspaces.

**Create a workspace (administrator only)**

    POST /api/v1/workspaces

| Parameter   | Type             | Description                                        |
|-------------|------------------|----------------------------------------------------|
| name        | string           | **Required** Workspace name (request body)        |
| domainNames | array of strings | Existing domain names (request body)               |

`domainNames` may be omitted or empty. Returns HTTP 201 and the created workspace resource.

**Read a workspace**

    GET /api/v1/workspaces/${workspaceId}

| Parameter   | Type    | Description                                      |
|-------------|---------|--------------------------------------------------|
| workspaceId | integer | **Required** The ID of the workspace (path)      |

Administrators can read any workspace; other users need a mapping to that workspace. Returns the same workspace resource shape.

**Rename a workspace or replace its allocation (administrator only)**

    PUT /api/v1/workspaces/${workspaceId}

| Parameter   | Type             | Description                                                   |
|-------------|------------------|---------------------------------------------------------------|
| workspaceId | integer          | **Required** The ID of the workspace (path)                   |
| name        | string           | **Required** The new workspace name (request body)            |
| domainNames | array of strings | Domain names to allocate (request body)                        |

`name` is required. If `domainNames` is omitted, the current allocation is kept; if supplied, it replaces the allocation. Returns the updated workspace resource.

**Replace a workspace's domain allocation (administrator only)**

    PUT /api/v1/workspaces/${workspaceId}/domains

| Parameter   | Type             | Description                                                      |
|-------------|------------------|------------------------------------------------------------------|
| workspaceId | integer          | **Required** The ID of the workspace (path)                      |
| domainNames | array of strings | Request body. Optional; omission or an empty list removes all allocations |

Returns the updated workspace resource.

**Append an existing domain (administrator only)**

    POST /api/v1/workspaces/${workspaceId}/domains

| Parameter   | Type    | Description                                        |
|-------------|---------|----------------------------------------------------|
| workspaceId | integer | **Required** The ID of the workspace (path)        |
| domainName  | string  | **Required** Existing domain name (request body)   |

Returns the updated workspace resource. A domain can be allocated to more than one workspace.

**Remove a domain allocation (administrator only)**

    DELETE /api/v1/workspaces/${workspaceId}/domains/${domainName}

| Parameter   | Type    | Description                                      |
|-------------|---------|--------------------------------------------------|
| workspaceId | integer | **Required** The ID of the workspace (path)      |
| domainName  | string  | **Required** The domain name (path)              |

Removing a domain allocation returns the updated workspace resource.

**Delete a workspace (administrator only)**

    DELETE /api/v1/workspaces/${workspaceId}

| Parameter   | Type    | Description                                      |
|-------------|---------|--------------------------------------------------|
| workspaceId | integer | **Required** The ID of the workspace (path)      |

Returns HTTP 204.

**List workspace mappings (workspace administrator or administrator)**

    GET /api/v1/workspaces/${workspaceId}/mappings

| Parameter   | Type    | Description                                      |
|-------------|---------|--------------------------------------------------|
| workspaceId | integer | **Required** The ID of the workspace (path)      |

Returns an array of mappings:

```json
[
  { "id": 45, "email": "user@example.com", "role": "WORKSPACE_ADMIN" }
]
```

**Add or change a user's workspace mapping (workspace administrator or administrator)**

    POST /api/v1/workspaces/${workspaceId}/mappings

| Parameter   | Type    | Description                                                            |
|-------------|---------|------------------------------------------------------------------------|
| workspaceId | integer | **Required** The ID of the workspace (path)                            |
| email       | string  | **Required** Email address of an existing user (request body)          |
| role        | string  | Optional request body field. Defaults to `WORKSPACE_MEMBER`            |

Allowed roles are `WORKSPACE_MEMBER` and `WORKSPACE_ADMIN`. `ADMIN` is a global role and cannot be set on a workspace mapping. Returns the saved mapping resource.

A workspace administrator cannot downgrade their own mapping. Another workspace administrator or a global administrator must make that change; self-demotion returns HTTP 403.

**Remove a user's workspace mapping (workspace administrator or administrator)**

    DELETE /api/v1/workspaces/${workspaceId}/mappings/${email}

| Parameter   | Type    | Description                                      |
|-------------|---------|--------------------------------------------------|
| workspaceId | integer | **Required** The ID of the workspace (path)      |
| email       | string  | **Required** Email address of the user (path)    |

Returns HTTP 204. URL-encode the email path segment.

A workspace administrator cannot remove their own mapping; another workspace administrator or a global administrator must do so.

**Create an invitation (administrator, or workspace administrator inviting a member to their workspace)**

    POST /api/v1/invitations

| Parameter   | Type    | Description                                                                                         |
|-------------|---------|-----------------------------------------------------------------------------------------------------|
| email       | string  | **Required** Invitee's email address (request body)                                                 |
| role        | string  | **Required** `ADMIN`, `WORKSPACE_ADMIN`, or `WORKSPACE_MEMBER` (request body)                       |
| workspaceId | integer | Required for `WORKSPACE_ADMIN` and `WORKSPACE_MEMBER`; omit for `ADMIN` (request body)              |

Only a global administrator can invite an `ADMIN` or `WORKSPACE_ADMIN`. A global administrator can invite a `WORKSPACE_MEMBER` to any workspace; a workspace administrator can invite one only to a workspace they administer. Accepting the invitation creates the user account if needed and creates or updates their mapping to the selected workspace.

A workspace administrator cannot invite their own email as a member; another workspace administrator or a global administrator must perform that change.

Returns HTTP 201 with the token and its metadata:

```json
{
  "email": "member@example.com",
  "role": "WORKSPACE_MEMBER",
  "workspaceId": 12,
  "token": "one-time-token",
  "expiresAt": "<JSON date-time>"
}
```

The inviter or front end includes `token` in the URL sent to the invitee. The invitee submits the token and chosen password to the public acceptance endpoint:

    POST /api/v1/auth/invitations/accept

| Parameter | Type   | Description                                  |
|-----------|--------|----------------------------------------------|
| token     | string | **Required** Invitation token (request body) |
| password  | string | **Required** Password to set (request body)  |

Successful acceptance returns HTTP 204. The invitee then signs in using `/api/v1/auth/signin`. Invalid, expired, or already accepted invitations return HTTP 400.

### Users

**Update administrative user's account information**

    PUT /api/v1/users/${login}

| Parameter            | Type   | Description                                            |
|----------------------|--------|--------------------------------------------------------|
| login                | string | **Required** Your user's login / email address         |
| email                | string | A new email address to use as login                    |
| password             | string | A password to change the user's password               |
| passwordConfirmation | string | Password confirmation (must be the same as *password*) |

### Domains

**List all configured domains**

    GET /api/v1/domains

**Get information for a specific domain**

    GET /api/v1/domains/${domain}

| Parameter | Type   | Description                         |
|-----------|--------|-------------------------------------|
| domain    | string | **Required** The name of the domain |

**Create a new domain**

    POST /api/v1/domains

| Parameter | Type    | Description                                             |
|-----------|---------|---------------------------------------------------------|
| name      | string  | A new name for the domain specified by *domain*         |
| enabled   | boolean | A boolean value that specifies if the domain is enabled |

**Update domain**

    PUT /api/v1/domains/${domain}

| Parameter | Type    | Description                                             |
|-----------|---------|---------------------------------------------------------|
| domain    | string  | **Required** The name of the domain                     |
| name      | string  | A new name for the domain specified by *domain*         |
| enabled   | boolean | A boolean value that specifies if the domain is enabled |

**Delete a domain**

    DELETE /api/v1/domains/${domain}

| Parameter | Type   | Description                         |
|-----------|--------|-------------------------------------|
| name      | string | **Required** The name of the domain |

### Accounts

**Get all accounts for the specified domain**

    GET  /api/v1/domains/${domain}/accounts

| Parameter | Type   | Description                         |
|-----------|--------|-------------------------------------|
| name      | string | **Required** The name of the domain |

**Get information for an email account with given username and domain name**

    GET /api/v1/domains/${domain}/accounts/${username}

| Parameter | Type    | Description                                             |
|-----------|---------|---------------------------------------------------------|
| domain    | string  | **Required** The name of the domain                     |
| username  | string  | **Required** The username prefix for an email account   |

**Create a new email account**

    POST /api/v1/domains/${domain}/accounts/

| Parameter       | Type    | Description                                              |
|-----------------|---------|----------------------------------------------------------|
| domain          | string  | **Required** The name of the domain                      |
| username        | string  | **Required** The username prefix for an email account    |
| password        | string  | A password for the email account                         |
| confirmPassword | string  | A confirmation for the password specified in *password*  |
| enabled         | boolean | A boolean value that specifies if the account is enabled |

**Change an email account**

    PUT /api/v1/domains/${domain}/accounts/${username}

| Parameter       | Type    | Description                                              |
|-----------------|---------|----------------------------------------------------------|
| domain          | string  | **Required** The name of the domain                      |
| username        | string  | **Required** The username prefix for an email account    |
| username        | string  | The new username prefix for the email account            |
| password        | string  | A password for the email account                         |
| confirmPassword | string  | A confirmation for the password specified in *password*  |
| enabled         | boolean | A boolean value that specifies if the account is enabled |

**Delete an email account**

    DELETE /api/v1/domains/${domain}/accounts/${username}

| Parameter       | Type    | Description                                              |
|-----------------|---------|----------------------------------------------------------|
| domain          | string  | **Required** The name of the domain                      |
| username        | string  | **Required** The username prefix for an email account    |

### Aliases

**Get all aliases for specific domain**

    GET /api/v1/domains/${domain}/aliases

| Parameter | Type   | Description                         |
|-----------|--------|-------------------------------------|
| domain    | string | **Required** The name of the domain |

**Get all aliases for domain and email prefix**

    GET /api/v1/domains/${domain}/aliases/${name}

| Parameter | Type   | Description                                       |
|-----------|--------|---------------------------------------------------|
| domain    | string | **Required** The name of the domain               |
| name      | string | **Required** The email prefix for the given alias |

**Get information for specific alias in the given domain that is forwarding to the given recipient**

    GET /api/v1/domains/${domain}/aliases/${name}/${email}

| Parameter | Type   | Description                                       |
|-----------|--------|---------------------------------------------------|
| domain    | string | **Required** The name of the domain               |
| name      | string | **Required** The email prefix for the given alias |
| email     | string | **Required** A recipient assigned to this alias   |

**Create a new alias**

    POST /api/v1/domains/${domain}/aliases/

| Parameter | Type   | Description                                             |
|-----------|--------|---------------------------------------------------------|
| domain    | string | **Required** The name of the domain                     |
| name      | string | **Required** The email prefix for the given alias       |
| email     | string | **Required** A recipient assigned to this alias         |
| enabled   | boolean | A boolean value that specifies if the alias is enabled |

**Update an alias**

    PUT /api/v1/domains/${domain}/aliases/${name}/${email}

| Parameter | Type   | Description                                             |
|-----------|--------|---------------------------------------------------------|
| domain    | string | **Required** The name of the domain                     |
| name      | string | **Required** The email prefix for the given alias       |
| name      | string | The email prefix for the given alias                    |
| email     | string | A recipient assigned to this alias                      |
| enabled   | boolean | A boolean value that specifies if the alias is enabled |

**Delete all aliases with specific email prefix and domain**

    DELETE /api/v1/domains/${domain}/aliases/${name}

| Parameter | Type   | Description                                       |
|-----------|--------|---------------------------------------------------|
| domain    | string | **Required** The name of the domain               |
| name      | string | **Required** The email prefix for the given alias |

**Delete all aliases with specific email prefix, domain and recipient**

    DELETE /api/v1/domains/${domain}/aliases/${name}/${email}

| Parameter | Type   | Description                                       |
|-----------|--------|---------------------------------------------------|
| domain    | string | **Required** The name of the domain               |
| name      | string | **Required** The email prefix for the given alias |
| email     | string | **Required** A recipient assigned to this alias   |

### BCCs

**Get the automatic BCC address for all mail targeted to the specified account**

    GET /api/v1/domains/${domain}/accounts/${username}/bccs/incoming

| Parameter | Type   | Description                                       |
|-----------|--------|---------------------------------------------------|
| domain    | string | **Required** The name of the domain               |
| username  | string | **Required** The email address prefix             |

**Get the automatic BCC address for all mail that is sent from the specified account**

    GET /api/v1/domains/${domain}/accounts/${username}/bccs/outgoing

| Parameter | Type   | Description                                       |
|-----------|--------|---------------------------------------------------|
| domain    | string | **Required** The name of the domain               |
| username  | string | **Required** The email address prefix             |

**Set automatic BCC address for all mail targeted to the specified account**

    POST /api/v1/domains/${domain}/accounts/${username}/bccs/incoming

| Parameter | Type   | Description                                           |
|-----------|--------|-------------------------------------------------------|
| domain    | string | **Required** The name of the domain                   |
| username  | string | **Required** The email address prefix                 |
| enabled   | boolean | A boolean value that specifies if the BCC is enabled |

**Set automatic BCC address for all mail that is sent from the specified account**

    POST /api/v1/domains/${domain}/accounts/${username}/bccs/outgoing

| Parameter | Type   | Description                                           |
|-----------|--------|-------------------------------------------------------|
| domain    | string | **Required** The name of the domain                   |
| username  | string | **Required** The email address prefix                 |
| enabled   | boolean | A boolean value that specifies if the BCC is enabled |

**Update the incoming auto bcc address for the specified account**

    PUT /api/v1/domains/${domain}/accounts/${username}/bccs/incoming

| Parameter | Type   | Description                                           |
|-----------|--------|-------------------------------------------------------|
| domain    | string | **Required** The name of the domain                   |
| username  | string | **Required** The email address prefix                 |
| enabled   | boolean | A boolean value that specifies if the BCC is enabled |

**Update the outgoing auto bcc address for the specified account**

    PUT /api/v1/domains/${domain}/accounts/${username}/bccs/outgoing

| Parameter | Type   | Description                                           |
|-----------|--------|-------------------------------------------------------|
| domain    | string | **Required** The name of the domain                   |
| username  | string | **Required** The email address prefix                 |
| enabled   | boolean | A boolean value that specifies if the BCC is enabled |

**Delete the incoming BCC address for the specified account**

    DELETE /api/v1/domains/${domain}/accounts/${username}/bccs/incoming

| Parameter | Type   | Description                                       |
|-----------|--------|---------------------------------------------------|
| domain    | string | **Required** The name of the domain               |
| username  | string | **Required** The email address prefix             |

**Delete the outgoing BCC address for the specified account**

    DELETE /api/v1/domains/${domain}/accounts/${username}/bccs/outgoing

| Parameter | Type   | Description                                       |
|-----------|--------|---------------------------------------------------|
| domain    | string | **Required** The name of the domain               |
| username  | string | **Required** The email address prefix             |

## Clients

[emailctl](https://github.com/lyubenblagoev/emailctl) is a CLI (command line interface) for the Postfix REST Server. It supports all APIs and allows server management through the command line.

[Postfix REST Server Control Panel](https://github.com/lyubenblagoev/postfix-rest-client) is a web interface for managing the Postfix REST Server. Currently it still doesn't support all functions of the server (automatic BCC is not supported yet in the web UI).

## Bug reports 

If you discover any bugs, feel free to create an issue on GitHub. Please add as much information as possible to help me fix the possible bug. I encourage you to help even more by forking the project and sending a pull request.

## License

Apache License 2.0. See LICENSE for details.
