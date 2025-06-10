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

    private static final int TEST_PORT = 8081; // Use a different port for tests
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future<?> serverFuture;

    @BeforeAll
    static void startServer() throws IOException {
        // Start the server in a separate thread
        serverFuture = executor.submit(() -> {
            try {
                GistServer.main(new String[]{String.valueOf(TEST_PORT)}); // Pass test port to main
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to start server for tests", e);
            }
        });

        // Give the server a moment to start up
        try {
            Thread.sleep(2000); // Wait 2 seconds for server to bind
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    static void stopServer() {
        // This is a simple server, relies on process termination in real use.
        // For testing, we stop the executor and assume the server process will exit.
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Server thread did not terminate.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // In a real scenario, you'd want to expose a graceful shutdown method in GistServer
        // For this basic example, we rely on the test process finishing.
    }

    @Test
    void shouldReturnOkForHealthCheck() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/health"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("User not found"), "Error message should indicate user not found");
    }

    // Note: Testing 429 Too Many Requests is difficult in an automated test
    // without deliberately exhausting the GitHub API rate limit, which is not ideal.
    // It's covered by the GitHubApiClient unit test if we were to create one separately.
    // For this simple case, we trust the error handling in GitHubApiClient.

    @Test
    void shouldReturn400ForRootPath() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Please specify a GitHub username"), "Error message should indicate missing username");
    }
}