package com.aiservice.infrastructure.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.aiservice.infrastructure.feign.dto.CartItemResponse;
import com.aiservice.infrastructure.feign.dto.ProductPostEsSearchResponse;
import com.aiservice.infrastructure.feign.dto.PageResponse;

@FeignClient(name = "domain-service", url = "${custom.feign.url.domain-service}")
public interface DomainServiceClient {

    @GetMapping("/api/product-posts/search")
    PageResponse<List<ProductPostEsSearchResponse>> search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "size", defaultValue = "20") int size);

    @GetMapping("/api/carts/recent/{userId}")
    List<CartItemResponse> getRecentCartItems(@PathVariable("userId") String userId);
}
