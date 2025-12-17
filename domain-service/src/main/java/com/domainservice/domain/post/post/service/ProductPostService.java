package com.domainservice.domain.post.post.service;

import static com.common.exception.vo.ProductPostExceptionCode.*;
import static com.common.exception.vo.UserExceptionCode.*;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.common.event.productPost.EventType;
import com.common.exception.CustomException;
import com.common.model.persistence.BaseEntity;
import com.common.model.vo.TradeStatus;
import com.common.model.web.PageResponse;
import com.domainservice.common.configuration.feign.client.UserFeignClient;
import com.domainservice.common.model.user.UserResponse;
import com.domainservice.domain.asset.image.application.ImageService;
import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.asset.image.domain.entity.ImageTarget;
import com.domainservice.domain.post.category.model.entity.Category;
import com.domainservice.domain.post.category.repository.CategoryRepository;
import com.domainservice.domain.post.kafka.handler.ProductPostEventProducer;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.mapper.ProductPostMapper;
import com.domainservice.domain.post.post.model.dto.request.ProductPostRequest;
import com.domainservice.domain.post.post.model.dto.request.ProductPostSearchRequest;
import com.domainservice.domain.post.post.model.dto.response.ProductPostDescription;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import com.domainservice.domain.post.tag.model.entity.Tag;
import com.domainservice.domain.post.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 상품 게시글의 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductPostService {

	private final ProductPostEventProducer eventProducer;

	private final UserFeignClient userFeignClient;

	private final ImageService imageService;
	private final RecentlyViewedService recentlyViewedService;

	private final TagRepository tagRepository;
	private final CategoryRepository categoryRepository;
	private final ProductPostRepository productPostRepository;

	private static final int MAX_COUNT = 10;    // 최근 본 상품으로 조회할 최대 개수

	/**
	 * 상품 게시글을 생성합니다.
	 *
	 * @param request    게시글 생성 요청 정보
	 * @param userId     작성자 ID
	 * @param imageFiles 업로드할 이미지 파일 목록 (최소 1개 필수)
	 * @return 생성된 게시글 정보
	 * @throws ProductPostException 이미지가 없거나 10개를 초과하는 경우
	 */
	public ProductPostResponse createProductPost(
		ProductPostRequest request, String userId, List<MultipartFile> imageFiles) {

		UserResponse userInfo = getUserInfoByFeignClient(userId);
		validateSellerPermission(userInfo);

		ProductPost productPost = ProductPost.builder()
			.userId(userId)
			.categoryId(request.categoryId())
			.title(request.title())
			.name(request.name())
			.price(request.price())
			.description(request.description())
			.status(request.status())
			.tradeStatus(TradeStatus.SELLING)
			.build();

		addTags(productPost, request.tagIds());

		// 첨부된 이미지를 s3 업로드 후 productPost에 추가
		validateUploadImage(imageFiles);
		uploadAndAddImages(productPost, imageFiles);

		ProductPost saved = productPostRepository.save(productPost);

		// Elasticsearch 동기화를 위한 Kafka 이벤트 발행
		eventProducer.sendUpsertEvent(saved, EventType.CREATE);

		return ProductPostMapper.toResponse(saved);
	}

	/**
	 * 상품 게시글을 수정합니다. 기존 이미지는 삭제되고 새 이미지로 교체됩니다.
	 *
	 * @param request    게시글 수정 요청 정보
	 * @param imageFiles 새로운 이미지 파일 목록 (최소 1개 필수)
	 * @param userId     수정 요청자 ID
	 * @param postId     수정할 게시글 ID
	 * @return 수정된 게시글 정보
	 * @throws ProductPostException 게시글이 존재하지 않거나 수정 권한이 없는 경우
	 */
	public ProductPostResponse updateProductPost(
		ProductPostRequest request, List<MultipartFile> imageFiles, String userId, String postId) {

		UserResponse userInfo = getUserInfoByFeignClient(userId);
		validateSellerPermission(userInfo);

		ProductPost target = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		// 게시물이 수정 가능한 상태인지 유효성 검사
		target.validateUpdate(userId, userInfo.roles());

		// 기존 저장된 이미지 삭제하고 새 이미지로 교체
		replaceImages(target, imageFiles);

		// 게시글에 입력받은 tag 추가
		addTags(target, request.tagIds());

		// request 요청으로 게시글 정보 변경
		target.update(request);

		// Elasticsearch 동기화를 위한 Kafka 이벤트 발행
		eventProducer.sendUpsertEvent(target, EventType.UPDATE);

		return ProductPostMapper.toResponse(target);
	}

	/**
	 * 상품 게시글을 삭제합니다. (Soft Delete)
	 *
	 * @param userId 삭제 요청자 ID
	 * @param postId 삭제할 게시글 ID
	 * @return 삭제된 게시글 ID
	 * @throws ProductPostException 게시글이 존재하지 않거나 삭제 권한이 없는 경우
	 */
	public String deleteProductPost(String userId, String postId) {

		UserResponse userInfo = getUserInfoByFeignClient(userId);
		validateSellerPermission(userInfo);

		ProductPost target = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		// 게시물이 삭제 가능한 상태인지 유효성 검사
		target.validateDelete(userId, userInfo.roles());

		// 테이블에 저장된 이미지 삭제
		deleteProductPostImages(target);

		// soft delete 처리
		target.delete();

		// Elasticsearch 동기화를 위한 Kafka 이벤트 발행
		String targetId = target.getId();
		eventProducer.sendDeleteEvent(targetId);

		return targetId;
	}

	/**
	 * 단일 상품 게시글을 조회합니다.
	 * 비회원 조회 (userId X)
	 * 조회 시 조회수가 증가합니다.
	 *
	 * @param postId 조회할 게시글 ID
	 * @param viewerId 게시글을 조회한 유저 ID
	 * @return 게시글 정보 (판매자 닉네임 포함)
	 * @throws ProductPostException 게시글이 존재하지 않거나 삭제된 경우
	 */
	public ProductPostDescription getProductPostById(String viewerId, String postId) {

		ProductPost productPost = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		if (productPost.getDeleteStatus() == BaseEntity.DeleteStatus.D) {
			throw new ProductPostException(PRODUCT_POST_DELETED);
		}

		productPostRepository.incrementViewCount(postId); // @Modifying을 통해 DB에 조회수 증가를 적용하기 (조회수 정합성 관리)
		productPost.incrementViewCount(); // db에는 조회수 증가가 적용되었으나 entity의 값은 변경되지 않음, dto로 반환 시 값을 맞춰주기 위해 증가

		String categoryName = categoryRepository.findById(productPost.getCategoryId())
			.map(Category::getName)
			.orElseThrow(() -> new ProductPostException(CATEGORY_NOT_FOUND));

		UserResponse sellerInfo = getUserInfoByFeignClient(productPost.getUserId());

		// 게시글을 조회한 유저가 로그인한 회원이라면 redis에 최근 본 상품 목록으로 저장
		if (!viewerId.equals("anonymous")) {
			getUserInfoByFeignClient(viewerId); // 해당 유저가 존재하는지 검증
			recentlyViewedService.addRecentlyViewedPost(viewerId, productPost.getId(), MAX_COUNT);
		}

		return ProductPostMapper.toDescription(productPost, sellerInfo.nickname(), categoryName, viewerId);

	}

	// 내부 통신용 getProductPost 메소드
	public ProductPostResponse getProductPostById(String postId) {
		ProductPost productPost = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));
		return ProductPostMapper.toResponse(productPost);
	}

	/**
	 * 상품 게시글 목록을 페이징하여 조회합니다. 동적 필터링(Specification)을 지원합니다.
	 *
	 * @param pageable	페이징 정보
	 * @param request 	상품 필터 정보
	 * @return 페이징된 게시글 목록
	 */
	public PageResponse<List<ProductPostResponse>> getProductPostList(
		Pageable pageable, ProductPostSearchRequest request) {
		// Specification 생성
		Specification<ProductPost> spec = ProductPostSpecification.searchWith(request);

		Page<ProductPost> page = productPostRepository.findAll(spec, pageable);

		// PageResponse 생성
		return new PageResponse<>(
			page.getNumber(),
			page.getTotalPages(),
			page.getSize(),
			page.hasNext(),
			page.getContent().stream()
				.map(ProductPostMapper::toResponse)
				.toList()
		);
	}

	/**
	 * 사용자가 최근 본 게시물 목록을 조회합니다.
	 * Redis에서 게시물 ID를 가져온 후 DB에서 실제 게시물 정보를 조회합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 최근 본 게시물 응답 목록
	 */
	public List<ProductPostResponse> getRecentlyViewedPosts(String userId) {
		getUserInfoByFeignClient(userId); // 사용자 정보가 조회되지 않으면 예외 발생
		Set<String> viewedPostIds = recentlyViewedService.getRecentlyViewedPostIds(userId, MAX_COUNT);

		return productPostRepository.findAllById(viewedPostIds)
			.stream()
			.map(ProductPostMapper::toResponse)
			.toList();
	}

    /*
    ================= private Method =================
     */

	/**
	 * 게시글의 이미지를 교체합니다. 기존 이미지는 S3 및 DB에서 삭제됩니다.
	 */
	private void replaceImages(ProductPost productPost, List<MultipartFile> imageFiles) {
		// 첨부한 이미지 유효성 체크
		validateUploadImage(imageFiles);

		// 테이블에서 기존 이미지 삭제
		deleteProductPostImages(productPost);

		// 새 이미지 업로드 및 productPost에 추가
		uploadAndAddImages(productPost, imageFiles);
	}

	/**
	 * 이미지를 S3에 업로드하고 게시글에 추가합니다.
	 */
	private void uploadAndAddImages(ProductPost productPost, List<MultipartFile> imageFiles) {
		List<Image> uploadedImages = imageService.uploadProfileImageListByTarget(imageFiles, ImageTarget.PRODUCT);
		productPost.addImages(uploadedImages);
	}

	/**
	 * 업로드할 이미지의 유효성을 검증합니다.
	 *
	 * @throws ProductPostException 이미지가 없거나 10개를 초과하는 경우
	 */
	private void validateUploadImage(List<MultipartFile> imageFiles) {

		// 게시글 등록 시 이미지 반드시 1개는 필요, 없으면 예외처리
		if (imageFiles == null || imageFiles.isEmpty()) {
			throw new ProductPostException(IMAGE_REQUIRED);
		}

		// 10개를 초과해서 등록하더라도 예외처리
		if (imageFiles.size() > 10) {
			throw new ProductPostException(TOO_MANY_IMAGES);
		}
	}

	/**
	 * 게시글에 연결된 모든 이미지를 S3 및 DB에서 삭제합니다.
	 */
	private void deleteProductPostImages(ProductPost target) {
		List<String> targetIds = target.getProductPostImages().stream()
			.map(e -> e.getImage().getId())
			.toList();

		// clear 하게되면 'orphanRemoval = true' 옵션에 의해 ProductPostImage를 DB에 DELETE 요청함
		target.getProductPostImages().clear();
		productPostRepository.flush();

		targetIds.forEach(imageService::deleteProfileImageById);
	}

	/**
	 * 게시글에 태그를 추가합니다.
	 *
	 * @throws ProductPostException 존재하지 않는 태그 ID가 포함된 경우
	 */
	private void addTags(ProductPost productPost, List<String> tagIds) {
		if (tagIds != null) {
			List<Tag> tags = tagRepository.findAllById(tagIds);

			if (tags.size() != tagIds.size()) {
				throw new ProductPostException(TAG_NOT_FOUND);
			}
			productPost.replaceTags(tags);
		}
	}

	// FeignClient(userClient)를 통해 userId로 사용자 정보를 조회합니다.
	private UserResponse getUserInfoByFeignClient(String userId) {
		return userFeignClient.getUser(userId);
	}

	// feignClient를 통해 얻게된 사용자 정보의 판매자 인증 여부를 검증합니다.
	private void validateSellerPermission(UserResponse user) {
		if (!user.roles().contains("ADMIN") && !user.roles().contains("SELLER")) {
			throw new CustomException(SELLER_PERMISSION_REQUIRED.getMessage());
		}
	}

	public boolean isSellingTradeStatus(String id) {
		ProductPost productPost = productPostRepository.findById(id)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));
		return productPost.getTradeStatus() == TradeStatus.SELLING;
	}

	public void updateTradeStatusById(String postId, TradeStatus tradeStatus) {
		ProductPost product = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		switch (tradeStatus) {
			case PROCESSING -> product.markAsProcessing();
			case SOLDOUT -> product.markAsSoldout();
			case SELLING -> product.markAsSellingAgain();
		}

		productPostRepository.save(product);
	}

	// TODO: 내가 구매한 상품 조회
	// TODO: 내가 판매한 상품 조회

}
