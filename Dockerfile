# Use a recent stable OpenJDK base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from your local target directory into the container
# You need to run 'mvn clean install' locally first to create this JAR.
COPY target/simple-github-gists-api-1.0.0-SNAPSHOT.jar app.jar

# Expose the port the application listens on
EXPOSE 8080

# Command to run the application when the container starts
# We pass the default port 8080 as an argument, though it's the default anyway.
ENTRYPOINT ["java", "-jar", "app.jar", "8080"]