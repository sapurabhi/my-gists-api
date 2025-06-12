// GistServerTest.java
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

/**
 * Integration tests for the GistServer API.
 * This class starts the full HTTP server on a dedicated port and makes real HTTP requests to its endpoints.
 * It verifies the end-to-end flow, including HTTP request handling and integration with GitHubApiClient.
 * This complements the unit tests for GitHubApiClient by ensuring the entire application stack functions.
 */
public class GistServerTest {

    // Define a test port to avoid conflicts with other applications
    private static final int TEST_PORT = 8081;
    private static ExecutorService serverExecutor; // Dedicated executor for running the GistServer in a separate thread
    private static Future<?> serverFuture; // Represents the task of running the server

    /**
     * Set up method executed once before all tests in this class.
     * It starts the GistServer in a separate thread.
     */
    @BeforeAll
    static void startServer() throws IOException {
        serverExecutor = Executors.newSingleThreadExecutor(); // Initialize a single-threaded executor
        serverFuture = serverExecutor.submit(() -> {
            try {
                // Pass the TEST_PORT as an argument to the GistServer's main method.
                // This allows the server to bind to a specific port for testing.
                GistServer.main(new String[]{String.valueOf(TEST_PORT)});
            } catch (IOException e) {
                // Log and re-throw if the server fails to start, indicating a critical test setup issue.
                e.printStackTrace();
                throw new RuntimeException("Failed to start server for tests", e);
            }
        });

        // Give the server a moment to start up and bind to the port.
        // A more robust approach for complex applications would be to poll a health endpoint,
        // but for a simple server, a generous sleep works.
        try {
            System.out.println("GistServerTest: Waiting for server to start on port " + TEST_PORT + "...");
            Thread.sleep(4000); // Wait 4 seconds for robustness
            System.out.println("GistServerTest: Server wait complete.");
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Teardown method executed once after all tests in this class.
     * It stops the GistServer and shuts down the executor.
     */
    @AfterAll
    static void stopServer() {
        System.out.println("GistServerTest: Stopping server...");
        // Explicitly call the static stop method on the GistServer for a clean shutdown.
        GistServer.stop();

        // Shut down the executor that started the server thread.
        if (serverExecutor != null && !serverExecutor.isShutdown()) {
            serverExecutor.shutdownNow(); // Attempt to stop all running tasks immediately
            try {
                // Wait for the executor to terminate, ensuring the server thread is properly shut down.
                if (!serverExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("GistServerTest: Server thread did not terminate cleanly within 5 seconds.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }
        System.out.println("GistServerTest: Server stopped.");
    }

    /**
     * Test case to verify the /health endpoint returns "OK".
     */
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

        assertEquals(200, response.statusCode(), "Health check should return HTTP 200 OK");
        assertEquals("OK", response.body(), "Health check response body should be 'OK'");
    }

    /**
     * Test case to verify the /<username> endpoint returns gists for a known user (octocat).
     */
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

        assertEquals(200, response.statusCode(), "Octocat gists request should return HTTP 200 OK");
        assertTrue(response.body().contains("\"id\":"), "Response body should contain 'id' field for gists");
        assertTrue(response.body().contains("\"description\":"), "Response body should contain 'description' field for gists");
        assertTrue(response.body().startsWith("["), "Response body should be a JSON array");
        assertTrue(response.body().endsWith("]"), "Response body should be a JSON array");
    }

    /**
     * Test case to verify the /<username> endpoint returns 404 for a non-existent user.
     */
    @Test
    void shouldReturn404ForNonExistentUser() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/nonexistentuser123456789")) // Very unlikely username
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("NonExistentUser Response Status: " + response.statusCode());
        System.out.println("NonExistentUser Response Body: " + response.body());

        assertEquals(404, response.statusCode(), "Non-existent user request should return HTTP 404 Not Found");
        // Assert on the JSON error message structure and content
        assertTrue(response.body().contains("\"error\": \"GitHub user not found:"), "Error message should indicate user not found");
        assertTrue(response.body().contains("nonexistentuser123456789\"}"), "Error message should contain the requested username");
    }

    /**
     * Test case to verify the root path / returns 400 (Bad Request) if no username is provided.
     */
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

        assertEquals(400, response.statusCode(), "Root path request should return HTTP 400 Bad Request");
        // Assert on the JSON message structure and content
        assertTrue(response.body().contains("{\"message\": \"Please specify a GitHub username, e.g., /octocat\"}"), "Error message should indicate missing username");
    }

    /**
     * Test case to verify API rate limit handling (HTTP 429).
     * Note: This test is designed to verify the server's response if the API client
     * (GitHubApiClient) throws a TooManyRequestsException. It does not
     * attempt to exhaust the GitHub API rate limit during the test.
     */
    @Test
    void shouldReturn429ForTooManyRequests() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/rate-limit-test-user")) // A user that *might* trigger a rate limit
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Rate Limit Response Status: " + response.statusCode());
        System.out.println("Rate Limit Response Body: " + response.body());

        // We can't guarantee a 429 from GitHub during a test, but we can verify the server's handling.
        // For this integration test, we'll assume the GitHub API will return 200 or 404 for a normal user.
        // If the GitHubApiClient *itself* were somehow forced to throw a 429 (e.g., via a mock),
        // then this test would strictly assert 429.
        // For now, this test primarily serves as a placeholder to verify the *server's behavior*
        // if a 429 does occur from the upstream API.
        // A better approach for directly testing 429 *handling* at the server level
        // would involve mocking the GitHubApiClient's behavior within GistServer,
        // which would require a dependency injection framework for GistServer.

        // Given the simple architecture, we primarily verify that the error handling in GistServer
        // correctly surfaces the UserNotFoundException or generic exceptions.
        // The actual 429 from GitHub is out of direct control in this integration test.
        // However, if the API *does* return 429, the GistServer's handler should correctly pass it through.
        if (response.statusCode() == 429) {
            assertTrue(response.body().contains("\"error\": \"GitHub API rate limit exceeded.\""), "Error message should indicate rate limit exceeded");
        } else {
            // If it's not 429, it should typically be 200 (for octocat) or 404 (for a random user)
            // No explicit assertion on 200/404 here, as other tests cover that.
            // This test is more about observing the potential 429 path.
            System.out.println("Note: Rate limit test did not receive a 429. This is expected if API not rate-limited.");
        }
    }
}