package com.example.event_booking.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final Key key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret:default-insecure-secret-do-not-use-in-prod}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs) {

        byte[] keyBytes = ensureKeyBytes(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Generate token with only subject (backwards-compatible).
     * Kept for any callers that still pass only username.
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate token for username and role.
     * Adds "role" claim and a "roles" array for client compatibility.
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        if (role != null) {
            claims.put("role", role);
            claims.put("roles", new String[]{ role });
        }

        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate token and return the username (subject). Returns null if invalid.
     */
    public String validateAndGetUsername(String token) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return jws.getBody().getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private static byte[] ensureKeyBytes(String secret) {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) return bytes;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(bytes);
        } catch (Exception ex) {
            byte[] out = new byte[32];
            for (int i = 0; i < out.length; i++) out[i] = bytes.length == 0 ? (byte) 0 : bytes[i % bytes.length];
            return out;
        
        }
    }
}