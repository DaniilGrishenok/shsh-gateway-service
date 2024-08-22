package com.shsh.api_gateway_social_network;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewaySocialNetworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewaySocialNetworkApplication.class, args);
	}

}
