
package com.domainservice.domain.post.favorite.service;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.common.model.vo.ProductStatus;
import com.common.model.vo.TradeStatus;
import com.domainservice.domain.post.favorite.repository.FavoriteRepository;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@DataJpaTest(showSql = false)
@Import({FavoriteService.class})
class FavoriteServiceConcurrencyTest {

	@Autowired
	private FavoriteService favoriteService;

	@Autowired
	private FavoriteRepository favoriteRepository;

	@Autowired
	private ProductPostRepository productPostRepository;

	private ProductPost testProductPost;

	@TestConfiguration
	static class TestConfig {

		@Bean
		public JPAQueryFactory queryFactory(EntityManager entityManager) {
			return new JPAQueryFactory(entityManager);
		}

	}

	@BeforeEach
	void setUp() {

		// 테스트용 상품 생성
		testProductPost = ProductPost.builder()
			.userId("seller-001")
			.categoryId("category-001")
			.title("테스트 상품")
			.name("동시성 테스트 상품")
			.price(10000)
			.description("동시성 테스트를 위한 상품입니다")
			.status(ProductStatus.NEW)
			.tradeStatus(TradeStatus.SELLING)
			.build();

		productPostRepository.save(testProductPost);

	}

	@AfterEach
	void tearDown() {
		favoriteRepository.deleteAll();
		productPostRepository.deleteAll();
	}

