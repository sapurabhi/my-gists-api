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
* [Management and Troubleshooting](#management-and-troubleshooting)
* [Best Practices Implemented](#best-practices-implemented)
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

Option A: Run the Java Application Directly (JAR)
This is the quickest way to run the application for development and testing without Docker.

java -jar target/simple-github-gists-api-1.0.0-SNAPSHOT.jar
Expected Output:
Server started on port 8080
Access health check at http://localhost:8080/health
Access Gists API at http://localhost:8080/<username>
The server will continue to run in your terminal until you stop it (e.g., by pressing Ctrl+C).
Option B: Run with Docker
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
Manual API Testing
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

You've asked for crucial elements for any software project, especially when demonstrating to an interviewer! Let's cover .gitignore, .dockerignore, best practices, a detailed README.md, and a narrative explanation.

1. .gitignore and .dockerignore Files
These files are essential for proper version control and efficient Docker builds.

.gitignore
Purpose: Tells Git which files or directories to intentionally ignore from your repository. This includes generated files, build artifacts, IDE-specific files, and sensitive information.
Best Practices:
Ignore Build Outputs: Always ignore the target/ directory for Maven projects.
Ignore IDE Files: Files generated by your IDE (e.g., IntelliJ's .idea/, VS Code's .vscode/) should be ignored as they are local to your development environment.
Ignore OS-Specific Files: Files like .DS_Store (macOS) or Thumbs.db (Windows) are specific to the operating system and irrelevant to the project code.
Ignore Logs and Temporary Files: Any log files (*.log) or temporary directories (temp/, tmp/) generated during runtime or development.
Ignore Sensitive Data: If you were to add configuration files with credentials, they should be ignored. (Not directly applicable here, but good practice).
Create simple-github-gists-api/.gitignore:

# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar # Wrapper JAR itself can be ignored, but not the properties
.mvn/wrapper/maven-wrapper.properties # Keep this if you use Maven Wrapper

# Eclipse
.classpath
.project
.settings/

# IntelliJ IDEA
.idea/
*.iml
*.ipr
*.iws

# VS Code
.vscode/

# OS-specific files
.DS_Store
Thumbs.db

# Logs and other temporary files
*.log
*.bak
*.tmp
temp/
tmp/

# JRE crash dumps
hs_err_pid*.log

# Environment variables or secrets (if applicable)
.env
*.env
.dockerignore
Purpose: Tells the Docker daemon which files or directories to exclude from the build context when you run docker build. This is crucial for:
Faster Builds: Sending fewer files to the Docker daemon means quicker context transfer.
Smaller Images: Prevents unnecessary files from being copied into the final image, reducing its size.
Security: Avoids including sensitive information or irrelevant development files in the image.
Best Practices:
Exclude Version Control Metadata: .git/, .gitignore are not needed in the image.
Exclude Source Code and Build Artifacts (except the final JAR): Your Dockerfile explicitly copies target/simple-github-gists-api-1.0.0-SNAPSHOT.jar. You don't need the src/ directory, pom.xml, or the rest of the target/ contents in the build context.
Exclude Documentation/Dev Files: README.md, LICENSE, IDE configurations are not needed for runtime.
Exclude Any Temporary Files: Logs, temp directories, etc.
Create simple-github-gists-api/.dockerignore:

# Exclude Git-related files and directories
.git/
.gitignore
.gitattributes

# Exclude source code, as only the compiled JAR is needed in the image
src/

# Exclude Maven build files, as only the final JAR is needed
pom.xml
target/ # Exclude the entire target directory, then COPY the specific JAR later

# Exclude IDE-specific files
.idea/
.vscode/
*.iml
*.ipr
*.iws

# Exclude OS-specific files
.DS_Store
Thumbs.db

# Exclude documentation and other non-runtime files
README.md
LICENSE

# Exclude temporary files and logs that might be generated locally
*.log
*.bak
*.tmp
temp/
tmp/
2. Best Practices for the Entire Solution
Beyond just ignore files, here are best practices demonstrated or implied in this solution:

Clear Code Structure (Separation of Concerns):
GistServer: Handles HTTP requests, acts as the "controller".
GitHubApiClient: Encapsulates logic for interacting with the external GitHub API.
Gist, GistFile: Simple Plain Old Java Objects (POJOs) for data modeling.
This makes the code easier to read, understand, and maintain.
Robust Error Handling:
Uses meaningful HTTP status codes (400, 404, 429, 500) to communicate API outcomes to clients.
Provides clear, JSON-formatted error messages.
Employs custom exceptions (UserNotFoundException, TooManyRequestsException) for specific error scenarios, making the error flow clearer.
Modern HTTP Client Usage:
Leverages Java 11's java.net.http.HttpClient for efficient and standard HTTP communication, avoiding older, more cumbersome APIs or heavy third-party libraries for simple needs.
Automated Testing (JUnit 5):
Includes a dedicated test suite (GistServerTest) to validate the server's behavior automatically.
Uses @BeforeAll and @AfterAll for reliable server startup and shutdown during tests, ensuring test isolation and repeatability.
Tests cover different scenarios: health check, successful data retrieval, user not found, and bad request.
Uses System.out.println within tests for debugging, which is helpful in simple scenarios to see actual responses.
Maven Build Management:
Uses pom.xml for declarative dependency management and build lifecycle control.
maven-compiler-plugin: Ensures code is compiled for the target Java version.
maven-surefire-plugin with junit-platform-surefire-provider: Correctly discovers and executes JUnit 5 tests.
maven-shade-plugin: Creates a self-contained "fat JAR" which simplifies deployment by bundling all dependencies into a single executable file.
Efficient Containerization (Docker):
Minimal Base Image: Uses openjdk:17-jdk-slim, a smaller image based on Debian slim, reducing image size and attack surface.
Layer Optimization: The Dockerfile copies the pre-built JAR, meaning source code compilation happens outside the container, improving build speed.
Correct WORKDIR: Sets a working directory for consistency.
EXPOSE Port: Clearly indicates the port the application listens on.
ENTRYPOINT: Defines the main command to run, making the container behave like an executable.
.dockerignore: Crucial for creating lean and efficient Docker images.
Documentation:
A comprehensive README.md is provided, detailing setup, running, testing, API endpoints, and potential enhancements, making the project easy for others to understand and use.
Version Control:
Utilizes Git for source code management.
Employs .gitignore to keep the repository clean and focused on essential source files.
3. Detailed README.md File
This README.md combines all the information we've discussed, including the fixes and best practices, presented in a clear, step-by-step manner.

README.md:

Markdown

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
* [Management and Troubleshooting](#management-and-troubleshooting)
* [Best Practices Implemented](#best-practices-implemented)
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

```bash
git clone <your-repository-url> # Replace with your actual repository URL
cd simple-github-gists-api
2. Build the Application
This step compiles the Java code, runs tests, and packages the application into a single, executable JAR file (a "fat JAR") that includes all its dependencies.

Bash

mvn clean install
Expected Output: You should see [INFO] BUILD SUCCESS at the end. This command will also run the automated tests. If tests fail, it will report the failures.
3. Run the Application
You have two options to run the application: directly as a Java JAR or as a Docker container.

Option A: Run the Java Application Directly (JAR)
This is the quickest way to run the application for development and testing without Docker.

Bash

java -jar target/simple-github-gists-api-1.0.0-SNAPSHOT.jar
Expected Output:
Server started on port 8080
Access health check at http://localhost:8080/health
Access Gists API at http://localhost:8080/<username>
The server will continue to run in your terminal until you stop it (e.g., by pressing Ctrl+C).
Option B: Run with Docker
This method demonstrates running the application as a Docker container, providing an isolated and consistent environment.

Ensure the JAR is built (from Step 2 above).

Build the Docker image:

Bash

docker build -t simple-gists-api:latest .
Expected Output: You'll see Docker layers being built, ending with Successfully tagged simple-gists-api:latest.
Run the Docker container:

Bash

docker run -p 8080:8080 simple-gists-api:latest
Expected Output: Similar to the direct JAR run, you'll see the server startup messages.
The -p 8080:8080 flag maps port 8080 from your host machine to port 8080 inside the container.
The container will run in the foreground. To run in the background, add -d: docker run -d -p 8080:8080 simple-gists-api:latest.
Testing
Automated Tests
The automated tests are run automatically as part of mvn clean install. You can also run them independently:

Bash

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
Manual API Testing
Once the application is running (via direct Java execution or Docker), you can test the endpoints using curl or a web browser:

Health check:

Bash

curl http://localhost:8080/health
Expected: OK
Fetch Gists for 'octocat' (example user):

Bash

curl http://localhost:8080/octocat
Expected: A JSON array of gists for the octocat user.
Test with a non-existent user (should return 404 Not Found):

Bash

curl -v http://localhost:8080/nonexistentuser123456789
Expected: HTTP 404 status code with a JSON error message like {"error": "GitHub user not found: nonexistentuser123456789"}.
Test root path (should return 400 Bad Request):

Bash

curl -v http://localhost:8080/
Expected: HTTP 400 status code with a JSON error message like {"message": "Please specify a GitHub username, e.g., /octocat"}.
GitHub API Rate Limits: Be aware that the public GitHub API has strict unauthenticated rate limits (typically 60 requests per hour). If you hit this limit, you'll receive a 429 Too Many Requests error. Waiting for an hour usually resolves this, or consider providing a GitHub Personal Access Token (PAT) as an enhancement (see below).

API Endpoints
GET /health:

Description: A simple health check endpoint.
Response: OK (text/plain)
GET /<USER>:

Description: Retrieves a list of public Gists for the specified GitHub username.
Example: http://localhost:8080/octocat
Response: A JSON array of Gist objects, each containing id, description, url, created_at, and files (filename, type, language, raw URL, size).
Error Responses (JSON):
400 Bad Request: If no username is provided (e.g., accessing /).
404 Not Found: If the GitHub user does not exist.
429 Too Many Requests: If the GitHub API rate limit is exceeded.
500 Internal Server Error: For any unexpected errors during processing or GitHub API interaction.

