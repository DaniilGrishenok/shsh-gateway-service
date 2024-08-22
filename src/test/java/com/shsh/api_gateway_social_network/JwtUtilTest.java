package com.shsh.api_gateway_social_network;

import com.shsh.api_gateway_social_network.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "42244938548A0C2C0D9C78995F3A3430C17AB5B206C4DBF1E477A08E47119A99";
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlVXNlciIsImVtYWlsIjoiZXhhbXBsZUBleGFtcGxlLmNvbSIsImp0aSI6ImZmNDcwMzg1LTZlZjYtNDU4ZS04ZmE4LTU3YWIwZDNiMWMyOCIsImlhdCI6MTcyMDI5NDIyMCwiZXhwIjoxNzIwMzEyMjIwfQ.BnI03CSmpR1NNEP_fJQIMbVZwsmQsBbny6TVRdStZM0";
    private static final String EXPIRED_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlVXNlciIsImVtYWlsIjoiZXhhbXBsZUBleGFtcGxlLmNvbSIsImp0aSI6ImZmNDcwMzg1LTZlZjYtNDU4ZS04ZmE4LTU3YWIwZDNiMWMyOCIsImlhdCI6MTYwOTI5NDIyMCwiZXhwIjoxNjA5MzEyMjIwfQ.BnI03CSmpR1NNEP_fJQIMbVZwsmQsBbny6TVRdStZM0";

    @Test
    public void testValidToken() {
        jwtUtil.jwtSecret = secret;
        assertTrue(jwtUtil.validateToken(VALID_TOKEN));
    }

    @Test
    public void testExpiredToken() {
        jwtUtil.jwtSecret = secret;
        assertFalse(jwtUtil.validateToken(EXPIRED_TOKEN));
    }
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtil = new JwtUtil();
        jwtUtil.jwtSecret = secret;
    }

    @Test
    void testExtractAllClaims() {
        String token = Jwts.builder()
                .setSubject("user1")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals("user1", claims.getSubject());
    }

    @Test
    void testValidateToken() {
        String token = Jwts.builder()
                .setSubject("user1")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateExpiredToken() {
        String token = Jwts.builder()
                .setSubject("user1")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 10)) // 10 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 5)) // 5 hours ago
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        boolean isValid = jwtUtil.validateToken(token);
        assertFalse(isValid);
    }

}