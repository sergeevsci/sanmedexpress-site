package ru.sanmedexpress.web;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sanmedexpress.domain.OrderStatus;
import ru.sanmedexpress.repository.OrderRequestRepository;
import ru.sanmedexpress.service.OrderService;

import java.time.OffsetDateTime;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final OrderRequestRepository orderRequestRepository;
    private final OrderService orderService;

    public AdminController(OrderRequestRepository orderRequestRepository, OrderService orderService) {
        this.orderRequestRepository = orderRequestRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) OrderStatus status, Model model) {
        var sort = Sort.by(Sort.Direction.DESC, "createdAt");
        var orders = status == null ? orderRequestRepository.findAll(sort) : orderRequestRepository.findAllByStatus(status, sort);
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("todayCount", orderRequestRepository.countByCreatedAtAfter(OffsetDateTime.now().minusDays(1)));
        return "admin/index";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        var order = orderRequestRepository.findById(id).orElseThrow();
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/details";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
        return "redirect:/admin/" + id;
    }
}
