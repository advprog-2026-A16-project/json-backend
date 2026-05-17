package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.config.JwtProperties;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.security.JwtKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private static final String SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long   EXPIRY_MS = 1000L * 60 * 60 * 24;

    private JwtServiceImpl jwtService;
    private User           user;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpirationMs(EXPIRY_MS);

        JwtKeyProvider keyProvider = new JwtKeyProvider(props);
        jwtService = new JwtServiceImpl(keyProvider, props);

        user = new User("test@email.com", "password", Role.TITIPERS);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        assertNotNull(jwtService.generateToken(user));
    }

    @Test
    void extractUsername_shouldReturnSubjectFromGeneratedToken() {
        String token = jwtService.generateToken(user);
        assertEquals(user.getUsername(), jwtService.extractUsername(token));
    }

    @Test
    void generateToken_withExtraClaims_shouldEmbedClaimsInToken() {
        Map<String, Object> extraClaims = Map.of("role", "ADMIN");
        String token = jwtService.generateToken(user, extraClaims);

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("ADMIN", role);
    }

    @Test
    void extractClaim_shouldReturnIssuedAtDate() {
        String token = jwtService.generateToken(user);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        assertNotNull(issuedAt);
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidToken() {
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_shouldReturnFalse_forDifferentUser() {
        String token = jwtService.generateToken(user);
        User differentUser = new User("other@email.com", "password", Role.TITIPERS);
        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test
    void isTokenValid_shouldReturnFalse_forExpiredToken() {
        String expiredToken = buildExpiredToken(user.getUsername());
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(expiredToken, user));
    }

    @Test
    void extractUsername_fromExpiredToken_shouldThrowExpiredJwtException() {
        String expiredToken = buildExpiredToken(user.getUsername());
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));
    }

    @Test
    void extractUsername_fromMalformedToken_shouldThrowJwtException() {
        assertThrows(Exception.class, () -> jwtService.extractUsername("not.a.valid.jwt"));
    }

    @Test
    void extractUsername_fromTamperedToken_shouldThrowSignatureException() {
        String token = jwtService.generateToken(user);
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "TAMPERED." + parts[2];
        assertThrows(Exception.class, () -> jwtService.extractUsername(tampered));
    }

    private String buildExpiredToken(String subject) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}