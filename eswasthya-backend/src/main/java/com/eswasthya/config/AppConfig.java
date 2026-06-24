package com.eswasthya.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * General application beans.
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate used by {@link com.eswasthya.service.ExternalHealthApiService}
     * to call external health-reference APIs.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
