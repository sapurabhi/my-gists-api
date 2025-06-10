import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson; // Ensure Gson is imported
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors; // For fixed thread pool

public class GistServer {

    private static int PORT = 8080; // Make it non-final so it can be set by args
    private static HttpServer server; // Keep a reference to the server for potential stopping
    private static final GitHubApiClient gitHubApiClient = new GitHubApiClient();
    private static final Gson gson = new Gson(); // Re-use Gson instance

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            try {
                PORT = Integer.parseInt(args[0]); // Allow port to be passed as argument
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument. Using default port " + PORT);
            }
        }

        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/health", GistServer::handleHealthCheck);
        server.createContext("/", GistServer::handleGistsRequest); // Catches all paths starting with /

        server.setExecutor(Executors.newFixedThreadPool(10)); // Use a thread pool for handling requests
        server.start();

        System.out.println("Server started on port " + PORT);
        System.out.println("Access health check at http://localhost:" + PORT + "/health");
        System.out.println("Access Gists API at http://localhost:" + PORT + "/<username>");
    }

    // Optional: Add a stop method for graceful shutdown in tests/applications
    public static void stop() {
        if (server != null) {
            server.stop(0); // Stop immediately
            System.out.println("Server stopped.");
        }
    }

    private static void handleHealthCheck(HttpExchange exchange) throws IOException {
        String response = "OK";
        sendResponse(exchange, 200, response, "text/plain");
    }

    private static void handleGistsRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            String response = "{\"message\": \"Please specify a GitHub username, e.g., /octocat\"}";
            sendResponse(exchange, 400, response, "application/json");
            return;
        }

        String username = path.substring(1); // Remove leading slash
        String responseBody = "{}"; // Initialize with an empty JSON object as a safe default
        int statusCode = 200;
        String contentType = "application/json"; // Default for JSON responses

        try {
            List<Gist> gists = gitHubApiClient.getUserGists(username);
            responseBody = gson.toJson(gists);
        } catch (GitHubApiClient.UserNotFoundException e) {
            statusCode = 404;
            responseBody = "{\"error\": \"" + e.getMessage() + "\"}";
        } catch (GitHubApiClient.TooManyRequestsException e) {
            statusCode = 429;
            responseBody = "{\"error\": \"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            statusCode = 500;
            responseBody = "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
            System.err.println("Error fetching gists for " + username + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            sendResponse(exchange, statusCode, responseBody, contentType);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String responseBody, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, responseBody.length());
        OutputStream os = exchange.getResponseBody();
        os.write(responseBody.getBytes());
        os.close();
    }
}