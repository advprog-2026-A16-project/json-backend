package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.config.JwtProperties;
import id.ac.ui.cs.advprog.jsonbackend.auth.security.JwtKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private final JwtKeyProvider keyProvider;
    private final JwtProperties jwtProperties;

    public JwtServiceImpl(JwtKeyProvider keyProvider, JwtProperties jwtProperties) {
        this.keyProvider    = keyProvider;
        this.jwtProperties  = jwtProperties;
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, Collections.emptyMap());
    }

    @Override
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtProperties.getExpirationMs()))
                .signWith(keyProvider.getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keyProvider.getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
