# REST server for Postfix

REST API for the Postfix mail server. Uses virtual domains configuration with database backend (using the configuration files and script provided in the [postfix-database](https://github.com/lyubenblagoev/postfix-database) project. Provides a REST endpoints for administering different aspects of your mail server: virtual domains, users, aliases, automatic sender and receiver BCC.

Implemented in Java using Spring Boot.

## Requirements

You need Java 11+ installed on the server. For building the project you need [Maven](https://maven.apache.org), as described in the following section.

Out of the box it works with [PostgreSQL](https://www.postgresql.org) and [MySQL](https://www.mysql.com) based databases, but can also be used with other databases for which a suitable JDBC driver exist.

To configure your mail server for Postfix REST Server you need the configuration files and database views provided in my [postfix-database](https://github.com/lyubenblagoev/postfix-database) project.

Tested with Java 11+, PostgreSQL 10.2+ and MariaDB 10.1.14+.

## Building the application

This project uses [Maven](https://maven.apache.org) as dependency management and build system. The project can be build using the following command:

    mvn package

When the project is built you'll find the project jar file in the `target` directory.

## Configuration

This is a Spring Boot project and to configure it for your system you need to put an `application.properties` file in the directory in which you install the application jar file. The application.properties file must include `mail-server.vhosts-path` key to specify the path to the virtual hosts folder for your mail server (the default is `/var/mail/vhosts`). Take a look at the `src/main/resources/application.properties` file for other default values.

By default, the application is configured to connect to a PostgreSQL server database `servermail` running on the local machine with username `mailuser` and password `mailpassword`.

During development, you can set `spring.profiles.active` to `dev` to use an H2 database as configured in `src/main/resources/application-dev.properties`. Activating the `dev` profile also activates SQL query and debug level logging.

Running the app with the `updatedb` profile sets `spring.jpa.hibernate.ddl-auto` to `update`, which will update the database schema by comparing the existing database schema with the entity mappings. This is useful when in need to update to a new version of the Postfix REST Server that requires changes to the database. 

The following lines in `src/main/resources/application.properties` configure the default admin user for the server:

```properties
users.default-user.email=admin@example.com
users.default-user.password=admin
```

The default admin user will be created during application startup if no user exists yet. This is the user that will be used for authenticating the HTTP requests to the API. It is advisable to specify different email and password for the administrative user account during installation, especially when installing on production servers.

When a user successfully logs in using their email address (login) and password, a JSON Web Token will be returned along with a refresh token. When the client wants to access a protected route or resource, it should send the JWT in the `Authorization` header using the `Bearer` schema. The content of the header might look like the following:

    Authorization: Bearer eyJhbGci...<snip>...yu5CSpyHI

The server will check for a valid authentication token in the Authorization header, and if it is present, the user will be allowed to access the protected resource.

For security purposes the JWTs are valid for 15 minutes. If an invalid token is sent to the server, it will respond with HTTP status code 401. Clients can respond to this event by silently obtaining a new access token (JWT) using the user's login (email address), and the refresh token received from the server after successful login. If the provided refresh token is valid the server responds by returning a new authentication and refresh tokens. By default, refresh tokens are valid for 30 days after they are issued. 

Tokens' configuration and validity can be configured with the following properties in `application.properties`:

```properties
# The JWT signing key (default: secret)
users.jwt.secret-key=secret
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
