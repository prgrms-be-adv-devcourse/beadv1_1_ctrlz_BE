package com.domainservice.domain.reivew.repository.impl;

import java.util.List;

import com.domainservice.domain.reivew.model.entity.Review;
import com.domainservice.domain.reivew.repository.ReviewQueryRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

	// private final JPAQueryFactory queryFactory;
	private static final int PAGE_SIZE = 10;

	/**
	 * 리뷰 목록 조회(페이징 O - Pageable -> KeySetPagination 전환 예정)
	 * @param id
	 * @param userId
	 * @return
	 */
	@Override
	public List<Review> findPagedReviewByIdAndUserId(String id, String userId) {
		return null;
	}
}
