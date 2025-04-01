package com.cooperativismo.votacao.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties( CallbackConfig.class )
public class AppConfig {
    
    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }
} 