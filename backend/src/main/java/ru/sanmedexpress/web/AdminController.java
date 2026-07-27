package ru.sanmedexpress.web;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    private final OrderRequestRepository orderRequestRepository;
    private final OrderService orderService;

    public AdminController(OrderRequestRepository orderRequestRepository, OrderService orderService) {
        this.orderRequestRepository = orderRequestRepository;
        this.orderService = orderService;
    }

    @GetMapping({"", "/"})
    public String index(@RequestParam(required = false) OrderStatus status, Model model) {
        var sort = Sort.by(Sort.Direction.DESC, "createdAt");
        var orders = status == null ? orderRequestRepository.findAll(sort) : orderRequestRepository.findAllByStatus(status, sort);
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("todayCount", orderRequestRepository.countByCreatedAtAfter(OffsetDateTime.now().minusDays(1)));
        model.addAttribute("totalCount", orderRequestRepository.count());
        model.addAttribute("newCount", orderRequestRepository.findAllByStatus(OrderStatus.NEW, sort).size());
        model.addAttribute("moscowZone", MOSCOW_ZONE);
        return "admin/index";
    }

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) OrderStatus status) {
        var sort = Sort.by(Sort.Direction.DESC, "createdAt");
        var orders = status == null ? orderRequestRepository.findAll(sort) : orderRequestRepository.findAllByStatus(status, sort);
        var csv = buildCsv(orders);
        var filename = status == null ? "sanmedexpress-orders.csv" : "sanmedexpress-orders-" + status.name().toLowerCase() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        var order = orderRequestRepository.findById(id).orElseThrow();
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("moscowZone", MOSCOW_ZONE);
        return "admin/details";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
        return "redirect:/admin/" + id;
    }

    private String buildCsv(List<ru.sanmedexpress.domain.OrderRequest> orders) {
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        var builder = new StringBuilder("\uFEFF");
        builder.append("ID;Дата МСК;Имя;Телефон;Статус;Комментарий;Источник;IP\n");
        for (var order : orders) {
            builder.append(order.getId()).append(';')
                    .append(escape(order.getCreatedAt().atZoneSameInstant(MOSCOW_ZONE).format(formatter) + " МСК")).append(';')
                    .append(escape(order.getClient().getName())).append(';')
                    .append(escape(order.getClient().getPhone())).append(';')
                    .append(escape(order.getStatus().getTitle())).append(';')
                    .append(escape(order.getComment())).append(';')
                    .append(escape(order.getSource())).append(';')
                    .append(escape(order.getIpAddress())).append('\n');
        }
        return builder.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return '"' + value.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + '"';
    }
}
