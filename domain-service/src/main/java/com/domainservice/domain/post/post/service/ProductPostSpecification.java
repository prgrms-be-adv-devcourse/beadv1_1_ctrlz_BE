package com.domainservice.domain.post.post.service;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자가 입력한 필터링을 받아서 동적으로 검색 조건을 생성해주는 클래스
 */
public class ProductPostSpecification {

    public static Specification<ProductPost> searchWith(
            String categoryId,
            ProductStatus status,
            TradeStatus tradeStatus,
            Integer minPrice,
            Integer maxPrice
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 삭제되지 않은 게시글만 조회
            predicates.add(criteriaBuilder.equal(root.get("deleteStatus"), BaseEntity.DeleteStatus.N));

            // 카테고리 필터
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }

            // 상품 상태 필터
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 거래 상태 필터
            if (tradeStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("tradeStatus"), tradeStatus));
            }

            // 최소 가격 필터
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            // 최대 가격 필터
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}