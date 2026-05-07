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

            UserRegisteredEvent userRegisteredEvent = new UserRegisteredEvent(
                    event.getEventId(),
                    userId,
                    email,
                    role,
                    accountStatus,
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
}