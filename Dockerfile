# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:17-jdk-slim
# Create a dedicated non-root user and group
RUN groupadd --system appuser && useradd --system --gid appuser appuser
WORKDIR /app
# Copy the built JAR from the 'build' stage
COPY --from=build /app/target/*.jar app.jar
# Set permissions for the appuser to execute the jar
RUN chown appuser:appuser app.jar
# Switch to the non-root user
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
