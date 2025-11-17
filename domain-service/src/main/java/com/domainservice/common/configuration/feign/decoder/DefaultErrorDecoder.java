package com.domainservice.common.configuration.feign.decoder;

import com.common.feign.exception.decoder.AbstractErrorDecoder;

import feign.Response;

public class DefaultErrorDecoder extends AbstractErrorDecoder {

	@Override
	public Exception decode(String methodKey, Response response) {
		return super.decode(methodKey, response);
	}
}
