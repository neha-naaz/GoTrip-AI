# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/tripflow-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --retries=10 --start-period=40s \
  CMD bash -c "exec 3<>/dev/tcp/127.0.0.1/8080"
ENTRYPOINT ["java", "-jar", "app.jar"]
