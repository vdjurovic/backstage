
FROM eclipse-temurin:17.0.6_10-jdk-jammy

#ENV BACKSTAGE_DB_HOST=
#ENV BACKSTAGE_DB_PORT=
#ENV BACKSTAGE_DB_NAME=
#ENV BACKSTAGE_DB_USER=
#ENV BACKSTAGE_DB_PASSWORD=
#ENV LAUNCHCODE_HOST=
#ENV LAUNCHCODE_PORT=
#ENV PACKAGE_TOOLS_HOST=
#ENV PACKAGE_TOOLS_PORT=

ARG BACKSTAGE_VERSION


RUN mkdir -p  /opt/backstage/config/liquibase
COPY target/backstage-${BACKSTAGE_VERSION}.jar /opt/backstage/backstage.jar
COPY docker/application.properties /opt/backstage/config
COPY liquibase/*.xml /opt/backstage/config/liquibase
COPY docker/run.sh /usr/local/bin/run-backstage
RUN chmod 755 /usr/local/bin/run-backstage

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "/usr/local/bin/run-backstage"]

