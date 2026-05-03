package id.ac.ui.cs.advprog.jsonbackend.auth.event;

import java.util.UUID;

public final class AuthEventPayloadFactory {

    private AuthEventPayloadFactory() {
    }

    public static String emailPayload(UUID userId, String email) {
        return "{\"userId\":\"" + userId + "\",\"email\":" + email + "}";
    }
}