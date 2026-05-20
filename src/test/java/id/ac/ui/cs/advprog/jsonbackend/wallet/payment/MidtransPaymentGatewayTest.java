package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MidtransPaymentGatewayTest {

    private HttpServer server;
    private AtomicReference<String> requestBody;
    private AtomicReference<String> authorizationHeader;

    @BeforeEach
    void setUp() throws IOException {
        requestBody = new AtomicReference<>();
        authorizationHeader = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/snap/v1/transactions", exchange -> {
            authorizationHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"token\":\"snap-token\",\"redirect_url\":\"https://pay.example/snap\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void createChargeShouldCallSnapApiWithBasicAuthAndTransactionDetails() {
        URI baseUri = URI.create("http://localhost:" + server.getAddress().getPort());
        MidtransPaymentGateway gateway = new MidtransPaymentGateway(
                "server-key",
                baseUri,
                HttpClient.newHttpClient(),
                new ObjectMapper()
        );

        PaymentGatewayChargeResponse response = gateway.createCharge(new PaymentGatewayChargeRequest(
                "WALLET-TOPUP-" + UUID.randomUUID(),
                new BigDecimal("50000"),
                UUID.randomUUID(),
                "Wallet Top-up"
        ));

        assertEquals("snap-token", response.token());
        assertEquals("https://pay.example/snap", response.redirectUrl());
        assertEquals(
                "Basic " + Base64.getEncoder().encodeToString("server-key:".getBytes(StandardCharsets.UTF_8)),
                authorizationHeader.get()
        );
        assertTrue(requestBody.get().contains("\"transaction_details\""));
        assertTrue(requestBody.get().contains("\"gross_amount\":50000"));
    }
}
