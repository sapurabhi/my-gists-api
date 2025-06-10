# Simple GitHub Gists API

This project implements a very simple HTTP web server API in Java that interacts directly with the GitHub API to retrieve a list of a user's publicly available Gists. It's designed to be minimal, explicitly showing HTTP server setup and API interaction without complex frameworks.

## Table of Contents

* [Features](#features)
* [Technologies Used](#technologies-used)
* [Prerequisites](#prerequisites)
* [Getting Started (Setup & Run)](#getting-started-setup--run)
    * [1. Clone the Repository](#1-clone-the-repository)
    * [2. Build the Application](#2-build-the-application)
    * [3. Run the Application](#3-run-the-application)
        * [Option A: Run the Java Application Directly (JAR)](#option-a-run-the-java-application-directly-jar)
        * [Option B: Run with Docker](#option-b-run-with-docker)
* [Testing](#testing)
    * [Automated Tests](#automated-tests)
    * [Manual API Testing](#manual-api-testing)
* [API Endpoints](#api-endpoints)
* [Further Enhancements (Extension Points)](#further-enhancements-extension-points)
* [License](#license)

## Features

* **GitHub Gists Retrieval:** Fetches public Gists for a specified GitHub username (`octocat` is a good test user).
* **Simple HTTP Web Server:** Custom implementation using Java's built-in `com.sun.net.httpserver`.
* **Basic Error Handling:** Returns appropriate HTTP status codes (e.g., 400, 404, 429, 500) with JSON error messages.
* **Automated Testing:** Includes a JUnit 5 test suite to validate API functionality.
* **Containerized:** Packaged into a Docker image for consistent execution.
* **Lean Build:** Uses `.gitignore` and `.dockerignore` for efficient version control and Docker builds.

## Technologies Used

* **Core Language:** Java 17+
* **HTTP Server:** `com.sun.net.httpserver` (built-in Java)
* **HTTP Client:** `java.net.http.HttpClient` (built-in Java 11+)
* **JSON Processing:** Google Gson
* **Build Tool:** Apache Maven
* **Testing:** JUnit 5
* **Containerization:** Docker

## Prerequisites

To run, build, and test this application on your local machine, you need the following software installed:

* **Git:** For cloning the repository.
* **Java Development Kit (JDK) 11 or higher (JDK 17 recommended):** The runtime and development environment for Java.
* **Apache Maven 3.6.0 or higher:** The build automation tool.
* **Docker Desktop:** Essential for building and running Docker containers locally.

## Getting Started (Setup & Run)

Follow these steps to get the application running on your local machine.

### 1. Clone the Repository

Open your terminal or command prompt and clone the project:
git clone <your-repository-url> # Replace with your actual repository URL
cd equal-experts-mindful-mature-talented-sight-18baf3c8ca41

### 2. Build the Application
This step compiles the Java code, runs tests, and packages the application into a single, executable JAR file (a "fat JAR") that includes all its dependencies.


mvn clean install
Expected Output: You should see [INFO] BUILD SUCCESS at the end. This command will also run the automated tests. If tests fail, it will report the failures.
### 3. Run the Application
You have two options to run the application: directly as a Java JAR or as a Docker container.

**Option A: Run the Java Application Directly (JAR)**
This is the quickest way to run the application for development and testing without Docker.

java -jar target/simple-github-gists-api-1.0.0-SNAPSHOT.jar
Expected Output:
Server started on port 8080
Access health check at http://localhost:8080/health
Access Gists API at http://localhost:8080/<username>
The server will continue to run in your terminal until you stop it (e.g., by pressing Ctrl+C).

**Option B: Run with Docker**
This method demonstrates running the application as a Docker container, providing an isolated and consistent environment.

Ensure the JAR is built (from Step 2 above).

Build the Docker image:
docker build -t simple-gists-api:latest .

Expected Output: You'll see Docker layers being built, ending with Successfully tagged simple-gists-api:latest.

Run the Docker container:
docker run -p 8080:8080 simple-gists-api:latest

Expected Output: Similar to the direct JAR run, you'll see the server startup messages.
The -p 8080:8080 flag maps port 8080 from your host machine to port 8080 inside the container.
The container will run in the foreground. To run in the background, add -d: docker run -d -p 8080:8080 simple-gists-api:latest.

Testing
Automated Tests
The automated tests are run automatically as part of mvn clean install. You can also run them independently:

mvn test
Expected Output:
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running GistServerTest
GistServerTest: Waiting for server to start on port 8081...
GistServerTest: Server wait complete.
Health Check Response Status: 200
Health Check Response Body: OK
Octocat Gists Response Status: 200
Octocat Gists Response Body: [...] (JSON content)
NonExistentUser Response Status: 404
NonExistentUser Response Body: {"error": "GitHub user not found: nonexistentuser123456789"}
Root Path Response Status: 400
Root Path Response Body: {"message": "Please specify a GitHub username, e.g., /octocat"}
GistServerTest: Stopping server...
GistServerTest: Server stopped.
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
All 4 tests should pass. The System.out.println messages will give you insights into the actual HTTP responses during testing.

**Manual API Testing**
Once the application is running (via direct Java execution or Docker), you can test the endpoints using curl or a web browser:

Health check:

curl http://localhost:8080/health
Expected: OK
Fetch Gists for 'octocat' (example user):


curl http://localhost:8080/octocat
Expected: A JSON array of gists for the octocat user.
Test with a non-existent user (should return 404 Not Found):


curl -v http://localhost:8080/nonexistentuser123456789
Expected: HTTP 404 status code with a JSON error message like {"error": "GitHub user not found: nonexistentuser123456789"}.
Test root path (should return 400 Bad Request):

curl -v http://localhost:8080/
Expected: HTTP 400 status code with a JSON error message like {"message": "Please specify a GitHub username, e.g., /octocat"}.

Further Enhancements (Extension Points)
Authentication/Authorization for GitHub API:
Allow providing a GitHub Personal Access Token (PAT) via an environment variable to increase the API rate limit for the GitHubApiClient.
Deployment & Orchestration (Beyond Docker):
Kubernetes: Create basic Kubernetes Deployment and Service manifests.
Helm: Package the application into a Helm chart for easier Kubernetes deployment and management.
Cloud Deployment: Deploy the Docker image to a cloud provider like AWS (e.g., ECS, EKS) using Infrastructure as Code (IaC) tools like Terraform to provision infrastructure.
CI/CD Pipeline:
Set up a GitHub Actions workflow or Jenkins pipeline to automatically build, test, and containerize the application on every push to the repository.
Extend the CI/CD pipeline for continuous deployment to a development/staging environment.
