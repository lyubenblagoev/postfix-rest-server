# REST server for Postfix

REST API for the Postfix mail server. Uses virtual domains configuration with database beckend (using the database schema provided in the [postfix-database](https://github.com/lyubenblagoev/postfix-database) project. Provides a REST endpoints for administering different aspects of your mail server: virtual domains, users, aliases, automatic sender and recever BCC.

Implemented in Java using Spring Boot.

## Requirements

You need Java 11 installed on the server. For building the project you need [Maven](https://maven.apache.org), as described in the following section.

Out of the box it works with [PostgreSQL](https://www.postgresql.org) and [MySQL](https://www.mysql.com) based databases, but can also be used with other databases for which a suitable JDBC driver exist.

The Postfix REST Server uses the database schema provided in my [postfix-database](https://github.com/lyubenblagoev/postfix-database) project.

Tested with Java 11, PostgreSQL 10.2 and MariaDB 10.1.14.

## Building the application

This project uses [Maven](https://maven.apache.org) as dependency management and build system. The project can be build using the following command:

    $ mvn package

When the project is built you'll find the project jar file in the `build/libs` directory.

## Configuration

This is a Spring Boot project and to configure it for your system you need to place an `application.properties` file in the directory in which you install the application jar file. The application.properties file must include `mail-server.vhosts-path` key to specify the path to the virtual hosts folder for your mail server (the default is /var/mail/vhosts). Take a look at the src/main/resources/application.properties file for other default values.

## Running the application

The application can be run as any other jar file: 

    $ java -jar postfix-rest-server-0.1.0-SNAPSHOT.jar

The jar file contains jdbc drivers for PostgreSQL and MySQL. To use it with different database management system you need to add the appropriate JDBC driver jar file to the classpath when running the application.

As already mentioned you need to configure the appropriate driver name, connection string, username and password in the `application.properties` file.

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
  * **GET** http://URL/api/v1/domains/{domain}/aliases/{name} - get all aliases for domain {domain} and name {name}
  * **GET** http://URL/api/v1/domains/{domain}/aliases/{name}/{email} - get the alias {alias} in domain {domain} and recipient {email} 
  * **POST** http://URL/api/v1/domains/{domain}/aliases/ - create a new alias for domain {domain} (params: alias, email)
  * **PUT** http://URL/api/v1/domains/{domain}/aliases/{name}/{email} - update alias with name {alias} in domain {domain} and recipient {email} (params: name, email)
  * **DELETE** http://URL/api/v1/domains/{domain}/aliases/{name} - delete all aliases with name {name} in domain {domain}
  * **DELETE** http://URL/api/v1/domains/{domain}/aliases/{name}/{email} - delete alias with alias {alias} in domain {domain} and recipient {email}

**BCCs**

  * **GET** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incomming - get the automatic BCC address for all mail targeted to the specified account
  * **GET** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - get the automatic BCC address for all mail that is sent from the specified account
  * **POST** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incomming - set automatic BCC address for all mail targeted to the specified account
  * **POST** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - set automatic BCC address for all mail that is sent from the specified account
  * **PUT** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incomming - update the incomming auto bcc address for the specified account
  * **PUT** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - update the outgoing auto bcc address for the specified account
  * **DELETE** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/incomming - delete the incomming BCC address for the specified account
  * **DELETE** http://URL/api/v1/domains/{domain}/accounts/{username}/bccs/outgoing - delete the outgoing BCC address for the specified account

[emailctl](https://github.com/lyubenblagoev/emailctl) is a CLI (command line interface) for the Postfix REST Server. It supports all APIs and allows server management through the command line.

## Bug reports 

If you discover any bugs, feel free to create an issue on GitHub. Please add as much information as possible to help me fixing the possible bug. I encourage you to help even more by forking the project and sending a pull request.

## License

Apache License 2.0. See LICENSE for details.
