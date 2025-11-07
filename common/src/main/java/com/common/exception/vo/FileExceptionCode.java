package com.common.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FileExceptionCode {

	FILE_EMPTY("파일이 비었습니다."),
	FILE_MAX_SIZE("최대 파일 사이즈를 초과하였습니다."),
	NOT_VALID_EXTENSION("지원하는 확장자 파일이 아닙니다."),
	NOT_IMAGE("이미지 파일이 아닙니다."),
	NO_SUCH_IMAGE("해당 이미지를 찾을 수 없습니다.");

	private final String value;

	public String addArgument(String argument) {
		return this.value + ":" + argument;
	}
}
