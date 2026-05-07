package id.ac.ui.cs.advprog.jsonbackend.auth.event.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.enums.AuthEventType;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.AuthOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.UserRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InProcessAuthEventPublisher implements AuthEventPublisher {

    private static final Pattern USER_ID_PATTERN = Pattern.compile("\"userId\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ROLE_PATTERN = Pattern.compile("\"role\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ACCOUNT_STATUS_PATTERN = Pattern.compile("\"accountStatus\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PROFILE_ID_PATTERN = Pattern.compile("\"profileId\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("\"username\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern RATING_PATTERN = Pattern.compile("\"rating\"\\s*:\\s*([0-9.]+)");
    private static final Pattern TRANSACTIONS_PATTERN = Pattern.compile("\"successfulTransactions\"\\s*:\\s*([0-9]+)");

    private final ApplicationEventPublisher applicationEventPublisher;

    public InProcessAuthEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(AuthOutboxEvent event) {
        if (AuthEventType.USER_REGISTERED.name().equals(event.getEventType())) {
            UUID userId = extractUserId(event.getPayload());
            String email = extractEmail(event.getPayload());
            String role = extractRole(event.getPayload());
            String accountStatus = extractAccountStatus(event.getPayload());
            UUID profileId = extractProfileId(event.getPayload());
            String username = extractUsername(event.getPayload());
            double rating = extractRating(event.getPayload());
            int successfulTransactions = extractSuccessfulTransactions(event.getPayload());

            UserRegisteredEvent userRegisteredEvent = new UserRegisteredEvent(
                    event.getEventId(),
                    userId,
                    email,
                    role,
                    accountStatus,
                    profileId,
                    username,
                    rating,
                    successfulTransactions,
                    event.getCorrelationId()
            );

            applicationEventPublisher.publishEvent(userRegisteredEvent);
        } else {
            throw new RuntimeException("Unsupported auth event type: " + event.getEventType());
        }
    }

    private UUID extractUserId(String payload) {
        Matcher matcher = USER_ID_PATTERN.matcher(payload);
        if (!matcher.find()) throw new IllegalArgumentException("Missing userId");
        return UUID.fromString(matcher.group(1));
    }

    private String extractEmail(String payload) {
        Matcher matcher = EMAIL_PATTERN.matcher(payload);
        if (!matcher.find()) throw new IllegalArgumentException("Missing email");
        return matcher.group(1);
    }

    private String extractRole(String payload) {
        Matcher matcher = ROLE_PATTERN.matcher(payload);
        if (!matcher.find()) throw new IllegalArgumentException("Missing role");
        return matcher.group(1);
    }

    private String extractAccountStatus(String payload) {
        Matcher matcher = ACCOUNT_STATUS_PATTERN.matcher(payload);
        if (!matcher.find()) throw new IllegalArgumentException("Missing accountStatus");
        return matcher.group(1);
    }

    private UUID extractProfileId(String payload) {
        Matcher matcher = PROFILE_ID_PATTERN.matcher(payload);
        if (!matcher.find()) throw new IllegalArgumentException("Missing profileId");
        return UUID.fromString(matcher.group(1));
    }

    private String extractUsername(String payload) {
        Matcher matcher = USERNAME_PATTERN.matcher(payload);
        if (!matcher.find()) throw new IllegalArgumentException("Missing username");
        return matcher.group(1);
    }

    private double extractRating(String payload) {
        Matcher matcher = RATING_PATTERN.matcher(payload);
        if (!matcher.find()) throw new IllegalArgumentException("Missing rating");
        return Double.parseDouble(matcher.group(1));
    }

    private int extractSuccessfulTransactions(String payload) {
        Matcher matcher = TRANSACTIONS_PATTERN.matcher(payload);
        if (!matcher.find()) throw new IllegalArgumentException("Missing successfulTransactions");
        return Integer.parseInt(matcher.group(1));
    }
}