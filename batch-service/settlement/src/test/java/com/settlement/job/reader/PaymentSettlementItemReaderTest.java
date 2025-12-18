package com.settlement.job.reader;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import com.settlement.common.feign.PaymentFeignClient;
import com.settlement.common.dto.BaseResponse;
import com.settlement.dto.PaymentResponse;
import com.settlement.job.dto.SettlementSourceDto;

@ExtendWith(MockitoExtension.class)
class PaymentSettlementItemReaderTest {

    @Mock
    private PaymentFeignClient paymentFeignClient;

    @InjectMocks
    private PaymentSettlementItemReader reader;

    @Test
    @DisplayName("test1: 결제 데이터를 읽어 SettlementSourceDto 로 변환한다")
    void test1() throws Exception {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 2, 0, 0);
        PaymentResponse payment = new PaymentResponse(
                "pay-1",
                "orderItem-1",
                "user-1",
                new BigDecimal("10000"),
                "SUCCESS",
                LocalDateTime.now(),
                "TOSS");
        given(paymentFeignClient.getPaymentsForSettlement(start, end))
                .willReturn(new BaseResponse<>(List.of(payment), "success"));

        // set job parameters via reflection (since we are not in a StepScope)
        ReflectionTestUtils.setField(reader, "startDateStr", start.toString());
        ReflectionTestUtils.setField(reader, "endDateStr", end.toString());

        // when
        SettlementSourceDto dto = reader.read();

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getUserId()).isEqualTo("user-1");
        assertThat(dto.getPayment()).isEqualTo(payment);
        // second read should return null (no more data)
        assertThat(reader.read()).isNull();
    }
}
