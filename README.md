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

    java -jar postfix-rest-server-0.2.0.jar

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

## Usage

Here's a short overview of the possible REST API calls:

**Authentication**

  * **POST** http://URL/api/v1/auth/signin - log in using the administrative user's credentials (params: login, password)
  * **POST** http://URL/api/v1/auth/signout - log out (params: login, refreshToken)
  * **POST** http://URL/api/v1/auth/refresh-token - refresh user's authentication token (params: login, refreshToken)

**Users**

  * **POST** http://URL/api/v1/users/{login} - update administrative user's account information (params: email, password, passwordConfirmation)

**Domains**

  * **GET** http://URL/api/v1/domains - get all domains
  * **GET** http://URL/api/v1/domains/{domain} - get domain with the specified name
  * **POST** http://URL/api/v1/domains - create domain (params: name)
  * **PUT** http://URL/api/v1/domains/{domain} - update domain (params: name)
  * **DELETE** http://URL/api/v1/domains/{domain} - delete domain with name {domain}

**Accounts**

  * **GET** http://URL/api/v1/domains/{domain}/accounts - get all accounts for the specified domain
  * **GET** http://URL/api/v1/domains/{domain}/accounts/{username} - get the user with username {username} for domain {domain}
  * **POST** http://URL/api/v1/domains/{domain}/accounts/ - create a new account for domain {domain} (params: username, password, confirmPassword)
  * **PUT** http://URL/api/v1/domains/{domain}/accounts/{username} - update account with username {username} for domain {domain} (params: username, password, confirmPassword)
  * **DELETE** http://URL/api/v1/domains/{domain}/accounts/{username} - delete account with username {username} for domain {domain}

**Aliases**

  * **GET** http://URL/api/v1/domains/{domain}/aliases - get all aliases for the specified domain
  * **GET** http://URL/api/v1/domains/{domain}/aliases/{name} - get all aliases for domain {domain} and name {name}
  * **GET** http://URL/api/v1/domains/{domain}/aliases/{name}/{email} - get the alias {alias} in domain {domain} and recipient {email} 
  * **POST** http://URL/api/v1/domains/{domain}/aliases/ - create a new alias for domain {domain} (params: alias, email)
  * **PUT** http://URL/api/v1/domains/{domain}/aliases/{name}/{email} - update alias with name {alias} in domain {domain} and recipient {email} (params: name, email)
  * **DELETE** http://URL/api/v1/domains/{domain}/aliases/{name} - delete all aliases with name {name} in domain {domain}
  * **DELETE** http://URL/api/v1/domains/{domain}/aliases/{name}/{email} - delete alias with alias {alias} in domain {domain} and recipient {email}

**BCCs**

  * **GET** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incoming - get the automatic BCC address for all mail targeted to the specified account
  * **GET** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - get the automatic BCC address for all mail that is sent from the specified account
  * **POST** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incoming - set automatic BCC address for all mail targeted to the specified account
  * **POST** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - set automatic BCC address for all mail that is sent from the specified account
  * **PUT** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incoming - update the incoming auto bcc address for the specified account
  * **PUT** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - update the outgoing auto bcc address for the specified account
  * **DELETE** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incoming - delete the incoming BCC address for the specified account
  * **DELETE** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - delete the outgoing BCC address for the specified account

[emailctl](https://github.com/lyubenblagoev/emailctl) is a CLI (command line interface) for the Postfix REST Server. It supports all APIs and allows server management through the command line.

## Bug reports 

If you discover any bugs, feel free to create an issue on GitHub. Please add as much information as possible to help me fix the possible bug. I encourage you to help even more by forking the project and sending a pull request.

## License

Apache License 2.0. See LICENSE for details.
