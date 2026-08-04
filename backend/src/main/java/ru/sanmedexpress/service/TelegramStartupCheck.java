package ru.sanmedexpress.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.sanmedexpress.config.TelegramProperties;

@Component
public class TelegramStartupCheck implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(TelegramStartupCheck.class);
    private static final String TELEGRAM_BASE_URL = "https://api.telegram.org";

    private final RestClient telegramRestClient;
    private final TelegramProperties properties;

    public TelegramStartupCheck(RestClient telegramRestClient, TelegramProperties properties) {
        this.telegramRestClient = telegramRestClient;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            log.info("Telegram startup check skipped because TELEGRAM_ENABLED=false");
            return;
        }

        if (isBlank(properties.botToken())) {
            log.warn("Telegram startup check skipped because TELEGRAM_BOT_TOKEN is empty");
            return;
        }

        var url = TELEGRAM_BASE_URL + "/bot" + properties.botToken() + "/getMe";
        try {
            var response = telegramRestClient.get()
                    .uri("/bot{token}/getMe", properties.botToken())
                    .retrieve()
                    .body(String.class);
            log.info("Telegram startup check succeeded url={} response={}", url, response);
        } catch (Exception exception) {
            log.error("Telegram startup check failed url={} error={} cause={}", url, exception.getMessage(), exception.getCause(), exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
