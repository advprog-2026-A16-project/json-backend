package id.ac.ui.cs.advprog.jsonbackend.auth.event.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.enums.AuthEventType;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.AuthOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InProcessAuthEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private InProcessAuthEventPublisher publisher;

    @Test
    void publishSuccessForUserRegisteredEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        String validPayload = String.format(
                "{\"userId\":\"%s\",\"email\":\"test@email.com\",\"role\":\"TITIPERS\",\"accountStatus\":\"ACTIVE\",\"profileId\":\"%s\",\"username\":\"test\",\"rating\":4.5,\"successfulTransactions\":10}",
                userId, profileId
        );

        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventId(eventId);
        outboxEvent.setEventType(AuthEventType.USER_REGISTERED.name());
        outboxEvent.setPayload(validPayload);
        outboxEvent.setCorrelationId("corr-id-123");

        publisher.publish(outboxEvent);

        ArgumentCaptor<UserRegisteredEvent> captor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());

        UserRegisteredEvent capturedEvent = captor.getValue();
        assertEquals(eventId, capturedEvent.eventId());
        assertEquals(userId, capturedEvent.userId());
        assertEquals("test@email.com", capturedEvent.email());
        assertEquals("TITIPERS", capturedEvent.role());
        assertEquals("ACTIVE", capturedEvent.accountStatus());
        assertEquals(profileId, capturedEvent.profileId());
        assertEquals("test", capturedEvent.username());
        assertEquals(4.5, capturedEvent.rating());
        assertEquals(10, capturedEvent.successfulTransactions());
        assertEquals("corr-id-123", capturedEvent.correlationId());
    }

    @Test
    void publishThrowsExceptionForUnsupportedEventType() {
        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventType("UNSUPPORTED_EVENT_TYPE");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> publisher.publish(outboxEvent));

        assertEquals("Unsupported auth event type: UNSUPPORTED_EVENT_TYPE", exception.getMessage());

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void publishThrowsExceptionWhenPayloadMissingUserId() {
        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventType(AuthEventType.USER_REGISTERED.name());
        outboxEvent.setPayload("{\"email\":\"test@email.com\",\"role\":\"TITIPERS\",\"accountStatus\":\"ACTIVE\",\"profileId\":\"" + UUID.randomUUID() + "\",\"username\":\"test\",\"rating\":4.5,\"successfulTransactions\":10}");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> publisher.publish(outboxEvent));
        assertEquals("Missing userId", exception.getMessage());
    }

    @Test
    void publishThrowsExceptionWhenPayloadMissingEmail() {
        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventType(AuthEventType.USER_REGISTERED.name());
        outboxEvent.setPayload("{\"userId\":\"" + UUID.randomUUID() + "\",\"role\":\"TITIPERS\",\"accountStatus\":\"ACTIVE\",\"profileId\":\"" + UUID.randomUUID() + "\",\"username\":\"test\",\"rating\":4.5,\"successfulTransactions\":10}");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> publisher.publish(outboxEvent));
        assertEquals("Missing email", exception.getMessage());
    }

    @Test
    void publishThrowsExceptionWhenPayloadMissingRole() {
        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventType(AuthEventType.USER_REGISTERED.name());
        outboxEvent.setPayload("{\"userId\":\"" + UUID.randomUUID() + "\",\"email\":\"test@email.com\",\"accountStatus\":\"ACTIVE\",\"profileId\":\"" + UUID.randomUUID() + "\",\"username\":\"test\",\"rating\":4.5,\"successfulTransactions\":10}");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> publisher.publish(outboxEvent));
        assertEquals("Missing role", exception.getMessage());
    }

    @Test
    void publishThrowsExceptionWhenPayloadMissingAccountStatus() {
        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventType(AuthEventType.USER_REGISTERED.name());
        outboxEvent.setPayload("{\"userId\":\"" + UUID.randomUUID() + "\",\"email\":\"test@email.com\",\"role\":\"TITIPERS\",\"profileId\":\"" + UUID.randomUUID() + "\",\"username\":\"test\",\"rating\":4.5,\"successfulTransactions\":10}");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> publisher.publish(outboxEvent));
        assertEquals("Missing accountStatus", exception.getMessage());
    }

    @Test
    void publishThrowsExceptionWhenPayloadMissingProfileId() {
        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventType(AuthEventType.USER_REGISTERED.name());
        outboxEvent.setPayload("{\"userId\":\"" + UUID.randomUUID() + "\",\"email\":\"test@email.com\",\"role\":\"TITIPERS\",\"accountStatus\":\"ACTIVE\",\"username\":\"test\",\"rating\":4.5,\"successfulTransactions\":10}");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> publisher.publish(outboxEvent));
        assertEquals("Missing profileId", exception.getMessage());
    }

    @Test
    void publishThrowsExceptionWhenPayloadMissingUsername() {
        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventType(AuthEventType.USER_REGISTERED.name());
        outboxEvent.setPayload("{\"userId\":\"" + UUID.randomUUID() + "\",\"email\":\"test@email.com\",\"role\":\"TITIPERS\",\"accountStatus\":\"ACTIVE\",\"profileId\":\"" + UUID.randomUUID() + "\",\"rating\":4.5,\"successfulTransactions\":10}");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> publisher.publish(outboxEvent));
        assertEquals("Missing username", exception.getMessage());
    }

    @Test
    void publishThrowsExceptionWhenPayloadMissingRating() {
        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventType(AuthEventType.USER_REGISTERED.name());
        outboxEvent.setPayload("{\"userId\":\"" + UUID.randomUUID() + "\",\"email\":\"test@email.com\",\"role\":\"TITIPERS\",\"accountStatus\":\"ACTIVE\",\"profileId\":\"" + UUID.randomUUID() + "\",\"username\":\"test\",\"successfulTransactions\":10}");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> publisher.publish(outboxEvent));
        assertEquals("Missing rating", exception.getMessage());
    }

    @Test
    void publishThrowsExceptionWhenPayloadMissingSuccessfulTransactions() {
        AuthOutboxEvent outboxEvent = new AuthOutboxEvent();
        outboxEvent.setEventType(AuthEventType.USER_REGISTERED.name());
        outboxEvent.setPayload("{\"userId\":\"" + UUID.randomUUID() + "\",\"email\":\"test@email.com\",\"role\":\"TITIPERS\",\"accountStatus\":\"ACTIVE\",\"profileId\":\"" + UUID.randomUUID() + "\",\"username\":\"test\",\"rating\":4.5}");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> publisher.publish(outboxEvent));
        assertEquals("Missing successfulTransactions", exception.getMessage());
    }
}