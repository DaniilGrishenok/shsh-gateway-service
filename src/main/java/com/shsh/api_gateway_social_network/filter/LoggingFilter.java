package com.shsh.api_gateway_social_network.filter;


import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {
    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            exchange.getRequest().getHeaders().forEach((name, values) -> {
                values.forEach(value -> {
                    System.out.println(name + ": " + value);
                });
            });
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Configuration properties if any
    }
}