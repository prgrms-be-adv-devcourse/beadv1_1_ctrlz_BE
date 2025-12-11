package com.domainservice.domain.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.order.model.entity.Order;

public interface OrderJpaRepository extends JpaRepository<Order, String> {
    Page<Order> findByBuyerId(String userId, Pageable pageable);

    List<Order> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}