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
public class JwtAuthWsFilter extends AbstractGatewayFilterFactory<JwtAuthWsFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthWsFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthWsFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
        // Configuration properties, if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getQueryParams().getFirst("token");  // Получаем токен из параметра запроса
            logger.debug("JWT Token from query parameter: {}", token);

            if (token == null || !jwtUtil.validateToken(token)) {
                logger.warn("Invalid or missing JWT token in query parameter");
                return handleUnauthorized(exchange);
            }

            Claims claims = jwtUtil.extractAllClaims(token);
            String userId = claims.getId();
            logger.debug("Token is valid. UserId: {}", userId);

            // Мутируем запрос для добавления X-User-Id и Authorization
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)  // Добавляем заголовок Authorization
                    .build();

            // Выводим все заголовки запроса в лог для проверки
            mutatedRequest.getHeaders().forEach((key, value) -> logger.info("Header '{}' : {}", key, value));

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

