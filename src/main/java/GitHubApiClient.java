// GitHubApiClient.java
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GitHubApiClient {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * Constructs a GitHubApiClient with specific HttpClient and Gson instances.
     * This constructor is primarily used for dependency injection and testing,
     * allowing mocking of external HTTP calls.
     *
     * @param httpClient The HttpClient instance to use for making HTTP requests.
     * @param gson The Gson instance to use for JSON serialization/deserialization.
     */
    public GitHubApiClient(HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    /**
     * Constructs a GitHubApiClient with default HttpClient and Gson instances.
     * This constructor is suitable for direct instantiation in application code
     * where mocking is not required (e.g., in GistServer).
     */
    public GitHubApiClient() {
        // Initialize with default HttpClient and Gson implementations
        this(HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build(), new Gson());
    }

    /**
     * Fetches a list of public Gists for a given GitHub username.
     *
     * @param username The GitHub username.
     * @return A List of Gist objects.
     * @throws IOException If a network-related or I/O error occurs.
     * @throws InterruptedException If the operation is interrupted.
     * @throws UserNotFoundException If the GitHub user is not found (HTTP 404).
     * @throws TooManyRequestsException If the GitHub API rate limit is exceeded (HTTP 429).
     * @throws Exception For any other unexpected HTTP API errors.
     */
    public List<Gist> getUserGists(String username) throws IOException, InterruptedException, UserNotFoundException, TooManyRequestsException, Exception {
        String url = GITHUB_API_BASE_URL + "/users/" + username + "/gists";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new UserNotFoundException("GitHub user not found: " + username);
        } else if (response.statusCode() == 429) {
            throw new TooManyRequestsException("GitHub API rate limit exceeded.");
        } else if (response.statusCode() != 200) {
            // Provide a more detailed error message including status and body
            throw new Exception("GitHub API error: " + response.statusCode() + " - " + response.body());
        }

        // Use TypeToken to correctly deserialize a List of Gist objects from JSON
        Type gistListType = new TypeToken<List<Gist>>(){}.getType();
        return gson.fromJson(response.body(), gistListType);
    }

    /**
     * Custom exception for when a GitHub user is not found (HTTP 404).
     */
    public static class UserNotFoundException extends Exception {
        public UserNotFoundException(String message) { super(message); }
    }

    /**
     * Custom exception for when the GitHub API rate limit is exceeded (HTTP 429).
     */
    public static class TooManyRequestsException extends Exception {
        public TooManyRequestsException(String message) { super(message); }
    }
}