package com.domainservice.common.init.data;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import com.domainservice.domain.reivew.model.dto.request.ReviewRequest;
import com.domainservice.domain.reivew.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewInitializer {

	private static final String[] USER_IDS = {"user-001", "user-002", "user-003", "user-004", "user-005"};

	private final ReviewService reviewService;
	private final ProductPostRepository productPostRepository;

	private static final Random RANDOM = new Random();

	public void init() {
		log.info("--- 리뷰 초기화 시작 ---");
		List<ProductPost> productPostList = productPostRepository.findAll();

		int postIndex = 0;

		while(postIndex < productPostList.size()) {
			int randomIndex = Math.abs(RANDOM.nextInt()) % USER_IDS.length;
			ProductPost post = productPostList.get(postIndex);
			if(post.getUserId().equals(USER_IDS[randomIndex])) {
				continue;
			}

			reviewService.createReview(
				new ReviewRequest(
					post.getId(),
					"내용%d".formatted(randomIndex),
					randomIndex % USER_IDS.length + 1,
					randomIndex % USER_IDS.length + 1
				),
				USER_IDS[randomIndex]
			);
			postIndex++;
		}

		log.info("--- 리뷰 초기화 종료 ---");
	}
}
