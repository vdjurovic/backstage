# Backstage

*Ignite is part of [AppForge](https://github.com/bitshifted/appforge) platform.*

Backstage is the engine for building and deploying application packages and updates. It's a server side application that provides the
following functionality:

* build application installers for all supported operating systems (Windows, Mac OS X, Linux)
* Allows users to search for applications and download installers
* Serves content for application auto-updates

# License

Backstage is released under Mozilla Public License 2.0. See [LICENSE](./LICENSE) file for details.

# Building and running

Backstage is a Spring Boot application written in Kotlin. Requirements for building and running the application are:

* Java 17 or gigher
* Maven 3.x.x

To run the application, execute the following command:

```shell
mvn spring-boot.run.profiles=local spring-boot:run
```

Before running, check the file [application-local.properties](./src/main/resources/application-local.properties) and adjust any paths 
to match your local system.
