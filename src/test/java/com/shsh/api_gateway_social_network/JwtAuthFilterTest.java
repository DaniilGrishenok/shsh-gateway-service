package com.shsh.api_gateway_social_network;


import com.shsh.api_gateway_social_network.config.JwtUtil;
import com.shsh.api_gateway_social_network.filter.JwtAuthFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtAuthFilter jwtAuthFilter;
    private JwtUtil jwtUtil;
    private String secret = "42244938548A0C2C0D9C78995F3A3430C17AB5B206C4DBF1E477A08E47119A99";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtil = new JwtUtil();
        jwtUtil.jwtSecret = secret;
        jwtAuthFilter = new JwtAuthFilter(jwtUtil);
    }

    @Test
    void testValidToken() {
        String token = Jwts.builder()
                .setSubject("exampleUser")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthFilter.apply(new JwtAuthFilter.Config()).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalidToken";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        Mono<Void> result = jwtAuthFilter.apply(new JwtAuthFilter.Config()).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, times(0)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testExpiredToken() {
        String token = Jwts.builder()
                .setSubject("exampleUser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        Mono<Void> result = jwtAuthFilter.apply(new JwtAuthFilter.Config()).filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, times(0)).filter(any(ServerWebExchange.class));
    }
}
