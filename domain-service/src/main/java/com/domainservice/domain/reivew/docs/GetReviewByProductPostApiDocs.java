package com.domainservice.domain.reivew.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
	summary = "상품별 리뷰 단건 조회",
	description = """
        특정 상품 게시글(`productPostId`)에 작성된 리뷰를 조회합니다.
        
        ### 특징
        - 로그인하지 않은 사용자도 조회할 수 있습니다.
        """
)
@Parameters({
	@Parameter(
		name = "productPostId",
		description = "조회할 상품 게시글의 UUID",
		in = ParameterIn.PATH,
		required = true,
		schema = @Schema(type = "string")
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "리뷰 조회 성공"
	),
	@ApiResponse(
		responseCode = "404",
		description = "리뷰 없음 (아직 작성되지 않음)",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
                {
                    "code": 404,
                    "message": "해당 상품에 대한 리뷰가 존재하지 않습니다."
                }
                """
			)
		)
	),
	@ApiResponse(
		responseCode = "500",
		description = "서버 내부 오류",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
                {
                    "code": 500,
                    "message": "서버 내부 오류가 발생했습니다."
                }
                """
			)
		)
	)
})
public @interface GetReviewByProductPostApiDocs {
}
