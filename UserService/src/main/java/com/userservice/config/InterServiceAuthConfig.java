
package com.userservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class InterServiceAuthConfig {

    @Bean
    public ClientHttpRequestInterceptor bearerTokenInterceptor() {
        return (request, body, execution) -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest incomingRequest =
                        attributes.getRequest();

                String authorization =
                        incomingRequest.getHeader("Authorization");

                if (authorization != null
                        && authorization.startsWith("Bearer ")) {
                    request.getHeaders().set(
                            "Authorization", authorization);
                }
            }

            return execution.execute(request, body);
        };
    }

    @Bean
    public RequestInterceptor feignBearerTokenInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                String authorization =
                        attributes.getRequest()
                                .getHeader("Authorization");

                if (authorization != null
                        && authorization.startsWith("Bearer ")) {
                    template.header("Authorization", authorization);
                }
            }
        };
    }
}
