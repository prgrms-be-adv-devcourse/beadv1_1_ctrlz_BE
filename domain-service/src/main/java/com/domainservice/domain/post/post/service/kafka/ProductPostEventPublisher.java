package com.domainservice.domain.post.post.service.kafka;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.common.event.productPost.EventType;
import com.common.event.productPost.ProductPostDeleteEvent;
import com.common.event.productPost.ProductPostUpsertEvent;
import com.domainservice.domain.post.category.model.entity.Category;
import com.domainservice.domain.post.category.repository.CategoryRepository;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.repository.ProductPostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ProductPost 엔티티를 Kafka 이벤트로 변환하여 발행하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostEventPublisher {

	private final ProductPostEventProducer eventProducer;
	private final CategoryRepository categoryRepository;
	private final ProductPostRepository productPostRepository;

	/**
	 * CREATE 이벤트 발행
	 */
	public void publishCreateEvent(ProductPost productPost) {
		publishUpsertEvent(productPost, EventType.CREATE);
	}

	/**
	 * UPDATE 이벤트 발행
	 */
	public void publishUpdateEvent(ProductPost productPost) {
		publishUpsertEvent(productPost, EventType.UPDATE);
	}

	public void publishUpdateEvent(String postId) {
		ProductPost target = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		publishUpsertEvent(target, EventType.UPDATE);
	}

	/**
	 * DELETE 이벤트 발행
	 */
	public void publishDeleteEvent(String postId) {
		try {

			ProductPostDeleteEvent event = new ProductPostDeleteEvent(postId, EventType.DELETE);
			eventProducer.sendDeleteEvent(event);

			log.debug("DELETE 이벤트 발행 요청 완료 - postId: {}", postId);

		} catch (Exception e) {

			log.error("DELETE 이벤트 발행 실패 - postId: {}", postId, e);
			// TODO: 이벤트 발행 실패 시 재시도 또는 보상 트랜잭션 고려

		}
	}

	// CREATE/UPDATE Event 처리
	private void publishUpsertEvent(ProductPost productPost, EventType eventType) {
		try {

			ProductPostUpsertEvent event = convertToUpsertEvent(productPost, eventType);
			eventProducer.sendUpsertEvent(event);

			log.debug("Upsert 이벤트 발행 요청 완료 - postId: {}, eventType: {}",
				productPost.getId(), eventType);

		} catch (Exception e) {

			log.error("Upsert 이벤트 발행 실패 - postId: {}, eventType: {}",
				productPost.getId(), eventType, e);
			// TODO: 이벤트 발행 실패 시 재시도 또는 보상 트랜잭션 고려

		}
	}

	// ProductPost → ProductPostUpsertEvent 변환
	private ProductPostUpsertEvent convertToUpsertEvent(ProductPost productPost, EventType eventType) {
		// TODO: Category 조회로 N+1 문제 가능성, 추후 최적화 필요
		String categoryName = categoryRepository.findById(productPost.getCategoryId())
			.map(Category::getName)
			.orElse(null);

		List<String> tagNames = productPost.getProductPostTags().stream()
			.map(ppt -> ppt.getTag().getName())
			.toList();

		return ProductPostUpsertEvent.builder()
			.id(productPost.getId())
			.name(productPost.getName())
			.title(productPost.getTitle())
			.description(productPost.getDescription())
			.tags(tagNames)
			.categoryName(categoryName)
			.price(productPost.getPrice().longValue())
			.likedCount(productPost.getLikedCount().longValue())
			.viewCount(productPost.getViewCount().longValue())
			.status(productPost.getStatus().name())
			.tradeStatus(productPost.getTradeStatus().name())
			.deleteStatus(productPost.getDeleteStatus().name())
			.createdAt(productPost.getCreatedAt())
			.eventType(eventType)
			.build();
	}
}