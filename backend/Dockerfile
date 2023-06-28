# Use a base image with Java and Maven installed
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project file
COPY ./pom.xml .

# Download the project dependencies
RUN mvn dependency:go-offline -B

# Copy the application source code
COPY ./src ./src

# Build the application
RUN mvn package

# Use a lightweight base image with Java installed
FROM openjdk:17-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the application will be listening on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
