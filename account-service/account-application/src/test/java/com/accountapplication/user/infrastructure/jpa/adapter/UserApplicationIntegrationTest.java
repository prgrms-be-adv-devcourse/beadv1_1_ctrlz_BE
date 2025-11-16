package com.accountapplication.user.infrastructure.jpa.adapter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.user.application.adapter.dto.CartCreateCommand;
import com.user.application.adapter.UserApplication;
import com.user.application.adapter.dto.UserContext;
import com.user.application.port.out.ExternalEventPersistentPort;
import com.user.application.port.out.OutboundEventPublisher;
import com.user.application.port.out.UserPersistencePort;
import com.user.domain.model.User;
import com.user.domain.vo.EventType;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApplicationIntegrationTest {

	@Autowired
	private UserApplication userApplication;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@MockitoBean
	private UserPersistencePort userPersistencePort;

	@MockitoBean
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private ExternalEventPersistentPort externalEventPersistentPort;

	@MockitoBean
	private OutboundEventPublisher outboundEventPublisher;

	@DisplayName("사용자 생성 시 트랜잭션 커밋 전에는 이벤트가 발행되지 않는다")
	@Test
	void test1() {
		// given
		UserContext newUserContext = UserContext_생성();
		mock_세팅("testId");

		// when
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		userApplication.create(newUserContext);

		// then
		//커밋되기전에 수행되지 않음.
		verify(externalEventPersistentPort, never()).save(any(), any());

		transactionManager.commit(status);
		//커밋 직전에 수행
		verify(externalEventPersistentPort, times(1)).save(any(), any());
	}

	@DisplayName("사용자 생성 시 트랜잭션 커밋 후에 이벤트가 정상적으로 발행 및 처리된다")
	@Test
	void test2() {
		// given
		UserContext newUserContext = UserContext_생성();
		String expectedUserId = "testId";
		mock_세팅(expectedUserId);

		// when
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		userApplication.create(newUserContext);

		//then
		verify(outboundEventPublisher, never())
			.publish(any(String.class), any(CartCreateCommand.class));
		verify(externalEventPersistentPort, never())
			.completePublish(expectedUserId, EventType.CREATED);

		transactionManager.commit(status);

		verify(outboundEventPublisher, times(1))
			.publish(any(String.class), any(CartCreateCommand.class));
		verify(externalEventPersistentPort, times(1))
			.completePublish(expectedUserId, EventType.CREATED);
	}

	private UserContext UserContext_생성() {
		return UserContext.builder()
			.email("test3@example.com")
			.password("password123")
			.name("테스트유저3")
			.phoneNumber("010-1111-2222")
			.nickname("테스트닉네임3")
			.state("서울특별시")
			.city("강남구")
			.street("테헤란로")
			.zipCode("12345")
			.addressDetails("103호")
			.build();
	}

	private void mock_세팅(String expectedUserId) {
		User savedUser = User.builder()
			.id(expectedUserId)
			.email("test2@example.com")
			.password("password123")
			.name("테스트유저2")
			.phoneNumber("010-9876-5432")
			.nickname("테스트닉네임2")
			.build();

		when(userPersistencePort.existsNickname(any())).thenReturn(false);
		when(userPersistencePort.existsPhoneNumber(any())).thenReturn(false);
		when(passwordEncoder.encode(any())).thenReturn("encoded-password");
		when(userPersistencePort.save(any(User.class))).thenReturn(savedUser);
	}
}
