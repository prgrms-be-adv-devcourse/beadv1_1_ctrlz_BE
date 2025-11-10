package com.userservice.infrastructure.feign.exception.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.common.exception.feign.YeongeunFeignClientException;

@Slf4j
@Component
public class DefaultFeignClientErrorDecoder implements ErrorDecoder {

	@Override
	public Exception decode(String methodKey, Response response) {
		int status = response.status();
		log.error("[Feign Error] methodKey={}, status={}, reason={}", methodKey, status, response.reason());

		return switch (status) {
			case 400 -> new YeongeunFeignClientException.BadRequest("잘못된 요청입니다.");
			case 404 -> new YeongeunFeignClientException.NotFound("리소스를 찾을 수 없습니다.");
			case 500 -> new YeongeunFeignClientException.InternalServerError("서버 내부 오류가 발생했습니다.");
			default -> new YeongeunFeignClientException.CustomError(
				HttpStatus.resolve(status),
				response.reason()
			);
		};
	}
}
