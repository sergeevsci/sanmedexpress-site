package ru.sanmedexpress.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private final OrderService orderService;

    public ApiOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, HttpServletRequest servletRequest) {
        var order = orderService.create(request, servletRequest);
        return new OrderResponse(order.getId(), "Заявка принята");
    }
}
