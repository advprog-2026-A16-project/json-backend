package id.ac.ui.cs.advprog.jsonbackend.auth.event;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthEventPayloadFactoryTest {

    private User user;
    private Profile profile;

    @BeforeEach
    void setUp() {
        user = new User(
                "test@example.com",
                "encodedPassword",
                Role.TITIPERS
        );

        user.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        user.setAccountStatus(AccountStatus.ACTIVE);

        profile = Profile.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .username("testuser")
                .rating(4.5)
                .successfulTransactions(10)
                .build();
    }

    @Test
    void userRegisteredPayload_ShouldReturnCorrectJsonPayload() {
        String expectedPayload = """
            {"userId":"11111111-1111-1111-1111-111111111111",\
            "email":"test@example.com",\
            "role":"TITIPERS",\
            "accountStatus":"ACTIVE",\
            "profileId":"22222222-2222-2222-2222-222222222222",\
            "username":"testuser",\
            "rating":4.500000,\
            "successfulTransactions":10}""";

        String actualPayload =
                AuthEventPayloadFactory.userRegisteredPayload(user, profile);

        assertEquals(expectedPayload, actualPayload);
    }
}