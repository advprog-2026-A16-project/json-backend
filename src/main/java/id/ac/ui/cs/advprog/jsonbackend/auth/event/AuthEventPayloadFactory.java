package id.ac.ui.cs.advprog.jsonbackend.auth.event;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;

public class AuthEventPayloadFactory {

    public static String userRegisteredPayload(User user, Profile profile) {
        return String.format(
                "{\"userId\":\"%s\"," +
                        "\"email\":\"%s\"," +
                        "\"role\":\"%s\"," +
                        "\"accountStatus\":\"%s\"," +
                        "\"profileId\":\"%s\"," +
                        "\"username\":\"%s\"," +
                        "\"rating\":%f," +
                        "\"successfulTransactions\":%d}",
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getAccountStatus().name(),
                profile.getId(),
                profile.getUsername(),
                profile.getRating(),
                profile.getSuccessfulTransactions()
        );
    }
}