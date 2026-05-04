package id.ac.ui.cs.advprog.jsonbackend.profile.model;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserProfileTest {
    @Test
    void testUserProfileCreation() {
        User user = User.builder().email("test@email.com").build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .username("budi")
                .fullName("Test User")
                .bio("test bio")
                .isVerifiedJastiper(false)
                .build();

        profile.onCreate();

        assertEquals("budi", profile.getUsername());
        assertEquals(0, profile.getSuccessfulTransaction());
        assertEquals(0.0, profile.getRating());

        profile.onUpdate();

        assertThat(profile.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());




    }
}
