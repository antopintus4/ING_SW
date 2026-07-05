import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestPost {
    public static void main(String[] args) throws Exception {
        String json = Files.readString(Path.of("test_registration.json"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/registration/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("STATUS: " + response.statusCode());
        System.out.println("BODY: " + response.body());
    }
}
