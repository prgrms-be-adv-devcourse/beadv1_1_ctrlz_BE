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
	summary = "내 리뷰 목록 조회",
	description = """
        로그인한 사용자가 작성한 리뷰 목록을 조회합니다.
        
        ### 기능
        - **페이징**: 페이지 번호(`pageNumber`)를 지정하여 조회합니다. (0부터 시작)
        - **정렬**: 최신순(기본값)으로 정렬됩니다.
        """
)
@Parameters({
	@Parameter(
		name = "X-REQUEST-ID",
		description = "사용자 ID",
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", example = "user-uuid-1234")
	),
	@Parameter(
		name = "pageNumber",
		description = "조회할 페이지 번호 (0부터 시작)",
		in = ParameterIn.QUERY,
		required = true,
		schema = @Schema(type = "integer", defaultValue = "0", minimum = "0")
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "내 리뷰 목록 조회 성공",
		content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				value = """
                {
                    "data": [
                        {
                            "reviewId": "review-uuid-1",
                            "userId": "user-uuid-1234",
                            "nickname": "구매왕",
                            "profileImageUrl": "https://s3/profile.jpg",
                            "contents": "상품 정말 좋아요!",
                            "userRating": 5,
                            "productRating": 5,
                            "orderedAt": "2024-03-15T10:00:00",
                            "isMine": true
                        },
                        {
                            "reviewId": "review-uuid-2",
                            "userId": "user-uuid-1234",
                            "nickname": "구매왕",
                            "profileImageUrl": "https://s3/profile.jpg",
                            "contents": "배송이 조금 늦었네요.",
                            "userRating": 4,
                            "productRating": 3,
                            "orderedAt": "2024-03-10T14:30:00",
                            "isMine": true
                        }
                    ],
                    "message": "리뷰 데이터를 가져왔습니다."
                }
                """
			)
		)
	),
	@ApiResponse(
		responseCode = "401",
		description = "인증 실패 (헤더 누락)",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
                {
                    "code": 401,
                    "message": "로그인된 회원만 이용 가능합니다."
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
public @interface GetReviewListApiDocs {
}
