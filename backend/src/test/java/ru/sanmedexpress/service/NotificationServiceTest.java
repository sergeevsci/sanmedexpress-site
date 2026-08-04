package ru.sanmedexpress.service;

import org.junit.jupiter.api.Test;
import ru.sanmedexpress.domain.Client;
import ru.sanmedexpress.domain.OrderRequest;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationServiceTest {
    @Test
    void telegramIsStillCalledWhenEmailFails() {
        var emailService = mock(ReportEmailService.class);
        var telegramService = mock(TelegramNotificationService.class);
        var order = order();
        doThrow(new RuntimeException("SMTP is down")).when(emailService).sendNewOrder(order);

        new NotificationService(emailService, telegramService, Runnable::run).sendNewOrderNotifications(order);

        verify(emailService).sendNewOrder(order);
        verify(telegramService).sendNewOrder(order);
    }

    @Test
    void emailIsCalledWhenTelegramFails() {
        var emailService = mock(ReportEmailService.class);
        var telegramService = mock(TelegramNotificationService.class);
        var order = order();
        doThrow(new RuntimeException("Telegram is down")).when(telegramService).sendNewOrder(order);

        new NotificationService(emailService, telegramService, Runnable::run).sendNewOrderNotifications(order);

        verify(emailService).sendNewOrder(order);
        verify(telegramService).sendNewOrder(order);
    }

    private OrderRequest order() {
        return new OrderRequest(new Client("Maxim", "+79057690303"), "Комментарий", "127.0.0.1", "test-agent");
    }
}
