package com.sdt.feedback.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SupabaseStorageProperties.class)
public class SupabaseStorageConfig {

    @Bean
    RestClient supabaseStorageRestClient() {
        return RestClient.builder().build();
    }
}
