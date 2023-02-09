
FROM eclipse-temurin:17.0.6_10-jdk-jammy
ARG BACKSTAGE_VERSION
ARG SYNCRO_VERSION

ENV BACKSTAGE_DB_HOST="backstage-db"
ENV BACKSTAGE_DB_PORT=3306
ENV BACKSTAGE_DB_NAME="backstagedb"
ENV BACKSTAGE_DB_USER="root"
ENV BACKSTAGE_DB_PASSWORD="root"
ENV BACKSTAGE_SERVER_URL="http://localhost:8080"
ENV LAUNCHCODE_HOST="launchcode"
ENV LAUNCHCODE_PORT=22
ENV PACKAGE_TOOLS_HOST="backstage-tools"
ENV PACKAGE_TOOLS_PORT=22

ENV BACKSTAGE_CONTENT_URL="/var/backstage/content"
ENV BACKSTAGE_JDK_ROOT="/var/backstage/jdk-root"
ENV BACKSTAGE_RELEASE_URL="/var/backstage/release"
ENV BACKSTAGE_SYNCRO_VERSION=${SYNCRO_VERSION}




RUN mkdir -p  /opt/backstage/config/liquibase && mkdir -p /var/backstage/{jdk-root,content,release}
RUN curl https://repo1.maven.org/maven2/co/bitshifted/appforge/syncro/${SYNCRO_VERSON}/syncro-${SYNCRO_VERSON}.jar -o /opt/backstage/syncro.jar
COPY target/backstage-${BACKSTAGE_VERSION}.jar /opt/backstage/backstage.jar
COPY docker/application.properties /opt/backstage/config
COPY liquibase/*.xml /opt/backstage/config/liquibase
COPY docker/run.sh /usr/local/bin/run-backstage
RUN chmod 755 /usr/local/bin/run-backstage

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "/usr/local/bin/run-backstage"]

