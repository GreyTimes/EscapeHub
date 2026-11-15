package com.escapehub.escapehub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@Profile("dev")
public class DevelopmentCorsConfig {

    @Bean
    public WebMvcConfigurer developmentCorsConfigurer(
            @Value("${app.cors.allowed-origin}") String allowedOrigin) {

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigin)
                        .allowedMethods(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",
                                "DELETE",
                                "OPTIONS"
                        )
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
}
