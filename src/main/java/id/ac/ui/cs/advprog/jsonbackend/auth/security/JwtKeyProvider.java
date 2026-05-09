package id.ac.ui.cs.advprog.jsonbackend.auth.security;

import id.ac.ui.cs.advprog.jsonbackend.auth.config.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtKeyProvider {

    private final JwtProperties jwtProperties;

    public JwtKeyProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
