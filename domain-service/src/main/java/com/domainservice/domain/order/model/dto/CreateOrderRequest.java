package com.domainservice.domain.order.model.dto;

import java.util.List;

public record CreateOrderRequest(
	List<String> cartItemIds

) {
}
