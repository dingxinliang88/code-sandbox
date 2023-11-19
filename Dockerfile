#FROM maven:3.8.3-jdk-8-slim as builder

## Copy local code to the container image.
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src

## Build a release artifact.
#RUN mvn package -DskipTests

## Run the web service on container startup.
#CMD ["java","-jar","/app/target/code-sandbox-1.0.jar","--spring.profiles.active=prod"]

FROM openjdk:8-jre-alpine

# Copy the jar file to the container image.
WORKDIR /app
COPY code-sandbox-1.0.jar ./code-sandbox.jar

# Run the web service on container startup.
CMD ["java", "-jar", "/app/code-sandbox.jar", "--spring.profiles.active=prod"]