# 🧹 Simple GitHub Gists API

This project implements a minimal HTTP web server in Java that interacts directly with the [GitHub Gists API](https://docs.github.com/en/rest/gists/gists). It allows retrieving a public list of gists for any GitHub user.

> ✅ Designed for learning and demonstration purposes with a clean, framework-free architecture.

---

## 📚 Table of Contents

* [Features](#features)
* [Technologies Used](#technologies-used)
* [Prerequisites](#prerequisites)
* [Getting Started (Setup & Run)](#getting-started-setup--run)

  * [1. Clone the Repository](#1-clone-the-repository)
  * [2. Build the Application (Maven)](#2-build-the-application-maven)
  * [3. Run the Application](#3-run-the-application)

    * [Option A: Run the Java Application Directly (JAR)](#option-a-run-the-java-application-directly-jar)
    * [Option B: Run with Docker](#option-b-run-with-docker)
  * [4. Local Development with Minikube and Helm](#4-local-development-with-minikube-and-helm)
  * [5. Cloud Deployment with GitHub Actions CI/CD](#5-cloud-deployment-with-github-actions-cicd)
* [Testing](#testing)
* [API Endpoints](#api-endpoints)
* [Management and Troubleshooting](#management-and-troubleshooting)
* [Best Practices Implemented](#best-practices-implemented)
* [Further Enhancements (Extension Points)](#further-enhancements-extension-points)
* [License](#license)

---

## ✅ Features

* 🔍 **Public Gists Retrieval** – Fetch public gists for a specified GitHub username.
* 🌐 **Minimal Web Server** – Uses Java’s native `com.sun.net.httpserver`.
* 💠 **Error Handling** – Gracefully handles 400/404/429/500 with JSON responses.
* 🧪 **Automated Testing** – JUnit 5 test suite included.
* 🐳 **Containerized** – Lightweight Docker image.
* ☕️ **Kubernetes-Ready** – Deployable via Helm on Minikube or any K8s cluster.
* 🔁 **CI/CD Ready** – Includes GitHub Actions workflow.
* 🪟 **Clean Build Artifacts** – Uses `.gitignore` and `.dockerignore` to reduce noise.

---

## 🪠 Technologies Used

| Layer            | Tool/Library                            |
| ---------------- | --------------------------------------- |
| Language         | Java 17                                 |
| HTTP Server      | `com.sun.net.httpserver` (JDK built-in) |
| HTTP Client      | `HttpClient` (Java 11+)                 |
| JSON             | Google Gson                             |
| Build Tool       | Apache Maven                            |
| Testing          | JUnit 5                                 |
| Containerization | Docker                                  |
| Orchestration    | Kubernetes (via Minikube)               |
| Deployment       | Helm                                    |
| CI/CD            | GitHub Actions                          |

---

## 🛠️ Prerequisites

Ensure you have the following tools installed:

* Git
* Java JDK 17+
* Apache Maven 3.6+
* Docker Desktop
* Minikube
* kubectl
* Helm

---

## 🚀 Getting Started (Setup & Run)

### 1. Clone the Repository

```bash
git clone https://github.com/EqualExperts-Assignments/equal-experts-mindful-mature-talented-sight-18baf3c8ca41.git
cd equal-experts-mindful-mature-talented-sight-18baf3c8ca41
```

### 2. Build the Application (Maven)

```bash
mvn clean install
```

Expected Output:

* Compiled Java classes
* JAR file in the `target/` directory
* All tests should pass with `BUILD SUCCESS`

---

### 3. Run the Application

You have two options to run the application: directly as a Java JAR or as a Docker container.

#### Option A: Run the Java Application Directly (JAR)

```bash
java -jar target/simple-github-gists-api-1.0.0-SNAPSHOT.jar
```

Expected Output:

* Server started on port 8080
* Access health check at [http://localhost:8080/health](http://localhost:8080/health)
* Access Gists API at [http://localhost:8080/octocat](http://localhost:8080/octocat)
* Server will run until you stop it (e.g., Ctrl+C)

#### Option B: Run with Docker

Ensure the JAR is built (from Step 2).

```bash
docker build -t simple-gists-api:latest .
docker run -p 8080:8080 simple-gists-api:latest
```

Expected Output:

* Docker image is built
* Application runs inside container
* Accessible at same endpoints as direct run

To run in the background:

```bash
docker run -d -p 8080:8080 simple-gists-api:latest
```

---

### 4. Local Development with Minikube and Helm

#### A. Install Minikube & Kubectl

Follow [Minikube installation guide](https://minikube.sigs.k8s.io/docs/start/).

#### B. Start Minikube Cluster

```bash
minikube start --driver=docker (preferred if Docker Desktop is installed)
```
#### C. Verify Kubectl Context:
```bash
kubectl config current-context
# Expected output: minikube
```

```bash
kubectl get nodes
# Expected output: minikube Ready ...
```

#### D. Build Docker Image Inside Minikube

```bash
minikube docker-env | Invoke-Expression # Connects your shell to Minikube's Docker daemon
minikube docker-env -U | Invoke-Expression # Disconnects from Minikube's Docker daemon (optional, but good practice)
 
```

#### E. Deploy with Helm

Create a Helm chart (e.g., `charts/simple-gists-api`). Example `values.yaml`:


Deploy using:

```bash
helm install simple-gists-api ./charts/simple-gists-api #First time installation
# rebuild docker image if not present -> docker build -t simple-gists-api:latest . 
cd helm/simple-gists-api
# Lint your chart for best practices
helm lint .
# Install or upgrade your release.
# We set the image.repository and image.tag to use the locally built image. 
#PowerShell-specific syntax. for Bash/Linux shells use The backslash \ character instead  . `
helm upgrade --install simple-gists-api-release . `
  --set image.repository=docker.io/library/simple-gists-api `
  --set image.tag=latest `
  --set service.type=NodePort

```
#### F. Verify Helm Deployment
```bash
helm list
# Expected output: simple-gists-api-release deployed ...

kubectl get deployments -n default -o wide
kubectl get pods -n default -o wide
kubectl get services 
```

#### G. Access Endpoints


```bash
minikube service simple-gists-api-release --url
```
Use the provided URL for curl/browser testing.


```bash
#If using Kind, or if minikube service doesn't work: You might need to use kubectl port-forward to access the service:
kubectl port-forward service/github-gists-api 8080:8080 #Then, access it via http://localhost:8080.
```

---

### 5. Cloud Deployment with GitHub Actions CI/CD

#### A. Configure Secrets

* `DOCKERHUB_USERNAME`
* `DOCKERHUB_TOKEN`
* `KUBE_CONFIG_DATA` (Base64 encoded kubeconfig)

```bash
📦 GitHub Actions CI/CD
Once the GitHub Actions workflows are configured in your repository (under .github/workflows/), you can trigger them in several ways:

🔁 How to Trigger GitHub Actions Workflows
On Push: Push to main or any configured branch automatically.

Manually via GitHub UI (for workflow_dispatch enabled workflows):
Go to your repository on GitHub.
Click on the Actions tab.
Choose the workflow you want to run.
Click Run workflow (select branch if prompted).

Via API (Advanced):
You can use GitHub's REST API to trigger a workflow dispatch event using a personal access token (PAT).
```
* **`ci.yaml`:**
    * Triggers on every push to `solution` (or pull requests).
    * Builds the Java application.
    * Runs unit and integration tests.
    * Builds the Docker image.
    * Pushes the Docker image to the ECR repository provisioned by Terraform.
* **`cd.yaml`:**
    * Triggers on pushes to the `main` branch (after `ci.yaml` completes successfully).
    * Configures `kubectl` and `helm` to connect to the EKS cluster.
    * Deploys/updates the application on EKS using the Helm chart, referencing the image pushed to ECR.
---

## 🔧 Testing

### Automated Tests (Local)

```bash
mvn test
```

Expected Output:

```
T E S T S
Running GistServerTest
GistServerTest: Waiting for server to start on port 8081...
...
Health Check Response Status: 200
Health Check Response Body: OK
Octocat Gists Response Status: 200
...
NonExistentUser Response Status: 404
...
Root Path Response Status: 400
...
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Manual API Testing (Local)

With the app running (JAR or Docker):

```bash
curl http://localhost:8080/health
```

Expected:

```
OK
```

```bash
curl http://localhost:8080/octocat
```

Expected:

* JSON array of public gists

```bash
curl -v http://localhost:8080/nonexistentuser123456789
```

Expected:

* HTTP 404 with `{"error": "GitHub user not found: nonexistentuser123456789"}`

```bash
curl -v http://localhost:8080/
```

Expected:

* HTTP 400 with `{"message": "Please specify a GitHub username, e.g., /octocat"}`

---

## 🚧 API Endpoints

| Endpoint      | Method | Description                 |
| ------------- | ------ | --------------------------- |
| `/health`     | GET    | Health check endpoint       |
| `/{username}` | GET    | Fetch gists for GitHub user |

---

## 🌓 Management and Troubleshooting

* Logs printed to console (stdout)
* For Docker: use `docker logs <container_id>`
* For K8s: use `kubectl logs <pod>`

---

## 🌎 Best Practices Implemented

* Separation of concerns
* Consistent naming conventions
* Lightweight containers
* Health checks
* Clean and readable code
* Automated CI/CD pipeline

---

## 🧪 Further Enhancements (Extension Points)

* Infrastructure-as-code (Terraform)
* OAuth Token support for authenticated GitHub access
* Pagination and filtering for gists
* HTTPS support
* Request rate limiting
* Caching for performance

---

## 📚 License

This project is licensed under the MIT License.

---

Made with ❤️ by Equal Experts Assignment Team
