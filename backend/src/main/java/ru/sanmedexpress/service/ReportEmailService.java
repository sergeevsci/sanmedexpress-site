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
            log.info("Email notification skipped for order id={} because EMAIL_ENABLED=false", order.getId());
            return;
        }

        log.info("Sending email notification for order id={} to={} from={}", order.getId(), properties.to(), properties.from());

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
            log.info("Email notification sent successfully for order id={} to={}", order.getId(), properties.to());
        } catch (MailException exception) {
            log.error("Email notification failed for order id={} to={} error={}", order.getId(), properties.to(), exception.getMessage(), exception);
        }
    }
}
