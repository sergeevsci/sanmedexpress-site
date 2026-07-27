package ru.sanmedexpress.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminSecurityProperties(String username, String password) {
}
