// GitHubApiClientTest.java
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the GitHubApiClient class.
 * Uses Mockito to mock HttpClient and HttpResponse, allowing tests
 * without making actual network calls.
 */
@ExtendWith(MockitoExtension.class) // Integrates Mockito with JUnit 5
public class GitHubApiClientTest {

    @Mock // Mock the HttpClient interface. This will be injected into GitHubApiClient.
    private HttpClient mockHttpClient;

    @Mock // Mock the HttpResponse interface to control HTTP responses.
    private HttpResponse<String> mockHttpResponse;

    // Use a real Gson instance. Gson is a utility; we typically test its usage, not Gson itself.
    private Gson realGson;

    // The class under test. We will manually inject the mocks into its constructor.
    private GitHubApiClient gitHubApiClient;

    // ArgumentCaptor to capture the HttpRequest object sent by the client.
    // This allows us to inspect the request details (like URI, headers) in assertions.
    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        realGson = new Gson();
        // Manually inject the mocked HttpClient and the real Gson instance into the GitHubApiClient.
        gitHubApiClient = new GitHubApiClient(mockHttpClient, realGson);
    }

    /**
     * Test case for successful retrieval of Gists (HTTP 200 OK).
     */
    @Test
    void getUserGists_shouldReturnGistsOn200OK() throws Exception {
        // Arrange: Define the expected username and a mock JSON response.
        String username = "testuser";
        String jsonResponse = "[{\"id\":\"1\", \"description\":\"Gist One\", \"url\":\"url1\", \"files\":{\"file1.txt\":{\"filename\":\"file1.txt\"}}}, {\"id\":\"2\", \"description\":\"Gist Two\", \"url\":\"url2\", \"files\":{\"file2.txt\":{\"filename\":\"file2.txt\"}}}]";

        // Mock behavior: When mockHttpClient.send is called, return mockHttpResponse.
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(mockHttpResponse);
        // Mock behavior: When mockHttpResponse.statusCode() is called, return 200.
        when(mockHttpResponse.statusCode()).thenReturn(200);
        // Mock behavior: When mockHttpResponse.body() is called, return our mock JSON.
        when(mockHttpResponse.body()).thenReturn(jsonResponse);

        // Act: Call the method under test.
        List<Gist> gists = gitHubApiClient.getUserGists(username);

        // Assert: Verify the returned Gists list.
        assertNotNull(gists, "Gists list should not be null");
        assertFalse(gists.isEmpty(), "Gists list should not be empty");
        assertEquals(2, gists.size(), "Gists list should contain 2 items");
        assertEquals("1", gists.get(0).getId(), "First gist ID should match");
        assertEquals("Gist One", gists.get(0).getDescription(), "First gist description should match");

        // Verify that HttpClient.send was called exactly once and capture the HttpRequest.
        verify(mockHttpClient, times(1)).send(requestCaptor.capture(), any(HttpResponse.BodyHandlers.ofString().getClass()));
        // Assert on the captured HttpRequest's URI path to ensure the correct endpoint was hit.
        assertTrue(requestCaptor.getValue().uri().toString().contains("/users/" + username + "/gists"), "Request URI should contain the correct user gists path");
        // Assert on the header (optional, but good for robust testing)
        assertEquals("application/vnd.github+json", requestCaptor.getValue().headers().firstValue("Accept").orElse(""), "Request should have correct Accept header");
    }

    /**
     * Test case for User Not Found scenario (HTTP 404).
     */
    @Test
    void getUserGists_shouldThrowUserNotFoundExceptionOn404() throws Exception {
        // Arrange
        String username = "nonexistentuser";
        // The errorBody is not read by GitHubApiClient in this case, so no need to stub mockHttpResponse.body()

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(404);
        // REMOVED: when(mockHttpResponse.body()).thenReturn(errorBody); // This line caused UnnecessaryStubbing

        // Act & Assert: Verify that UserNotFoundException is thrown with the correct message.
        Exception exception = assertThrows(GitHubApiClient.UserNotFoundException.class, () -> {
            gitHubApiClient.getUserGists(username);
        });

        assertEquals("GitHub user not found: " + username, exception.getMessage(), "Exception message should indicate user not found");
        verify(mockHttpClient, times(1)).send(requestCaptor.capture(), any(HttpResponse.BodyHandlers.ofString().getClass()));
        assertTrue(requestCaptor.getValue().uri().toString().contains("/users/" + username + "/gists"), "Request URI should be for the non-existent user");
    }

    /**
     * Test case for Too Many Requests scenario (HTTP 429).
     */
    @Test
    void getUserGists_shouldThrowTooManyRequestsExceptionOn429() throws Exception {
        // Arrange
        String username = "rate_limited_user";
        // The errorBody is not read by GitHubApiClient in this case, so no need to stub mockHttpResponse.body()

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(429);
        // REMOVED: when(mockHttpResponse.body()).thenReturn(errorBody); // This line caused UnnecessaryStubbing

        // Act & Assert: Verify that TooManyRequestsException is thrown with the correct message.
        Exception exception = assertThrows(GitHubApiClient.TooManyRequestsException.class, () -> {
            gitHubApiClient.getUserGists(username);
        });

        assertEquals("GitHub API rate limit exceeded.", exception.getMessage(), "Exception message should indicate rate limit exceeded");
        verify(mockHttpClient, times(1)).send(requestCaptor.capture(), any(HttpResponse.BodyHandlers.ofString().getClass()));
        assertTrue(requestCaptor.getValue().uri().toString().contains("/users/" + username + "/gists"), "Request URI should be for the rate-limited user");
    }

    /**
     * Test case for other generic HTTP errors (e.g., 500 Internal Server Error).
     */
    @Test
    void getUserGists_shouldThrowGenericExceptionOnOtherHttpErrors() throws Exception {
        // Arrange
        String username = "error_user";
        String errorBody = "{\"message\":\"Internal Server Error\"}";
        int statusCode = 500;

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(statusCode);
        when(mockHttpResponse.body()).thenReturn(errorBody); // This stubbing IS necessary here as the body is used in the exception message

        // Act & Assert: Verify that a generic Exception is thrown with a detailed message.
        Exception exception = assertThrows(Exception.class, () -> {
            gitHubApiClient.getUserGists(username);
        });

        assertEquals("GitHub API error: " + statusCode + " - " + errorBody, exception.getMessage(), "Exception message should contain status code and error body");
        verify(mockHttpClient, times(1)).send(requestCaptor.capture(), any(HttpResponse.BodyHandlers.ofString().getClass()));
        assertTrue(requestCaptor.getValue().uri().toString().contains("/users/" + username + "/gists"), "Request URI should be for the error user");
    }

    /**
     * Test case for network-related errors (e.g., IOException during send).
     */
    @Test
    void getUserGists_shouldThrowIOExceptionOnNetworkError() throws Exception {
        // Arrange
        String username = "network_error_user";
        String errorMessage = "Simulated network error";

        // Simulate an IOException being thrown during the HttpClient.send operation.
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenThrow(new IOException(errorMessage));

        // Act & Assert: Verify that IOException is re-thrown.
        Exception exception = assertThrows(IOException.class, () -> {
            gitHubApiClient.getUserGists(username);
        });

        assertEquals(errorMessage, exception.getMessage(), "Exception message should match the simulated network error");
        verify(mockHttpClient, times(1)).send(requestCaptor.capture(), any(HttpResponse.BodyHandlers.ofString().getClass()));
        assertTrue(requestCaptor.getValue().uri().toString().contains("/users/" + username + "/gists"), "Request URI should be for the user that caused network error");
    }

    /**
     * Test case for handling an empty list of Gists (valid JSON, empty array).
     */
    @Test
    void getUserGists_shouldHandleEmptyGistListGracefully() throws Exception {
        // Arrange
        String username = "userwithnogists";
        String jsonResponse = "[]"; // Valid empty JSON array

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse); // This stubbing IS necessary as the body is read for parsing

        // Act
        List<Gist> gists = gitHubApiClient.getUserGists(username);

        // Assert
        assertNotNull(gists, "Gists list should not be null even if empty");
        assertTrue(gists.isEmpty(), "Gists list should be empty");
        verify(mockHttpClient, times(1)).send(requestCaptor.capture(), any(HttpResponse.BodyHandlers.ofString().getClass()));
    }

    /**
     * Test case for handling malformed JSON response from the API.
     */
    @Test
    void getUserGists_shouldHandleMalformedJsonResponse() throws Exception {
        // Arrange
        String username = "malformedjsonuser";
        String malformedJsonResponse = "{not_a_valid_json"; // Invalid JSON string

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(malformedJsonResponse); // This stubbing IS necessary as the body is read for parsing

        // Act & Assert: Expect JsonSyntaxException from Gson when parsing malformed JSON.
        assertThrows(com.google.gson.JsonSyntaxException.class, () -> {
            gitHubApiClient.getUserGists(username);
        }, "Should throw JsonSyntaxException for malformed JSON");
        verify(mockHttpClient, times(1)).send(requestCaptor.capture(), any(HttpResponse.BodyHandlers.ofString().getClass()));
    }

    /**
     * Test case to verify the correct base URL is used.
     */
    @Test
    void getUserGists_shouldUseCorrectBaseUrl() throws Exception {
        // Arrange
        String username = "testuser";
        String jsonResponse = "[]";

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandlers.ofString().getClass())))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse); // This stubbing IS necessary as the body is read for parsing

        // Act
        gitHubApiClient.getUserGists(username);

        // Assert
        verify(mockHttpClient, times(1)).send(requestCaptor.capture(), any(HttpResponse.BodyHandlers.ofString().getClass()));
        HttpRequest sentRequest = requestCaptor.getValue();
        // Direct assertion on the start of the URI string
        assertTrue(sentRequest.uri().toString().startsWith("https://api.github.com/users/" + username + "/gists"),
                   "Request URI should start with the correct GitHub API base URL");
    }
}