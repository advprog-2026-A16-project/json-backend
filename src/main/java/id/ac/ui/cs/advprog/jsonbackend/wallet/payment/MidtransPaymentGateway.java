package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class MidtransPaymentGateway implements PaymentGateway {
    private static final String SNAP_TRANSACTIONS_PATH = "/snap/v1/transactions";

    private final String serverKey;
    private final URI snapBaseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public MidtransPaymentGateway(
            @Value("${midtrans.server-key:}") String serverKey,
            @Value("${midtrans.snap.base-url:https://app.sandbox.midtrans.com}") String snapBaseUrl
    ) {
        this(serverKey, URI.create(snapBaseUrl), HttpClient.newHttpClient(), new ObjectMapper());
    }

    public MidtransPaymentGateway(String serverKey,
                                  URI snapBaseUri,
                                  HttpClient httpClient,
                                  ObjectMapper objectMapper) {
        this.serverKey = serverKey;
        this.snapBaseUri = snapBaseUri;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentGatewayChargeResponse createCharge(PaymentGatewayChargeRequest request) {
        if (serverKey == null || serverKey.isBlank()) {
            throw new PaymentGatewayException("Midtrans server key is not configured");
        }

        String payload = buildPayload(request);
        HttpRequest httpRequest = HttpRequest.newBuilder(snapBaseUri.resolve(SNAP_TRANSACTIONS_PATH))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", basicAuthHeader())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new PaymentGatewayException("Midtrans Snap request failed with status "
                        + response.statusCode() + ": " + response.body());
            }
            return parseResponse(response.body());
        } catch (IOException e) {
            throw new PaymentGatewayException("Failed to call Midtrans Snap API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayException("Interrupted while calling Midtrans Snap API", e);
        }
    }

    private String buildPayload(PaymentGatewayChargeRequest request) {
        long amount = toMidtransAmount(request.amount());

        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode transactionDetails = root.putObject("transaction_details");
        transactionDetails.put("order_id", request.orderId());
        transactionDetails.put("gross_amount", amount);

        ObjectNode item = root.putArray("item_details").addObject();
        item.put("id", "wallet-topup");
        item.put("price", amount);
        item.put("quantity", 1);
        item.put("name", request.description());

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new PaymentGatewayException("Failed to serialize Midtrans Snap payload", e);
        }
    }

    private PaymentGatewayChargeResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return new PaymentGatewayChargeResponse(
                    requiredText(root, "token"),
                    requiredText(root, "redirect_url")
            );
        } catch (JsonProcessingException e) {
            throw new PaymentGatewayException("Invalid Midtrans Snap response", e);
        }
    }

    private String requiredText(JsonNode root, String fieldName) {
        JsonNode value = root.get(fieldName);
        if (value == null || value.isNull() || !value.isTextual() || value.asText().isBlank()) {
            throw new PaymentGatewayException("Missing Midtrans response field: " + fieldName);
        }
        return value.asText();
    }

    private long toMidtransAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        try {
            return amount.setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Midtrans gross amount must be a whole number", e);
        }
    }

    private String basicAuthHeader() {
        String token = Base64.getEncoder().encodeToString((serverKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
