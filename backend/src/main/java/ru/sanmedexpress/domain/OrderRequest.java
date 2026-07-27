package ru.sanmedexpress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "order_requests")
public class OrderRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(nullable = false, length = 40)
    private String source = "SITE";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderStatus status = OrderStatus.NEW;

    @Column(length = 80)
    private String ipAddress;

    @Column(columnDefinition = "text")
    private String userAgent;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected OrderRequest() {
    }

    public OrderRequest(Client client, String comment, String ipAddress, String userAgent) {
        this.client = client;
        this.comment = comment;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public Long getId() { return id; }
    public Client getClient() { return client; }
    public String getComment() { return comment; }
    public String getSource() { return source; }
    public OrderStatus getStatus() { return status; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
