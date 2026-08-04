package ru.sanmedexpress.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.sanmedexpress.config.TelegramProperties;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;

@Component
public class TelegramJdkStartupCheck implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(TelegramJdkStartupCheck.class);
    private static final String TELEGRAM_HOST = "api.telegram.org";
    private static final String TELEGRAM_BASE_URL = "https://api.telegram.org";

    private final TelegramProperties properties;

    public TelegramJdkStartupCheck(TelegramProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            log.info("Telegram JDK startup check skipped because TELEGRAM_ENABLED=false");
            return;
        }

        var url = buildUrl();
        logResolvedIps();

        try {
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            var request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Telegram JDK startup check succeeded url={} status={} response={}", url, response.statusCode(), response.body());
        } catch (Exception exception) {
            log.error("Telegram JDK startup check failed url={} error={} cause={}", url, exception.getMessage(), exception.getCause(), exception);
        }
    }

    private String buildUrl() {
        if (properties.botToken() == null || properties.botToken().isBlank()) {
            return TELEGRAM_BASE_URL;
        }
        return TELEGRAM_BASE_URL + "/bot" + properties.botToken() + "/getMe";
    }

    private void logResolvedIps() {
        try {
            var addresses = Arrays.stream(InetAddress.getAllByName(TELEGRAM_HOST))
                    .map(InetAddress::getHostAddress)
                    .toList();
            log.info("Telegram JDK startup check resolved host={} ips={}", TELEGRAM_HOST, addresses);
        } catch (Exception exception) {
            log.error("Telegram JDK startup check cannot resolve host={} error={} cause={}", TELEGRAM_HOST, exception.getMessage(), exception.getCause(), exception);
        }
    }
}
