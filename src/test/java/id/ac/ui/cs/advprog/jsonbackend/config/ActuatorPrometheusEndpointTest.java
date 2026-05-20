package id.ac.ui.cs.advprog.jsonbackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ActuatorPrometheusEndpointTest {

    @LocalServerPort
    private int port;

    @Test
    void prometheusEndpointShouldBeReachable() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/actuator/prometheus");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("jvm_memory"));
        assertTrue(response.body().contains("http_server_requests"));
    }

    @Test
    void metricsEndpointShouldNotBeExposed() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/actuator/metrics");

        assertTrue(response.statusCode() == 401 || response.statusCode() == 403 || response.statusCode() == 404);
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
