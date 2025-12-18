package com.domainservice.domain.post.post.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.domainservice.domain.post.post.model.entity.ProductPost;

public interface ProductPostRepository extends JpaRepository<ProductPost, String>,
	JpaSpecificationExecutor<ProductPost> {

	// Initializer 전용 메서드
	@Query("SELECT p.id FROM ProductPost p WHERE p.userId <> :userId AND p.deleteStatus = 'N'")
	List<String> findAllIdsExceptOwner(@Param("userId") String userId);

	/**
	 * 특정 상품 게시글의 '좋아요' 수를 1 증가시킵니다.
	 * @Modifying Dirty Checking을 거치지 않고 DB에 직접 UPDATE 쿼리를 실행합니다.
	 * 			  따라서 동시에 여러 트랜잭션이 접근하더라도 DB 레벨에서 락과 원자성을 보장해 정합성을 유지하는 데 도움이 됩니다.
	 *
	 * 일반적인 JPA 동작 방식: 엔티티 조회(SELECT) → 자바 객체 값 변경 → 트랜잭션 종료 시 Dirty Checking으로 UPDATE
	 * 							(과정이 길고 엔티티를 영속성 컨텍스트에 올려야 함, 트랜잭션 종료 시에 반영되기 때문에 정합성 보장 어려움)
	 */
	@Modifying(clearAutomatically = false)
	@Query("""
		UPDATE ProductPost p
		SET p.likedCount = p.likedCount + 1
		WHERE p.id = :productPostId
		""")
	void incrementLikedCount(@Param("productPostId") String productPostId);

	/**
	 * 특정 상품 게시글의 '좋아요' 수를 1 감소시킵니다.
	 * @Modifying Dirty Checking을 거치지 않고 DB에 직접 UPDATE 쿼리를 실행합니다.
	 */
	@Modifying(clearAutomatically = false)
	@Query("""
		UPDATE ProductPost p
		SET p.likedCount = p.likedCount - 1
		WHERE p.id = :productPostId
		""")
	void decrementLikedCount(@Param("productPostId") String productPostId);

	@Modifying(clearAutomatically = false)
	@Query("""
		UPDATE ProductPost p
		SET p.viewCount = p.viewCount + 1
		WHERE p.id = :productPostId
		""")
	void incrementViewCount(@Param("productPostId") String productPostId);
}