package id.ac.ui.cs.advprog.jsonbackend.profile.repository;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;
    private UserProfile savedProfile;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("leon@email.com")
                .password("password123")
                .role(Role.TITIPERS)
                .build();
        savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .username("leon")
                .fullName("Leon Scott Kennedy")
                .build();
        savedProfile = userProfileRepository.save(profile);
    }

    @Test
    void testExistsByUsername_WhenExists_ShouldReturnTrue() {
        boolean exists = userProfileRepository.existsByUsername("leon");
        assertTrue(exists);
    }

    @Test
    void testExistsByUsername_WhenNotExists_ShouldReturnFalse() {
        boolean exists = userProfileRepository.existsByUsername("grace");
        assertFalse(exists);
    }

    @Test
    void testFindByUserId_WhenExists_ShouldReturnProfile() {
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(savedUser.getId());

        assertTrue(foundProfile.isPresent());
        assertEquals("leon", foundProfile.get().getUsername());
        assertEquals(savedUser.getId(), foundProfile.get().getUser().getId());
    }

    @Test
    void testFindByUserId_WhenNotExists_ShouldReturnEmpty() {
        java.util.UUID fakeId = java.util.UUID.randomUUID();
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(fakeId);

        assertFalse(foundProfile.isPresent());
    }
}