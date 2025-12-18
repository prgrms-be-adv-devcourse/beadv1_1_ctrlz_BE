package com.domainservice.domain.search.docs.word;

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
	summary = "실시간 인기 검색어 TOP 10",
	description = """
        Redis 기반 실시간 인기 검색어 상위 10개를 조회하는 API입니다.
        
        - 전체 사용자의 검색 빈도를 집계하여 인기순으로 정렬
        - 검색 점수(빈도)가 높은 순서대로 최대 10개 반환
        - 각 검색어의 QWERTY 변환값도 함께 제공
        """
)
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "조회 성공",
		content = @Content(
			examples = {
				@ExampleObject(
					name = "인기 검색어 TOP 10",
					description = "실시간 검색 빈도 기반 상위 10개",
					value = """
						{
						  "data": [
						    {
						      "word": "아이폰 15",
						      "qwertyInput": "dldlvhs 15"
						    },
						    {
						      "word": "맥북 에어",
						      "qwertyInput": "aoRqkr dpdlf"
						    },
						    {
						      "word": "갤럭시 S24",
						      "qwertyInput": "rofffbtl S24"
						    },
						    {
						      "word": "에어팟 프로",
						      "qwertyInput": "dpldlvhf vmfh"
						    },
						    {
						      "word": "아이패드",
						      "qwertyInput": "dldlvodzep"
						    },
						    {
						      "word": "닌텐도 스위치",
						      "qwertyInput": "sletpsej tmndlcl"
						    },
						    {
						      "word": "플레이스테이션",
						      "qwertyInput": "vmffpdldlrtmdldldy"
						    },
						    {
						      "word": "다이슨 청소기",
						      "qwertyInput": "ekdldy cjddthrl"
						    },
						    {
						      "word": "삼성 노트북",
						      "qwertyInput": "tkatjd shxmrmr"
						    },
						    {
						      "word": "무선 이어폰",
						      "qwertyInput": "ahtjs dldjavhs"
						    }
						  ],
						  "message": ""
						}
						"""
				),
				@ExampleObject(
					name = "검색어 없음",
					description = "집계된 검색어가 없는 경우",
					value = """
						{
						  "data": [],
						  "message": ""
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
public @interface GetTrendSearchWordsApiDocs {
}