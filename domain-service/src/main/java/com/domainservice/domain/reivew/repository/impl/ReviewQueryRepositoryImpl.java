package com.domainservice.domain.reivew.repository.impl;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.domainservice.domain.reivew.model.entity.Review;
import com.domainservice.domain.reivew.repository.ReviewQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import static com.domainservice.domain.reivew.model.entity.QReview.review;

@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Review> findReviewsByUserId(String userId, Pageable pageable) {
		return queryFactory
			.selectFrom(review)
			.where(review.userId.eq(userId))
			.orderBy(review.createdAt.desc())            // 필요에 맞게 정렬 필드 지정
			.offset(pageable.getOffset())                // 시작 위치
			.limit(pageable.getPageSize())               // 개수
			.fetch();
	}
}
