package ru.sanmedexpress.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.sanmedexpress.service.OrderService;

@RestController
@RequestMapping("/api/requests")
public class ApiOrderController {
    private static final Logger log = LoggerFactory.getLogger(ApiOrderController.class);

    private final OrderService orderService;

    public ApiOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, HttpServletRequest servletRequest) {
        log.info("Incoming site request name={} phone={} ip={}", request.name(), request.phone(), getIp(servletRequest));
        var order = orderService.create(request, servletRequest);
        return new OrderResponse(order.getId(), "Заявка принята");
    }

    private String getIp(HttpServletRequest request) {
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
