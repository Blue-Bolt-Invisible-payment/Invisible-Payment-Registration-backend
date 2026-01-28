package com.cognizant.smartpay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS Configuration for allowing frontend requests.
 * Default values are provided after the ':' to prevent application crashes.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. Set Allowed Origins
        // split(",") handles multiple origins if provided in properties
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // 2. Set Allowed Methods
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));

        // 3. Set Allowed Headers
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // 4. Set Credentials
        // Note: If origins is "*", allowCredentials must be false in some Spring versions.
        // If testing on localhost, this is usually fine.
        config.setAllowCredentials(true);

        // 5. Max age for browser caching of the CORS pre-flight request
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}