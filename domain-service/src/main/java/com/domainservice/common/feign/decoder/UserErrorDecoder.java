package com.domainservice.common.feign.decoder;

import org.springframework.http.HttpStatus;

import com.common.feign.exception.YeongeunFeignClientException;
import com.domainservice.common.feign.exception.UserClientException;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserErrorDecoder implements ErrorDecoder {

	@Override
	public Exception decode(String methodKey, Response response) {
		int status = response.status();
		log.error("[Feign Error - User] methodKey={}, status={}, reason={}", methodKey, status, response.reason());
		log.error("[Feign Error - User] response = {}",response);

		return switch (status) {
			case 400 -> new UserClientException.BadRequest("잘못된 요청입니다.");
			case 401 -> new UserClientException.Unauthorized(response.toString());
			case 404 -> new UserClientException.NotFound(response.reason());
			case 429 -> new UserClientException.TooManyRequests("너무 많은 요청을 보냅니다. 잠시 후 다시 실행하세요.");
			case 500 -> new UserClientException.InternalServerError("서버 내부 오류가 발생했습니다.");
			default -> new UserClientException.CustomError(
				HttpStatus.resolve(status),
				response.reason()
			);
		};
	}
}
