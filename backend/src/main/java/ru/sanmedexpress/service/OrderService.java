package ru.sanmedexpress.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sanmedexpress.domain.Client;
import ru.sanmedexpress.domain.OrderRequest;
import ru.sanmedexpress.domain.OrderStatus;
import ru.sanmedexpress.repository.ClientRepository;
import ru.sanmedexpress.repository.OrderRequestRepository;
import ru.sanmedexpress.web.CreateOrderRequest;

@Service
public class OrderService {
    private final ClientRepository clientRepository;
    private final OrderRequestRepository orderRequestRepository;
    private final ReportEmailService reportEmailService;

    public OrderService(ClientRepository clientRepository, OrderRequestRepository orderRequestRepository, ReportEmailService reportEmailService) {
        this.clientRepository = clientRepository;
        this.orderRequestRepository = orderRequestRepository;
        this.reportEmailService = reportEmailService;
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
        reportEmailService.sendNewOrder(saved);
        return saved;
    }

    @Transactional
    public void updateStatus(Long id, OrderStatus status) {
        var order = orderRequestRepository.findById(id).orElseThrow();
        order.setStatus(status);
    }

    private String normalizePhone(String phone) {
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
