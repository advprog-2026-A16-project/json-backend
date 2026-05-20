package id.ac.ui.cs.advprog.jsonbackend.auth.model;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ProfileTest {
    @Test
    void testUserProfileCreation() {
        User user = User.builder().email("test@email.com").build();

        Profile profile = Profile.builder()
                .user(user)
                .username("budi")
                .fullName("Budi Budiman")
                .bio("ini bio")
                .build();

        profile.onCreate();

        assertEquals("budi", profile.getUsername());
        assertEquals("Budi Budiman", profile.getFullName());
        assertEquals("ini bio", profile.getBio());
        assertEquals(0, profile.getSuccessfulTransactions());
        assertEquals(0.0, profile.getRating());

        assertNotNull(profile.getCreatedAt());
        assertNotNull(profile.getUpdatedAt());
        assertEquals(profile.getCreatedAt(), profile.getUpdatedAt());

        LocalDateTime beforeUpdate = profile.getUpdatedAt();

        profile.onUpdate();

        assertThat(profile.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    void successfulTransactions_ShouldMapToCanonicalDatabaseColumn() throws NoSuchFieldException {
        Field field = Profile.class.getDeclaredField("successfulTransactions");
        Column column = field.getAnnotation(Column.class);

        assertNotNull(column);
        assertEquals("successful_transactions", column.name());
        assertFalse(column.nullable());
    }
}
