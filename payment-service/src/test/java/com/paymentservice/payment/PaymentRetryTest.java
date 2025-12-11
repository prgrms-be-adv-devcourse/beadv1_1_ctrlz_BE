package com.paymentservice.payment;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach; // 변경됨
import org.junit.jupiter.api.BeforeEach; // 변경됨
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.paymentservice.payment.client.PaymentTossClient;
import com.paymentservice.payment.exception.PaymentGatewayFailedException;
import com.paymentservice.payment.model.dto.PaymentConfirmRequest;
import com.paymentservice.payment.model.dto.TossApprovalResponse;
import com.paymentservice.payment.model.enums.PaymentStatus;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(properties = {
    // 테스트 속도를 위해 재시도 대기 시간을 줄임 (필요 시 유지)
    "toss.retry.delay=10",
    "toss.retry.max-attempts=3"
})
@ActiveProfiles("test")
public class PaymentRetryTest {

    private MockWebServer mockWebServer; // static 제거
    private static final int PORT = 8888; // 포트 고정 (중요)

    @Autowired
    private PaymentTossClient paymentTossClient;

    private PaymentConfirmRequest request;

    // @BeforeAll 제거됨

    // Spring이 뜰 때 고정된 포트(8888)를 바라보도록 설정
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("custom.payment.toss.targetUrl",
            () -> "http://localhost:" + PORT
        );
    }

    @BeforeEach
    void setUp() throws IOException {
        // 매 테스트마다 새로운 서버 인스턴스 생성 및 시작
        mockWebServer = new MockWebServer();
        mockWebServer.start(PORT); // 위에서 설정한 8888 포트로 시작

        request = new PaymentConfirmRequest(
            "paymentKey-1",
            "order-1",
            BigDecimal.valueOf(10000),
            BigDecimal.ZERO,
            BigDecimal.valueOf(10000)
        );
    }

    @AfterEach // @AfterAll -> @AfterEach로 변경
    void tearDown() throws IOException {
        // 매 테스트가 끝나면 서버를 종료 (카운트 및 큐 초기화 효과)
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("결제 승인 성공한다")
    void test1() {
        // given
        String successBody = """
                {
                    "status": "DONE",
                    "paymentKey": "test_payment_key_123",
                    "orderId": "%s",
                    "currency": "KRW",
                    "approvedAt": "2025-12-09T12:00:00+09:00",
                    "totalAmount": 10000
                }
            """.formatted(request.orderId());

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(successBody)
            .addHeader("Content-Type", "application/json"));

        // when
        TossApprovalResponse result = paymentTossClient.approve(request);

        // then
        assertThat(result.paymentKey()).isEqualTo("test_payment_key_123");
        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1); // 카운트 확인
    }

    @Test
    @DisplayName("서버가 4번 실패해도 클라이언트는 설정된 3번까지만 시도하고 포기한다")
    void test2() {
        // given - 서버는 깨끗한 상태에서 시작됨 (Count: 0)
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // when
        assertThatThrownBy(() -> paymentTossClient.approve(request))
            .isInstanceOf(PaymentGatewayFailedException.class);

        // then - 누적되지 않았으므로 정확히 3이 나옴
        assertThat(mockWebServer.getRequestCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("2번 실패 후 3번째 재시도에서 성공하면 정상적으로 응답을 반환한다")
    void test3() throws Exception {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        String successBody = """
                {
                    "status": "DONE",
                    "paymentKey": "test_payment_key_123",
                    "orderId": "%s",
                    "currency": "KRW",
                    "approvedAt": "2025-12-09T12:00:00+09:00",
                    "totalAmount": 10000
                }
            """.formatted(request.orderId());

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(successBody)
            .addHeader("Content-Type", "application/json"));

        // when
        TossApprovalResponse response = paymentTossClient.approve(request);

        // then
        assertThat(mockWebServer.getRequestCount()).isEqualTo(3);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}