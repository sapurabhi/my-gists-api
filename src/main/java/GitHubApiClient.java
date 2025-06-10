 // GitHubApiClient.java
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class GitHubApiClient {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private final HttpClient httpClient;
    private final Gson gson;

    public GitHubApiClient() {
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        this.gson = new Gson();
    }

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
            throw new Exception("GitHub API error: " + response.statusCode() + " - " + response.body());
        }

        // Use TypeToken to correctly deserialize List of Gist objects
        Type gistListType = new TypeToken<List<Gist>>(){}.getType();
        return gson.fromJson(response.body(), gistListType);
    }

    // Custom Exceptions for better error handling
    public static class UserNotFoundException extends Exception {
        public UserNotFoundException(String message) { super(message); }
    }

    public static class TooManyRequestsException extends Exception {
        public TooManyRequestsException(String message) { super(message); }
    }
}