	/*
	@DataJpaTest는 기본적으로 모든 테스트 메서드에 @Transactional을 적용함
	즉, setUp() 메서드에서 게시글을 save 하더라도 영속성 컨텍스트에만 존재할 뿐 커밋된 상태가 아니라 조회가 안됨
	따라서 Propagation.NOT_SUPPORTED 를 통해 트랜잭션에 묶이지 않도록 하여 값을 조회할 수 있도록 함
	 */
	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@DisplayName("1000명이 동시에 좋아요를 누를 때 정합성 테스트")
	void concurrentLikeTest() throws InterruptedException {

		// given
		int threadCount = 1000;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		// when
		for (int i = 0; i < threadCount; i++) {
			String userId = "user-" + String.format("%03d", i);

			executorService.submit(() -> {
				try {
					favoriteService.addFavoriteProduct(userId, testProductPost.getId());
					successCount.incrementAndGet();
				} catch (Exception e) {
					failCount.incrementAndGet();
					System.err.println("Failed for user: " + Thread.currentThread().getName() +
						" - " + e.getClass().getSimpleName() + ": " + e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		// then
		ProductPost updatedPost = productPostRepository.findById(testProductPost.getId())
			.orElseThrow();

		long favoriteCount = favoriteRepository.count();

		// 검증
		assertThat(successCount.get()).isEqualTo(threadCount);
		assertThat(favoriteCount).isEqualTo(threadCount);
		assertThat(failCount.get()).isEqualTo(0);
		assertThat(updatedPost.getLikedCount()).isEqualTo(threadCount);
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@DisplayName("동일 사용자가 좋아요 추가/취소를 반복할 때 정합성 테스트")
	void sameUserRepeatAddAndCancelTest() throws InterruptedException {
		// given
		String userId = "user-001";
		int operationCount = 100;

		ExecutorService executorService = Executors.newFixedThreadPool(operationCount);
		CountDownLatch latch = new CountDownLatch(operationCount);

		AtomicInteger addSuccessCount = new AtomicInteger(0);
		AtomicInteger cancelSuccessCount = new AtomicInteger(0);
		AtomicInteger totalFailCount = new AtomicInteger(0);

		// when: 좋아요 요청와 취소를 번갈아 시도
		for (int i = 1; i <= operationCount; i++) {
			final boolean isAddOperation = (i % 2 == 0);

			executorService.submit(() -> {
				try {
					if (isAddOperation) {
						favoriteService.addFavoriteProduct(userId, testProductPost.getId());
						addSuccessCount.incrementAndGet();
					} else {
						favoriteService.cancelFavoriteProduct(userId, testProductPost.getId());
						cancelSuccessCount.incrementAndGet();
					}
				} catch (Exception e) {
					totalFailCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		// then
		ProductPost finalPost = productPostRepository.findById(testProductPost.getId())
			.orElseThrow();
		long finalFavoriteCount = favoriteRepository.count();

		// 최종 상태는 0 또는 1
		assertThat(finalFavoriteCount).isIn(0L, 1L);
		assertThat(finalPost.getLikedCount()).isIn(0, 1);

		// 저장된 favoritePost 개수와 likedCount가 일치해야 함
		assertThat(finalPost.getLikedCount()).isEqualTo((int)finalFavoriteCount);
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@DisplayName("동일 사용자가 동시에 100번 좋아요를 누를 때 1번만 성공")
	void sameUserConcurrent100TimesTest() throws InterruptedException {
		// given
		String sameUserId = "user-001";
		int attemptCount = 100;

		ExecutorService executorService = Executors.newFixedThreadPool(attemptCount);
		CountDownLatch latch = new CountDownLatch(attemptCount);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		// when: 같은 사용자가 동시에 100번 좋아요 시도
		for (int i = 0; i < attemptCount; i++) {
			executorService.submit(() -> {
				try {
					favoriteService.addFavoriteProduct(sameUserId, testProductPost.getId());
					successCount.incrementAndGet();
				} catch (Exception e) {
					failCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		// then
		ProductPost finalPost = productPostRepository.findById(testProductPost.getId())
			.orElseThrow();

		long finalFavoriteCount = favoriteRepository.count();

		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failCount.get()).isEqualTo(attemptCount - 1);
		assertThat(finalFavoriteCount).isEqualTo(1);
		assertThat(finalPost.getLikedCount()).isEqualTo(1);
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@DisplayName("50명 추가 후 50명 삭제 연속 진행")
	void sequentialAddAndRemoveTest() throws InterruptedException {
		// given
		int userCount = 50;

		// Phase 1: 50명이 동시에 좋아요 추가
		ExecutorService addExecutor = Executors.newFixedThreadPool(userCount);
		CountDownLatch addLatch = new CountDownLatch(userCount);

		for (int i = 0; i < userCount; i++) {
			String userId = "user-" + String.format("%03d", i);
			addExecutor.submit(() -> {
				try {
					favoriteService.addFavoriteProduct(userId, testProductPost.getId());
				} catch (Exception e) {
					System.out.println("Add failed: " + e.getMessage());
				} finally {
					addLatch.countDown();
				}
			});
		}

		addLatch.await();
		addExecutor.shutdown();

		// 중간 상태 확인
		ProductPost midPost = productPostRepository.findById(testProductPost.getId())
			.orElseThrow();
		long midFavoriteCount = favoriteRepository.count();

		System.out.println("\n=== 추가 완료 후 중간 상태 ===");
		System.out.println("favorite 레코드 수: " + midFavoriteCount);
		System.out.println("likedCount: " + midPost.getLikedCount());

		assertThat(midFavoriteCount).isEqualTo(userCount);
		assertThat(midPost.getLikedCount()).isEqualTo(userCount);

		// Phase 2: 50명이 동시에 좋아요 삭제
		ExecutorService removeExecutor = Executors.newFixedThreadPool(userCount);
		CountDownLatch removeLatch = new CountDownLatch(userCount);

		for (int i = 0; i < userCount; i++) {
			String userId = "user-" + String.format("%03d", i);
			removeExecutor.submit(() -> {
				try {
					favoriteService.cancelFavoriteProduct(userId, testProductPost.getId());
				} catch (Exception e) {
					System.out.println("Remove failed: " + e.getMessage());
				} finally {
					removeLatch.countDown();
				}
			});
		}

		removeLatch.await();
		removeExecutor.shutdown();

		// 최종 상태 확인
		ProductPost finalPost = productPostRepository.findById(testProductPost.getId())
			.orElseThrow();
		long finalFavoriteCount = favoriteRepository.count();

		// 모두 삭제되어 0이어야 함
		assertThat(finalFavoriteCount).isEqualTo(0);
		assertThat(finalPost.getLikedCount()).isEqualTo(0);
	}

}