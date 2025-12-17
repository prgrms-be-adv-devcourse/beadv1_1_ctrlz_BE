package com.domainservice.domain.search.docs.word;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
	summary = "검색어 저장",
	description = """
        사용자가 검색한 검색어를 Redis에 저장하는 API입니다.
        
        ### 주요 기능
        - **최근 검색어 저장**: 사용자 ID가 있으면 개인 최근 검색어로 저장
        
        - **인기 검색어 집계**: 사용자 ID 유무와 관계없이 전체 인기 검색어로 집계
        
        - **AI 이벤트 발행**: 사용자 ID가 있으면 검색 패턴 분석을 위한 이벤트 발행
        
        ### 저장 시점
        - 검색창에서 검색어를 입력하고 검색 버튼 클릭 시
        
        - 자동완성 목록에서 검색어를 선택했을 때
        
        - 검색 결과 화면으로 이동하기 직전
        
        ### 저장 동작
        - **사용자 ID 있음**: 
          - 개인 최근 검색어 목록에 추가
          - AI 추천 시스템에 검색 패턴 전송
          - 전체 인기 검색어 집계에 반영
        
        - **사용자 ID 없음**:
          - 전체 인기 검색어 집계에만 반영
        """,
	parameters = {
		@Parameter(
			name = "X-REQUEST-ID",
			description = "사용자 ID (최근 검색어 저장 및 개인화 추천용, 선택)",
			required = false,
			example = "user-uuid-1234",
			in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER
		)
	}
)
@ApiResponses({
	@ApiResponse(
		responseCode = "201",
		description = "검색어 저장 성공",
		content = @Content(
			examples = {
				@ExampleObject(
					name = "저장 성공 (사용자 ID 있음)",
					description = "최근 검색어 + 인기 검색어 + AI 이벤트 발행",
					value = """
						{
						  "data": null,
						  "message": "검색어 저장 완료"
						}
						"""
				),
				@ExampleObject(
					name = "저장 성공 (사용자 ID 없음)",
					description = "인기 검색어 집계만 수행",
					value = """
						{
						  "data": null,
						  "message": "검색어 저장 완료"
						}
						"""
				)
			}
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
public @interface SaveSearchWordApiDocs {
}
