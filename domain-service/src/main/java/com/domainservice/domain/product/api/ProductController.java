package com.domainservice.domain.product.api;

import com.common.model.web.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public BaseResponse<String> hello() {

        String data = "응답 데이터 추가";

        return new BaseResponse(
                data,
                "상품을 성공적으로 조회하였습니다."
        );
    }
}
