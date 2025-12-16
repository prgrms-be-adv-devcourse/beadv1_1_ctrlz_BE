package com.domainservice.domain.post.category.api.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
	summary = "카테고리 목록 조회",
	description = """
        등록 되어있는 모든 카테고리 목록을 조회합니다.
        """
)
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "카테고리 목록 조회 성공"
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
public @interface GetCategoriesApiDocs {
}