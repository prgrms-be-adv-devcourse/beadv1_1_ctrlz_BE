package com.aiservice.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.aiservice.controller.dto.BaseResponse;

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
    summary = "AI 상품 추천 요청 (Hybrid Search + LLM)",
    description = """
        사용자의 질문(Query)을 분석하여 하이브리드 검색을 수행하고, LLM을 통해 상품 추천 메시지를 생성합니다.
        
        ### 처리 과정
        1. **제한 체크**: 사용자의 일일 추천 요청 횟수(`custom.recommendation.limit`) 도달 여부를 확인합니다.
        2. **하이브리드 검색**: Vector Search와 Keyword Search를 결합하여 관련 상품 문서를 검색합니다.
        3. **프롬프트 생성**: 검색된 상품 정보와 사용자 쿼리를 조합하여 LLM 프롬프트를 구성합니다.
        4. **결과 반환**: 상품 링크가 포함된 추천 메시지를 반환하고, 세션 서비스에 이력을 저장합니다.
        """
)
@Parameters({
    @Parameter(
        name = "query",
        description = "사용자 검색어 (예: 가성비 좋은 아이폰 추천해줘)",
        required = true,
        in = ParameterIn.QUERY,
        schema = @Schema(type = "string")
    ),
    @Parameter(
        name = "X-REQUEST-ID",
        description = "사용자 식별 ID (UUID)",
        required = true,
        in = ParameterIn.HEADER,
        schema = @Schema(type = "string", example = "user-uuid-1234")
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "요청 처리 성공 (결과 상태에 따라 분기)",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = {
                @ExampleObject(
                    name = "성공 (OK)",
                    description = "추천 상품을 찾고 LLM 메시지 생성에 성공한 경우",
                    value = """
                        {
                          "data": {
                            "status": "OK",
                            "message": "안녕하세요! :blush: 아이폰을 찾고 계시군요! 아래는 추천드리는 아이폰 제품들입니다.\\n\\n1. [아이폰 14 (2228000원)](http://domain-svc:8081/api/products/019b28d5-4103-7a98-94d3-da1cf1bb6f10)\\n2. [아이폰 13 (2186000원)](http://domain-svc:8081/api/products/019b28d5-4103-7cfe-94d5-8cf8f9f46880)\\n3. [아이폰 SE (1517000원)](http://domain-svc:8081/api/products/019b28d5-4106-710d-a08d-095ebd1e3afe)\\n\\n좋은 선택 되시길 바랍니다! :sparkles:"
                          },
                          "message": "추천 생성 요청이 처리되었습니다."
                        }
                        """
                ),
                @ExampleObject(
                    name = "제한 도달 (LIMIT_REACHED)",
                    description = "설정된 추천 횟수 제한을 초과한 경우",
                    value = """
                        {
                          "data": {
                            "status": "LIMIT_REACHED",
                            "message": "추천 횟수 제한에 도달했습니다."
                          },
                          "message": "추천 생성 요청이 처리되었습니다."
                        }
                        """
                ),
                @ExampleObject(
                    name = "결과 없음 (NO_RESULTS)",
                    description = "검색 결과가 없거나 적절한 추천을 찾지 못한 경우",
                    value = """
                        {
                          "data": {
                            "status": "NO_RESULTS",
                            "message": "추천 상품을 찾지 못했습니다."
                          },
                          "message": "추천 생성 요청이 처리되었습니다."
                        }
                        """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (필수 파라미터 누락 등)",
        content = @Content(schema = @Schema(hidden = true))
    ),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류 (LLM 연동 실패 등)",
        content = @Content(schema = @Schema(hidden = true))
    )
})
public @interface TriggerRecommendationApiDocs {
}