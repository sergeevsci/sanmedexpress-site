package ru.sanmedexpress.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class TelegramClientConfig {
    @Bean
    RestClient telegramRestClient(RestClient.Builder builder) {
        var settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(10));
        return builder
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .baseUrl("https://api.telegram.org")
                .build();
    }
}
