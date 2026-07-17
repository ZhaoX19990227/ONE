FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S one && adduser -S one -G one
WORKDIR /app
COPY --from=build /workspace/target/one-server-*.jar app.jar
USER one
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
