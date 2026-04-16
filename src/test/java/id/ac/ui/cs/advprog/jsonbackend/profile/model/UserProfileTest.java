package id.ac.ui.cs.advprog.jsonbackend.profile.model;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserProfileTest {
    @Test
    void testUserProfileCreation() {
        User user = User.builder().email("test@email.com").build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .username("testuser")
                .fullName("Test User")
                .build();

        assertEquals("testuser", profile.getUsername());
        assertEquals(0, profile.getSuccessfulTransaction());
        assertEquals(0.0, profile.getRating());
    }
}
