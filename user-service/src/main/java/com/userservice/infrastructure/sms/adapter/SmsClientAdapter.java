package com.userservice.infrastructure.sms.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.common.exception.CustomException;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import com.userservice.application.port.out.SellerVerificationClient;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class SmsClientAdapter implements SellerVerificationClient {

	@Value("${coolsms.apiKey}")
	private String apiKey;

	@Value("${coolsms.from}")
	private String from;

	@Value("${coolsms.secretKey}")
	private String secretKey;

	private DefaultMessageService messageService;

	@PostConstruct
	public void init() {
		messageService = SolapiClient.INSTANCE.createInstance(apiKey, secretKey);
	}

	@Override
	public void send(String phoneNumber, String verificationCode) {
		Message message = new Message();
		message.setFrom(from);
		message.setTo(phoneNumber.replaceAll("-", ""));
		message.setText(
			"""
				[연근마켓]
				본인 확인 인증 번호 : %s
				""".formatted(verificationCode)
		);

		try {
			messageService.send(message);
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}
}
