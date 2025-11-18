package com.domainservice.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.order.model.entity.Order;

public interface OrderJpaRepository extends JpaRepository<Order, String> {
}
