package ru.sanmedexpress.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.sanmedexpress.domain.OrderRequest;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ReportEmailService reportEmailService;
    private final TelegramNotificationService telegramNotificationService;

    public NotificationService(ReportEmailService reportEmailService, TelegramNotificationService telegramNotificationService) {
        this.reportEmailService = reportEmailService;
        this.telegramNotificationService = telegramNotificationService;
    }

    public void sendNewOrderNotifications(OrderRequest order) {
        try {
            reportEmailService.sendNewOrder(order);
        } catch (Exception exception) {
            log.warn("Email notification channel failed unexpectedly for order id={}", order.getId(), exception);
        }

        try {
            telegramNotificationService.sendNewOrder(order);
        } catch (Exception exception) {
            log.warn("Telegram notification channel failed unexpectedly for order id={}", order.getId(), exception);
        }
    }
}
