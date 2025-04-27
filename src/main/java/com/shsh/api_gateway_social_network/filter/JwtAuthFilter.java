package com.shsh.api_gateway_social_network.filter;

import com.shsh.api_gateway_social_network.config.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
        // Configuration properties, if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            logger.debug("Authorization Header: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization Header");
                return handleUnauthorized(exchange);
            }

            String token = authHeader.substring(7);
            logger.debug("JWT Token: {}", token);

            if (!jwtUtil.validateToken(token)) {
                logger.warn("Invalid JWT token");
                return handleUnauthorized(exchange);
            }

            Claims claims = jwtUtil.extractAllClaims(token);
            String userId = claims.getSubject();
            logger.debug("Token is valid. UserId: {}", userId);

            // Мутируем запрос для добавления X-User-Id
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)  // Добавляем заголовок Authorization
                    .build();

            // Создание нового ServerWebExchange с мутированным запросом
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            return chain.filter(mutatedExchange);
        };
    }


    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
