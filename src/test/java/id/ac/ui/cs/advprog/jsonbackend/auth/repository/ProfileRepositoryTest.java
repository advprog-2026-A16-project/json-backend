package id.ac.ui.cs.advprog.jsonbackend.auth.repository;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;
    private Profile savedProfile;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("leon@email.com")
                .password("password123")
                .role(Role.TITIPERS)
                .build();
        savedUser = userRepository.save(user);

        Profile profile = Profile.builder()
                .user(savedUser)
                .username("leon")
                .fullName("Leon Scott Kennedy")
                .build();
        savedProfile = profileRepository.save(profile);
    }

    @Test
    void testExistsByUsername_WhenExists_ShouldReturnTrue() {
        boolean exists = profileRepository.existsByUsername("leon");
        assertTrue(exists);
    }

    @Test
    void testExistsByUsername_WhenNotExists_ShouldReturnFalse() {
        boolean exists = profileRepository.existsByUsername("grace");
        assertFalse(exists);
    }

    @Test
    void testFindByUserId_WhenExists_ShouldReturnProfile() {
        Optional<Profile> foundProfile = profileRepository.findByUserId(savedUser.getId());

        assertTrue(foundProfile.isPresent());
        assertEquals("leon", foundProfile.get().getUsername());
        assertEquals(savedUser.getId(), foundProfile.get().getUser().getId());
    }

    @Test
    void testFindByUserId_WhenNotExists_ShouldReturnEmpty() {
        java.util.UUID fakeId = java.util.UUID.randomUUID();
        Optional<Profile> foundProfile = profileRepository.findByUserId(fakeId);

        assertFalse(foundProfile.isPresent());
    }
}