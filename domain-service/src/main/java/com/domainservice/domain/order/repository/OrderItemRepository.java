package com.domainservice.domain.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.order.model.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findOrderItemByOrder(Order order);
}
