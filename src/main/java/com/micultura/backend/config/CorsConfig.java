package com.micultura.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * Comma-separated list of allowed origins. Accepts exact origins
     * ("https://micultura.com") and wildcard patterns ("https://*.micultura.com").
     * Override in production with APP_CORS_ALLOWED_ORIGINS.
     */
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    /**
     * Exposed as a CorsConfigurationSource (not a CorsFilter) so SecurityConfig's
     * .cors(Customizer.withDefaults()) picks it up. With a standalone CorsFilter,
     * Spring Security still rejects OPTIONS preflights for non-permitAll paths
     * before the filter runs, breaking any authenticated cross-origin request.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        // setAllowedOriginPatterns supports both exact origins and wildcards,
        // and is required when allowCredentials=true (which forbids "*").
        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
