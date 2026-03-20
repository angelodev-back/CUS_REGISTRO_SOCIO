package com.idat.pe.Cus_Registro_Postulante.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración para beans de la aplicación
 * Aplica SOLID: Separation of Concerns
 */
@Configuration
public class AppConfig {
    
    /**
     * Bean para RestTemplate que permite consumir APIs HTTP
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
    
    /**
     * Bean para ObjectMapper usado en mapeo de JSON
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    // PasswordEncoder está definido en SecurityConfig. No duplicar aquí.
}

