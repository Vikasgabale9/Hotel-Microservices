
package com.userservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class InterServiceAuthConfig {

    @Bean
    public RequestInterceptor feignBearerTokenInterceptor() {
        return template -> {
            var attributes = RequestContextHolder.getRequestAttributes();

            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                HttpServletRequest request = servletAttributes.getRequest();
                String authorization = request.getHeader("Authorization");

                if (authorization != null
                        && authorization.startsWith("Bearer ")) {
                    template.header("Authorization", authorization);
                }
            }
        };
    }
}
