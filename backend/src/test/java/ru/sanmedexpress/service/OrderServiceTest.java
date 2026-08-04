package ru.sanmedexpress.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.sanmedexpress.domain.OrderRequest;
import ru.sanmedexpress.repository.ClientRepository;
import ru.sanmedexpress.repository.OrderRequestRepository;
import ru.sanmedexpress.web.CreateOrderRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {
    @Test
    void createReturnsBeforeNotificationsAreSent() {
        var clientRepository = mock(ClientRepository.class);
        var orderRequestRepository = mock(OrderRequestRepository.class);
        var notificationService = mock(NotificationService.class);
        var servletRequest = mock(HttpServletRequest.class);
        var service = new OrderService(clientRepository, orderRequestRepository, notificationService);
        var request = new CreateOrderRequest("Максим", "+7 905 769-03-03", "Комментарий");

        when(clientRepository.findByPhone("+79057690303")).thenReturn(Optional.empty());
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(servletRequest.getHeader("User-Agent")).thenReturn("test-agent");
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        TransactionSynchronizationManager.initSynchronization();
        try {
            var saved = service.create(request, servletRequest);

            assertThat(saved.getClient().getPhone()).isEqualTo("+79057690303");
            verify(notificationService, never()).sendNewOrderNotifications(any());

            TransactionSynchronizationManager.getSynchronizations().forEach(synchronization -> synchronization.afterCommit());

            verify(notificationService).sendNewOrderNotifications(saved);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
