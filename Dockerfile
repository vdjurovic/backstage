
FROM eclipse-temurin:17.0.6_10-jdk-jammy

RUN mkdir /opt/backstage
COPY target/backstage-*.jar /opt/backstage/backstage.jar
COPY docker/run.sh /usr/local/bin/run-backstage
RUN chmod 755 /usr/local/bin/run-backstage

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "/usr/local/bin/run-backstage"]

