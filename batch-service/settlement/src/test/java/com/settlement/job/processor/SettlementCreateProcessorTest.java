package com.settlement.job.processor;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.settlement.dto.PaymentResponse;
import com.settlement.job.dto.SettlementModel;
import com.settlement.job.dto.SettlementSourceDto;

@DisplayName("SettlementCreateProcessor 테스트")
class SettlementCreateProcessorTest {

        private final SettlementCreateProcessor processor = new SettlementCreateProcessor();

        @Test
        @DisplayName("test1: PAID 상태 결제는 SettlementModel 로 변환한다")
        void test1() throws Exception {
                // given
                PaymentResponse payment = new PaymentResponse(
                                "pay-1",
                                "orderItem-1",
                                "user-1",
                                new BigDecimal("10000"),
                                "PAID",
                                LocalDateTime.now(),
                                "TOSS");
                SettlementSourceDto source = SettlementSourceDto.builder()
                                .userId("user-1")
                                .payment(payment)
                                .build();

                // when
                SettlementModel model = processor.process(source);

                // then
                assertThat(model).isNotNull();
                assertThat(model.getUserId()).isEqualTo("user-1");
                assertThat(model.getOrderItemId()).isEqualTo("orderItem-1");
                assertThat(model.getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
                assertThat(model.getStatus()).isEqualTo("PENDING");
                assertThat(model.getPayType()).isEqualTo("TOSS");
        }

        @Test
        @DisplayName("test2: PAID가 아닌 상태는 null 반환한다")
        void test2() throws Exception {
                // given
                PaymentResponse payment = new PaymentResponse(
                                "pay-2",
                                "orderItem-2",
                                "user-2",
                                new BigDecimal("20000"),
                                "UNPAID",
                                LocalDateTime.now(),
                                "DEPOSIT");
                SettlementSourceDto source = SettlementSourceDto.builder()
                                .userId("user-2")
                                .payment(payment)
                                .build();

                // when
                SettlementModel result = processor.process(source);

                // then
                assertThat(result).isNull();
        }
}
