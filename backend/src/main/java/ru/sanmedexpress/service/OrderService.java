package ru.sanmedexpress.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.sanmedexpress.domain.Client;
import ru.sanmedexpress.domain.OrderRequest;
import ru.sanmedexpress.domain.OrderStatus;
import ru.sanmedexpress.repository.ClientRepository;
import ru.sanmedexpress.repository.OrderRequestRepository;
import ru.sanmedexpress.web.CreateOrderRequest;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final ClientRepository clientRepository;
    private final OrderRequestRepository orderRequestRepository;
    private final NotificationService notificationService;

    public OrderService(ClientRepository clientRepository, OrderRequestRepository orderRequestRepository, NotificationService notificationService) {
        this.clientRepository = clientRepository;
        this.orderRequestRepository = orderRequestRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public OrderRequest create(CreateOrderRequest request, HttpServletRequest servletRequest) {
        var phone = normalizePhone(request.phone());
        var name = request.name().trim();
        var client = clientRepository.findByPhone(phone)
                .map(existing -> {
                    existing.setName(name);
                    return existing;
                })
                .orElseGet(() -> new Client(name, phone));
        clientRepository.save(client);

        var order = new OrderRequest(client, cleanComment(request.comment()), getIp(servletRequest), servletRequest.getHeader("User-Agent"));
        var saved = orderRequestRepository.save(order);
        log.info("Created order request id={} clientId={} phone={} source={} ip={}", saved.getId(), client.getId(), client.getPhone(), saved.getSource(), saved.getIpAddress());
        sendNotificationsAfterCommit(saved);
        return saved;
    }

    private void sendNotificationsAfterCommit(OrderRequest order) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    notificationService.sendNewOrderNotifications(order);
                } catch (Exception exception) {
                    log.warn("Cannot schedule notifications for order id={}", order.getId(), exception);
                }
            }
        });
    }

    @Transactional
    public void updateStatus(Long id, OrderStatus status) {
        var order = orderRequestRepository.findById(id).orElseThrow();
        var oldStatus = order.getStatus();
        order.setStatus(status);
        log.info("Updated order request id={} status {} -> {}", id, oldStatus, status);
    }

    private String normalizePhone(String phone) {
        var digits = phone.replaceAll("\\D+", "");
        if (digits.length() == 11 && digits.startsWith("8")) {
            return "+7" + digits.substring(1);
        }
        if (digits.length() == 11 && digits.startsWith("7")) {
            return "+" + digits;
        }
        if (digits.length() == 10) {
            return "+7" + digits;
        }
        return phone.trim().replaceAll("\\s+", " ");
    }

    private String cleanComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        return comment.trim();
    }

    private String getIp(HttpServletRequest request) {
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
