package com.goody.nus.se.gomoku.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for Spring Cloud Gateway
 *
 * @author Goody
 * @version 1.0, 2025/10/18
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (e.g., cookies, Authorization header)
        config.setAllowCredentials(true);

        // Allowed origins (specify exact domain in production)
        config.addAllowedOriginPattern("*");

        // Allowed HTTP methods
        config.addAllowedMethod("*");

        // Allowed request headers
        config.addAllowedHeader("*");

        // Exposed response headers (accessible by frontend)
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
