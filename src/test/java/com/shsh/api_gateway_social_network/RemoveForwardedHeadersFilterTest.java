package com.shsh.api_gateway_social_network;

import com.shsh.api_gateway_social_network.filter.RemoveForwardedHeadersFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

public class RemoveForwardedHeadersFilterTest {

    private RemoveForwardedHeadersFilter filter;
    private GatewayFilterChain filterChain;

    @BeforeEach
    public void setUp() {
        filter = new RemoveForwardedHeadersFilter();
        filterChain = mock(GatewayFilterChain.class);
    }



    @Test
    public void testNoForwardedHeaders() {
        ServerHttpRequest request = MockServerHttpRequest.get("http://localhost/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from((MockServerHttpRequest) request);

        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = filter.apply(new RemoveForwardedHeadersFilter.Config()).filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpRequest mutatedRequest = exchange.getRequest();
        HttpHeaders headers = mutatedRequest.getHeaders();

        assert headers.isEmpty();
    }
}
