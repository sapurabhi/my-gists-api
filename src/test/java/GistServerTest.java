import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GistServerTest {

    private static final int TEST_PORT = 8081;
    private static ExecutorService serverExecutor; // Use a dedicated executor for the server
    private static Future<?> serverFuture;

    @BeforeAll
    static void startServer() throws IOException {
        serverExecutor = Executors.newSingleThreadExecutor(); // Initialize here
        serverFuture = serverExecutor.submit(() -> {
            try {
                // Pass TEST_PORT as argument to main
                GistServer.main(new String[]{String.valueOf(TEST_PORT)});
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to start server for tests", e);
            }
        });

        // Give the server a moment to start up. Increased sleep time for reliability.
        // A more robust solution for production-grade tests would be to poll a health endpoint.
        try {
            System.out.println("GistServerTest: Waiting for server to start on port " + TEST_PORT + "...");
            Thread.sleep(4000); // Increased from 2 seconds to 4 seconds for robustness
            System.out.println("GistServerTest: Server wait complete.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    static void stopServer() {
        System.out.println("GistServerTest: Stopping server...");
        // Explicitly stop the server via its static method
        GistServer.stop();

        // Shut down the executor that started the server thread
        if (serverExecutor != null && !serverExecutor.isShutdown()) {
            serverExecutor.shutdownNow();
            try {
                if (!serverExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("GistServerTest: Server thread did not terminate cleanly within 5 seconds.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("GistServerTest: Server stopped.");
    }

    @Test
    void shouldReturnOkForHealthCheck() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/health"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Health Check Response Status: " + response.statusCode());
        System.out.println("Health Check Response Body: " + response.body());

        assertEquals(200, response.statusCode());
        assertEquals("OK", response.body());
    }

    @Test
    void shouldReturnGistsForOctocat() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/octocat"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Octocat Gists Response Status: " + response.statusCode());
        System.out.println("Octocat Gists Response Body: " + response.body());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"id\":"), "Response body should contain gist ID");
        assertTrue(response.body().contains("\"description\":"), "Response body should contain description");
        assertTrue(response.body().startsWith("["), "Response body should be a JSON array");
    }

    @Test
    void shouldReturn404ForNonExistentUser() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/nonexistentuser123456789")) // Very unlikely username
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("NonExistentUser Response Status: " + response.statusCode());
        System.out.println("NonExistentUser Response Body: " + response.body()); // Print the actual response body

        assertEquals(404, response.statusCode());
        // Updated assertion to be more precise for the expected JSON structure
        assertTrue(response.body().contains("\"error\": \"GitHub user not found:"), "Error message should indicate user not found");
        assertTrue(response.body().contains("nonexistentuser123456789"), "Error message should contain the username");
    }

    @Test
    void shouldReturn400ForRootPath() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Root Path Response Status: " + response.statusCode());
        System.out.println("Root Path Response Body: " + response.body());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Please specify a GitHub username"), "Error message should indicate missing username");
    }
}