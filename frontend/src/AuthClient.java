import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import javax.swing.SwingUtilities;

public class AuthClient {
    private static final String LOGIN_URL = "http://localhost:8080/login";
    public static String sessionCookie = null;

    public static void login(String username, String password, Runnable onSuccess, Runnable onFailure) {
        try {
            String requestBody = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LOGIN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {

                            var headers = response.headers();
                            var cookies = headers.map().get("set-cookie");
                            if (cookies != null && !cookies.isEmpty()) {
                                sessionCookie = cookies.get(0).split(";")[0];
                            }
                            SwingUtilities.invokeLater(onSuccess);
                        } else {
                            SwingUtilities.invokeLater(onFailure);
                        }
                    })
                    .exceptionally(ex -> {
                        SwingUtilities.invokeLater(onFailure);
                        return null;
                    });
        } catch (Exception e) {
            SwingUtilities.invokeLater(onFailure);
        }
    }

}
