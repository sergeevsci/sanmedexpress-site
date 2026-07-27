package ru.sanmedexpress.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.sanmedexpress.config.EmailReportProperties;
import ru.sanmedexpress.domain.OrderRequest;

@Service
public class ReportEmailService {
    private static final Logger log = LoggerFactory.getLogger(ReportEmailService.class);

    private final JavaMailSender mailSender;
    private final EmailReportProperties properties;

    public ReportEmailService(JavaMailSender mailSender, EmailReportProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void sendNewOrder(OrderRequest order) {
        if (!properties.enabled()) {
            return;
        }

        var message = new SimpleMailMessage();
        message.setTo(properties.to());
        message.setFrom(properties.from());
        message.setSubject("Новая заявка СанМедЭкспресс #" + order.getId());
        message.setText("Имя: " + order.getClient().getName() + "\n"
                + "Телефон: " + order.getClient().getPhone() + "\n"
                + "Комментарий: " + (order.getComment() == null ? "-" : order.getComment()) + "\n"
                + "Дата: " + order.getCreatedAt());
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.warn("Cannot send order email", exception);
        }
    }
}
