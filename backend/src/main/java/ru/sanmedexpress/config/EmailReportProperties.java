package ru.sanmedexpress.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public record EmailReportProperties(boolean enabled, String to, String from) {
}
