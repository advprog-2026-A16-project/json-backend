package id.ac.ui.cs.advprog.jsonbackend.auth.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void testUserConstructorAndGetters() {
        User user = new User("test@example.com", "password123", Role.TITIPERS);

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getRole()).isEqualTo(Role.TITIPERS);
    }

    @Test
    void testGetAuthorities() {
        User user = new User("user@example.com", "pwd", Role.TITIPERS);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority())
                .isEqualTo("ROLE_TITIPERS");
    }

    @Test
    void testGetUsernameReturnsEmail() {
        User user = new User("hello@world.com", "123", Role.TITIPERS);
        assertThat(user.getUsername()).isEqualTo("hello@world.com");
    }

    @Test
    void testBooleanMethods() {
        User user = new User();

        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void testSetters() {
        User user = new User();
        user.setEmail("new@email.com");
        user.setPassword("newpass");
        user.setRole(Role.TITIPERS);

        assertThat(user.getEmail()).isEqualTo("new@email.com");
        assertThat(user.getPassword()).isEqualTo("newpass");
        assertThat(user.getRole()).isEqualTo(Role.TITIPERS);
    }
}