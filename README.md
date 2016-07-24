# REST server for Postfix

REST server for the Postfix mail server configured with a database backend. Provides a REST endpoints for administering different aspects of your mail server like virtual domains, users, aliases, sender and recever BCCs, etc.

Implemented in Java using Spring Boot.

## Requirements

You need Java 8 installed on the server. For building the project you need [Gradle](https://gradle.org), as described in the following section.

Out of the box it works with MySQL based databases, but can also be used with other databases for which a suitable JDBC driver exist.

The Postfix REST Server uses the database schema provided in my [postfix-database](https://github.com/lyubenblagoev/postfix-database) project.

Tested with Java 8 and MariaDB 10.1.14.

## Building the application

This project uses [Gradle](https://gradle.org) as dependency management and build system. The project can be build using the following command:

    $ gradle build

When the project is built you'll find the project jar file in the `build/libs` directory.

## Configuration

This is a Spring Boot project and to configure it for your system you need to place an `application.properties` file in the directory in which you install the application jar file.

## Running the application

The application can be run as any other jar file: 

    $ java -jar postfix-rest-server-0.0.1-SNAPSHOT.jar

Out of the box it will run with MySQL database but if you want to use it with different database like PostgreSQL you need to add the appropriate JDBC driver jar file to the classpath when running the application.

To run the application as a service you need to create a service file in your `/etc/systemd/system` directory. 

Assuming that you've installed the jar file in the /opt/postfix-rest-server directory and that you've created a simple wrapper script in the same directory called postfix-rest-server, your systemd service file will look similar to the example below:

```
[Unit]
Description=REST server for administering Postfix mail server 
After=syslog.target

[Service] 
User=postfix-rest 
ExecStart=/opt/postfix-rest-server/postfix-rest-server 
SuccessExitStatus=0

[Install] 
WantedBy=multi-user.target
```

## Usage

Here's a short overview about the possible REST API calls:

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
  * **GET** http://URL/api/v1/domains/{domain}/aliases/{alias} - get the alias with ID {id} for domain {domain}
  * **POST** http://URL/api/v1/domains/{domain}/aliases/ - create a new alias for domain {domain} (params: alias, email)
  * **PUT** http://URL/api/v1/domains/{domain}/aliases/{alias} - update alias with name {alias} for domain {domain} (params: alias, email)
  * **DELETE** http://URL/api/v1/domains/{domain}/aliases/{alias} - delete alias with alias {alias} for domain {domain}

**BCCs**

  * **GET** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incomming - get the automatic BCC address for all mail targeted to the specified account
  * **GET** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - get the automatic BCC address for all mail that is sent from the specified account
  * **POST** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incomming - set automatic BCC address for all mail targeted to the specified account
  * **POST** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - set automatic BCC address for all mail that is sent from the specified account
  * **PUT** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incomming - update the incomming auto bcc address for the spcified account
  * **PUT** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - update the outgoing auto bcc address for the spcified account
  * **DELETE** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incomming - delete the incomming BCC address for the specified account
  * **DELETE** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - delete the outgoing BCC address for the specified account

## Bug reports 

If you discover any bugs, feel free to create an issue on GitHub. Please add as much information as possible to help me fixing the possible bug. I encourage you to help even more by forking the project and sending a pull request.

## License

Apache License 2.0. See LICENSE for details.
