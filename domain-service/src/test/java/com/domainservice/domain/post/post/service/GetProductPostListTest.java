package com.domainservice.domain.post.post.service;

import com.common.model.web.PageResponse;
import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * ProductPostService 목록 조회 기능 테스트
 */
@ExtendWith(MockitoExtension.class)
class GetProductPostListTest {

    @InjectMocks
    private ProductPostService productPostService;

    @Mock
    private ProductPostRepository productPostRepository;

    @DisplayName("게시글 목록을 조회할 수 있다.")
    @Test
    void test1() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId("category-001")
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(ProductStatus.GOOD)
                        .tradeStatus(TradeStatus.SELLING)
                        .build(),
                ProductPost.builder()
                        .userId("user-456")
                        .categoryId("category-001")
                        .title("갤럭시 S24")
                        .name("Galaxy S24")
                        .price(1000000)
                        .status(ProductStatus.NEW)
                        .tradeStatus(TradeStatus.SELLING)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, null, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.pageNum()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(20);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.contents()).hasSize(2);
        assertThat(result.contents().get(0).title()).isEqualTo("아이폰 15 Pro");
        assertThat(result.contents().get(1).title()).isEqualTo("갤럭시 S24");

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("카테고리로 필터링하여 조회할 수 있다.")
    @Test
    void test2() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        String categoryId = "category-001";

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId(categoryId)
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(ProductStatus.GOOD)
                        .tradeStatus(TradeStatus.SELLING)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, categoryId, null, null, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).hasSize(1);
        assertThat(result.contents().get(0).categoryId()).isEqualTo(categoryId);

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("상품 상태로 필터링하여 조회할 수 있다.")
    @Test
    void test3() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        ProductStatus status = ProductStatus.NEW;

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId("category-001")
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(status)
                        .tradeStatus(TradeStatus.SELLING)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, status, null, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).hasSize(1);
        assertThat(result.contents().get(0).status()).isEqualTo(status);

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("거래 상태로 필터링하여 조회할 수 있다.")
    @Test
    void test4() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        TradeStatus tradeStatus = TradeStatus.SELLING;

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId("category-001")
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(ProductStatus.GOOD)
                        .tradeStatus(tradeStatus)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, tradeStatus, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).hasSize(1);
        assertThat(result.contents().get(0).tradeStatus()).isEqualTo(tradeStatus);

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("최소 가격으로 필터링하여 조회할 수 있다.")
    @Test
    void test5() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Integer minPrice = 1000000;

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId("category-001")
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(ProductStatus.GOOD)
                        .tradeStatus(TradeStatus.SELLING)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, null, minPrice, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).hasSize(1);
        assertThat(result.contents().get(0).price()).isGreaterThanOrEqualTo(minPrice);

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("최대 가격으로 필터링하여 조회할 수 있다.")
    @Test
    void test6() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Integer maxPrice = 1500000;

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId("category-001")
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(ProductStatus.GOOD)
                        .tradeStatus(TradeStatus.SELLING)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, null, null, maxPrice
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).hasSize(1);
        assertThat(result.contents().get(0).price()).isLessThanOrEqualTo(maxPrice);

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("가격 범위로 필터링하여 조회할 수 있다.")
    @Test
    void test7() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Integer minPrice = 1000000;
        Integer maxPrice = 1500000;

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId("category-001")
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(ProductStatus.GOOD)
                        .tradeStatus(TradeStatus.SELLING)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, null, minPrice, maxPrice
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).hasSize(1);
        assertThat(result.contents().get(0).price()).isBetween(minPrice, maxPrice);

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("여러 조건으로 필터링하여 조회할 수 있다.")
    @Test
    void test8() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        String categoryId = "category-001";
        ProductStatus status = ProductStatus.GOOD;
        TradeStatus tradeStatus = TradeStatus.SELLING;
        Integer minPrice = 1000000;
        Integer maxPrice = 1500000;

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId(categoryId)
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(status)
                        .tradeStatus(tradeStatus)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, categoryId, status, tradeStatus, minPrice, maxPrice
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).hasSize(1);
        assertThat(result.contents().get(0).categoryId()).isEqualTo(categoryId);
        assertThat(result.contents().get(0).status()).isEqualTo(status);
        assertThat(result.contents().get(0).tradeStatus()).isEqualTo(tradeStatus);
        assertThat(result.contents().get(0).price()).isBetween(minPrice, maxPrice);

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("빈 결과를 반환할 수 있다.")
    @Test
    void test9() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ProductPost> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(emptyPage);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, null, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).isEmpty();
        assertThat(result.totalPages()).isEqualTo(0);
        assertThat(result.hasNext()).isFalse();

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("페이지 번호를 지정하여 조회할 수 있다.")
    @Test
    void test10() {
        // given
        Pageable pageable = PageRequest.of(1, 10);  // 2페이지, 10개씩

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId("category-001")
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(ProductStatus.GOOD)
                        .tradeStatus(TradeStatus.SELLING)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, 25);  // 전체 25개

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, null, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.pageNum()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("마지막 페이지를 조회할 수 있다.")
    @Test
    void test11() {
        // given
        Pageable pageable = PageRequest.of(2, 10);  // 3페이지, 10개씩

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId("category-001")
                        .title("아이폰 15 Pro")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(ProductStatus.GOOD)
                        .tradeStatus(TradeStatus.SELLING)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, 25);  // 전체 25개

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, null, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.pageNum()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.hasNext()).isFalse();

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("삭제된 게시글은 목록에 표시되지 않는다.")
    @Test
    void test12() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        ProductPost activePost = ProductPost.builder()
                .userId("user-123")
                .categoryId("category-001")
                .title("아이폰 15 Pro")
                .name("iPhone 15 Pro")
                .price(1200000)
                .status(ProductStatus.GOOD)
                .tradeStatus(TradeStatus.SELLING)
                .build();

        // 삭제된 게시글은 Specification에서 필터링되어야 함
        List<ProductPost> posts = Arrays.asList(activePost);
        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, null, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).hasSize(1);
        assertThat(result.contents().get(0).title()).isEqualTo("아이폰 15 Pro");

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("판매 완료된 게시글도 조회할 수 있다.")
    @Test
    void test13() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        List<ProductPost> posts = Arrays.asList(
                ProductPost.builder()
                        .userId("user-123")
                        .categoryId("category-001")
                        .title("아이폰 15 Pro (판매완료)")
                        .name("iPhone 15 Pro")
                        .price(1200000)
                        .status(ProductStatus.GOOD)
                        .tradeStatus(TradeStatus.SOLDOUT)
                        .build()
        );

        Page<ProductPost> page = new PageImpl<>(posts, pageable, posts.size());

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(page);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, TradeStatus.SOLDOUT, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).hasSize(1);
        assertThat(result.contents().get(0).tradeStatus()).isEqualTo(TradeStatus.SOLDOUT);

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @DisplayName("존재하지 않는 페이지를 요청하면 빈 결과를 반환한다.")
    @Test
    void test14() {
        // given
        Pageable pageable = PageRequest.of(100, 20);  // 100페이지 요청 (존재하지 않음)
        Page<ProductPost> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        given(productPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(emptyPage);

        // when
        PageResponse<List<ProductPostResponse>> result = productPostService.getProductPostList(
                pageable, null, null, null, null, null
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.contents()).isEmpty();
        assertThat(result.pageNum()).isEqualTo(100);
        assertThat(result.totalPages()).isEqualTo(0);
        assertThat(result.hasNext()).isFalse();

        verify(productPostRepository).findAll(any(Specification.class), any(Pageable.class));
    }

}