package ru.sanmedexpress.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.sanmedexpress.domain.OrderRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ReportEmailService reportEmailService;
    private final TelegramNotificationService telegramNotificationService;
    private final Executor notificationTaskExecutor;

    public NotificationService(ReportEmailService reportEmailService,
                               TelegramNotificationService telegramNotificationService,
                               @Qualifier("notificationTaskExecutor") Executor notificationTaskExecutor) {
        this.reportEmailService = reportEmailService;
        this.telegramNotificationService = telegramNotificationService;
        this.notificationTaskExecutor = notificationTaskExecutor;
    }

    @Async("notificationTaskExecutor")
    public void sendNewOrderNotifications(OrderRequest order) {
        runChannelAsync("Email", order, () -> reportEmailService.sendNewOrder(order));
        runChannelAsync("Telegram", order, () -> telegramNotificationService.sendNewOrder(order));
    }

    private void runChannelAsync(String channel, OrderRequest order, Runnable task) {
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    task.run();
                } catch (Exception exception) {
                    log.warn("{} notification channel failed unexpectedly for order id={}", channel, order.getId(), exception);
                }
            }, notificationTaskExecutor);
        } catch (Exception exception) {
            log.warn("Cannot schedule {} notification channel for order id={}", channel, order.getId(), exception);
        }
    }
}
