package com.domainservice.domain.order.repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.order.model.entity.Order;

public interface OrderRepository extends JpaRepository<Order, String>, OrderQueryRepository {
    Optional<Order> findById(String orderId);

    List<Order> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

}
