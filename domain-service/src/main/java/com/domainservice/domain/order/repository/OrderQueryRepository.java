package com.domainservice.domain.order.repository;

import java.util.Optional;

import com.domainservice.domain.order.model.dto.OrderedAt;

public interface OrderQueryRepository {


	Optional<OrderedAt> findOrderedAtByBuyerIdAndProductPostId(String productPostId, String userId);
}
