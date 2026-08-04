package ru.sanmedexpress.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.sanmedexpress.config.TelegramProperties;
import ru.sanmedexpress.domain.OrderRequest;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class TelegramNotificationService {
    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);
    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'МСК'");

    private final RestClient telegramRestClient;
    private final TelegramProperties properties;

    public TelegramNotificationService(RestClient telegramRestClient, TelegramProperties properties) {
        this.telegramRestClient = telegramRestClient;
        this.properties = properties;
    }

    public void sendNewOrder(OrderRequest order) {
        if (!properties.enabled()) {
            log.info("Telegram notifications are disabled");
            return;
        }

        if (isBlank(properties.botToken()) || isBlank(properties.chatId())) {
            log.warn("Telegram notifications are enabled but bot token or chat id is empty; skip order id={}", order.getId());
            return;
        }

        try {
            telegramRestClient.post()
                    .uri("/bot{token}/sendMessage", properties.botToken())
                    .body(Map.of("chat_id", properties.chatId(), "text", buildMessage(order)))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Telegram notification sent for order id={}", order.getId());
        } catch (RestClientException exception) {
            log.error("Cannot send Telegram notification for order id={}", order.getId(), exception);
        }
    }

    String buildMessage(OrderRequest order) {
        var comment = order.getComment();
        if (isBlank(comment)) {
            comment = "Не указан";
        }
        return "🚑 Новая заявка\n\n"
                + "№" + (order.getId() == null ? "-" : order.getId()) + "\n\n"
                + "Имя:\n" + safe(order.getClient().getName()) + "\n\n"
                + "Телефон:\n" + safe(order.getClient().getPhone()) + "\n\n"
                + "Комментарий:\n" + comment + "\n\n"
                + "Дата:\n" + order.getCreatedAt().atZoneSameInstant(MOSCOW_ZONE).format(DATE_FORMATTER);
    }

    private String safe(String value) {
        return value == null ? "Не указан" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
