package id.ac.ui.cs.advprog.jsonbackend.auth.event;

import java.util.UUID;

public final class AuthEventPayloadFactory {
    private AuthEventPayloadFactory() {
    }

    public static String userRegisteredPayload(
            UUID userId,
            String email,
            String role,
            String accountStatus
    ) {
        return "{\"userId\":\"" + userId +
                "\",\"email\":\"" + email +
                "\",\"role\":\"" + role +
                "\",\"accountStatus\":\"" + accountStatus +
                "\"}";
    }
}