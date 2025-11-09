package com.userservice.infrastructure.sms.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.common.exception.CustomException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;

class SmsClientAdapterTest {

	private final DefaultMessageService messageService = mock(DefaultMessageService.class);

	@DisplayName("sms를 전송할 수 있다.")
	@Test
	void test1() throws Exception {
		//given
		//when
		SmsClientAdapter smsClientAdapter = new SmsClientAdapter(messageService);
		smsClientAdapter.send("010-1111-1111", "code");

		//then
		verify(messageService).send(any(Message.class));
	}

	@DisplayName("sms api 호출 전체 횟수 10회 제한(실제 서비스 아니므로)")
	@Test
	void test2() throws Exception {
		SmsClientAdapter smsClientAdapter = new SmsClientAdapter(messageService);

		for (int i = 0; i < 10; i++) {
			assertThatCode(() -> smsClientAdapter.send("010-1111-1111", "code")).doesNotThrowAnyException();
		}

		assertThatThrownBy(() -> smsClientAdapter.send("010-1111-1111", "code")  )
			.isInstanceOf(CustomException.class);

	}

}