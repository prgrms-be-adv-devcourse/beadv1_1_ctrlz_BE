package com.aiservice.infrastructure.feign.dto;

import java.math.BigDecimal;

/**
 * 장바구니 아이템 DTO
 * Domain Service의 CartItemResponse와 매핑
 */
public record CartItemResponse(
        String cartItemId,
        String title,
        String name,
        BigDecimal price,
        boolean isSelected) {
}
