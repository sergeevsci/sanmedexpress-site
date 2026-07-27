package ru.sanmedexpress.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sanmedexpress.domain.OrderRequest;
import ru.sanmedexpress.domain.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;

public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {
    List<OrderRequest> findAllByStatus(OrderStatus status, Sort sort);
    long countByCreatedAtAfter(OffsetDateTime dateTime);
}
