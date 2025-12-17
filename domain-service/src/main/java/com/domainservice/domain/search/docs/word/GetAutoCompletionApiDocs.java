package com.domainservice.domain.search.docs;

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
	summary = "검색어 자동완성",
	description = """
        사용자가 입력한 검색어 prefix를 기반으로 자동완성 검색어 목록을 제공하는 API입니다.
        
        ### 주요 기능
        - **Prefix 매칭**: 입력한 단어로 시작하는 검색어 추천 (최대 20개)
        
        - **QWERTY 오타 교정**: 영문 자판으로 입력된 한글을 교정하여 검색
          - 예시: `dldlvhs` → `아이폰`으로 변환하여 검색
          - 한글-영문 자판 위치 매핑을 통한 자동 교정
        
        - **다중 매칭 전략**:
          1. 입력값 그대로 검색 (originValue)
          2. QWERTY를 한글로 변환한 값으로 검색 (convertedKorean)
          3. QWERTY 입력값으로 직접 검색 (qwertyInput)
          4. 위에서 결과가 없으면 QWERTY 단건 매칭 후 재검색
        
        - **검색어 미입력 시**:
          - **사용자 ID 있음**: 최근 검색어 목록 반환
          - **사용자 ID 없음**: 실시간 트렌드 검색어 반환
        
        ### 응답 필드 설명
        - `word`: 검색어 원문 (한글)
        - `qwertyInput`: 해당 검색어의 QWERTY 변환값 (오타 매칭용)
        
        ### 사용 예시
        - 검색창 실시간 자동완성 드롭다운
        - 입력 오타 자동 교정
        - 빈 검색창 클릭 시 추천 검색어 표시
        """,
	parameters = {
		@Parameter(
			name = "prefix",
			description = "검색창에 입력한 단어 (한글, 영문 지원)",
			required = false,
			example = "아이폰"
		),
		@Parameter(
			name = "X-REQUEST-ID",
			description = "사용자 ID (prefix가 없을 때 최근 검색어 조회용, 선택)",
			required = false,
			example = "user-uuid-1234",
			in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER
		)
	}
)
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "자동완성 성공",
		content = @Content(
			examples = {
				@ExampleObject(
					name = "한글 입력 자동완성",
					description = "'아이'로 시작하는 검색어 자동완성 (최대 20개)",
					value = """
						{
						  "data": [
						    {
						      "word": "아이폰",
						      "qwertyInput": "dldlvhs"
						    },
						    {
						      "word": "아이폰 15",
						      "qwertyInput": "dldlvhs 15"
						    },
						    {
						      "word": "아이폰 15 프로",
						      "qwertyInput": "dldlvhs 15 vmfh"
						    },
						    {
						      "word": "아이폰 케이스",
						      "qwertyInput": "dldlvhs zjdlfmtm"
						    },
						    {
						      "word": "아이패드",
						      "qwertyInput": "dldlvodzep"
						    },
						    {
						      "word": "아이패드 프로",
						      "qwertyInput": "dldlvodzep vmfh"
						    },
						    {
						      "word": "아이맥",
						      "qwertyInput": "dldlakr"
						    }
						  ],
						  "message": "검색어 자동완성 리스트 응답 성공"
						}
						"""
				),
				@ExampleObject(
					name = "QWERTY 오타 교정",
					description = "'dldlvhs' (아이폰의 QWERTY 오타) 입력 시 자동 교정",
					value = """
						{
						  "data": [
						    {
						      "word": "아이폰",
						      "qwertyInput": "dldlvhs"
						    },
						    {
						      "word": "아이폰 15",
						      "qwertyInput": "dldlvhs 15"
						    },
						    {
						      "word": "아이폰 15 프로",
						      "qwertyInput": "dldlvhs 15 vmfh"
						    },
						    {
						      "word": "아이폰 케이스",
						      "qwertyInput": "dldlvhs zjdlfmtm"
						    }
						  ],
						  "message": "검색어 자동완성 리스트 응답 성공"
						}
						"""
				),
				@ExampleObject(
					name = "검색어 미입력 - 최근 검색어",
					description = "prefix 없고 사용자 ID 있을 때: 최근 검색어 반환",
					value = """
						{
						  "data": [
						    {
						      "word": "맥북 프로",
						      "qwertyInput": "aoRqkr vmfh"
						    },
						    {
						      "word": "갤럭시 북",
						      "qwertyInput": "rofffbtl qnr"
						    },
						    {
						      "word": "아이패드",
						      "qwertyInput": "dldlvodzep"
						    }
						  ],
						  "message": "검색어 자동완성 리스트 응답 성공"
						}
						"""
				),
				@ExampleObject(
					name = "검색어 미입력 - 트렌드 검색어",
					description = "prefix 없고 사용자 ID 없을 때: 실시간 트렌드 검색어 반환",
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
						    }
						  ],
						  "message": "검색어 자동완성 리스트 응답 성공"
						}
						"""
				),
				@ExampleObject(
					name = "검색어 없음",
					description = "매칭되는 검색어가 없는 경우",
					value = """
						{
						  "data": [],
						  "message": "검색어 자동완성 리스트 응답 성공"
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
public @interface GetAutoCompletionApiDocs {
}