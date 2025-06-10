import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class GistServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Context for /health endpoint
        server.createContext("/health", GistServer::handleHealthCheck);

        // Start the server
        server.setExecutor(null); // use default executor
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

    // Placeholder for Gists handler
    private static void handleGistsRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        // Path will be like "/octocat"
        String username = path.substring(1); // Remove leading slash

        String response = "Fetching gists for: " + username + " (Not implemented yet)";
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}