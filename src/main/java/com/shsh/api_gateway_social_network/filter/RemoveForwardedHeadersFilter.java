package com.shsh.api_gateway_social_network.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RemoveForwardedHeadersFilter extends AbstractGatewayFilterFactory<RemoveForwardedHeadersFilter.Config> {
    public RemoveForwardedHeadersFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            exchange.getResponse().getHeaders().remove("Forwarded");
        }));
    }

    public static class Config {
        // Configuration properties if any
    }
}
