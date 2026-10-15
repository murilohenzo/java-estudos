# Build application using Java 17 and Maven.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package \
 && mvn -B dependency:copy \
    -Dartifact=org.postgresql:postgresql:42.7.4 \
    -DoutputDirectory=/workspace/drivers

# Official Eclipse Foundation image, compatible with Jakarta EE 10.
FROM ghcr.io/eclipse-ee4j/glassfish:7.1.0
COPY --from=build /workspace/drivers/postgresql-42.7.4.jar \
    /opt/glassfish7/glassfish/domains/domain1/lib/postgresql.jar
COPY docker/glassfish/init.sh /opt/glassfish7/custom/init.sh
COPY --from=build /workspace/target/javaee-laboratorio.war \
    /deploy/javaee-laboratorio.war
EXPOSE 8080
