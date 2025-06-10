import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

// Custom exceptions directly within GistServer for simplicity, or keep them in GitHubApiClient as nested.
// For this example, keeping them in GitHubApiClient as nested public static classes is fine.

public class GistServer {

    private static final int PORT = 8080;
    private static final GitHubApiClient gitHubApiClient = new GitHubApiClient(); // Instantiate the client

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/health", GistServer::handleHealthCheck);
        // Context for /<username> endpoint
        server.createContext("/", GistServer::handleGistsRequest); // Catches all paths starting with /

        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port " + PORT);
        System.out.println("Access health check at http://localhost:" + PORT + "/health");
        System.out.println("Access Gists API at http://localhost:" + PORT + "/<username>");
    }

    private static void handleHealthCheck(HttpExchange exchange) throws IOException {
        String response = "OK";
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void handleGistsRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        // Handle root path / by showing instructions or returning an error
        if (path.equals("/")) {
            String response = "Please specify a GitHub username, e.g., /octocat";
            sendResponse(exchange, 400, response);
            return;
        }

        String username = path.substring(1); // Remove leading slash
        String responseBody;
        int statusCode = 200;

        try {
            List<Gist> gists = gitHubApiClient.getUserGists(username);
            responseBody = new Gson().toJson(gists); // Convert list of Gist objects to JSON
            exchange.getResponseHeaders().set("Content-Type", "application/json");
        } catch (GitHubApiClient.UserNotFoundException e) {
            statusCode = 404;
            responseBody = "{\"error\": \"" + e.getMessage() + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
        } catch (GitHubApiClient.TooManyRequestsException e) {
            statusCode = 429;
            responseBody = "{\"error\": \"" + e.getMessage() + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
        } catch (Exception e) {
            statusCode = 500;
            responseBody = "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}";
            System.err.println("Error fetching gists for " + username + ": " + e.getMessage());
            e.printStackTrace();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
        } finally {
            sendResponse(exchange, statusCode, responseBody);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.sendResponseHeaders(statusCode, responseBody.length());
        OutputStream os = exchange.getResponseBody();
        os.write(responseBody.getBytes());
        os.close();
    }
}