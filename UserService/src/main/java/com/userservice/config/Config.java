
package com.userservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class Config {

    @Bean
    @LoadBalanced
    public RestTemplate getTemplate(
            ClientHttpRequestInterceptor bearerTokenInterceptor) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
                List.of(bearerTokenInterceptor));

        return restTemplate;
    }
}
