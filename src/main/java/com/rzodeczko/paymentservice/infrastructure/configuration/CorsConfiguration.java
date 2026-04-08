package com.rzodeczko.paymentservice.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS configuration dedicated to Swagger UI clients used in local development.
 */
@Configuration
@ConditionalOnSwaggerUiEnabled
public class CorsConfiguration implements WebMvcConfigurer {

    @Value("${swagger-ui-port}")
    private String swaggerUiPort;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:" + swaggerUiPort)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
