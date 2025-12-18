package com.settlement.job.processor;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.settlement.dto.PaymentResponse;
import com.settlement.dto.PaymentResponse;
import com.settlement.domain.entity.Settlement;
import com.settlement.domain.entity.SettlementStatus;
import com.settlement.job.dto.SettlementSourceDto;

@DisplayName("SettlementCreateProcessor 테스트")
class SettlementCreateProcessorTest {

        private final SettlementCreateProcessor processor = new SettlementCreateProcessor();

        @Test
        @DisplayName("test1: SUCCESS 상태 결제는 Settlement 엔티티로 변환한다")
        void test1() throws Exception {
                // given
                PaymentResponse payment = new PaymentResponse(
                                "pay-1",
                                "orderItem-1",
                                "user-1",
                                new BigDecimal("10000"),
                                "SUCCESS",
                                LocalDateTime.now(),
                                "TOSS");
                SettlementSourceDto source = SettlementSourceDto.builder()
                                .userId("user-1")
                                .payment(payment)
                                .build();

                // when
                Settlement settlement = processor.process(source);

                // then
                assertThat(settlement).isNotNull();
                assertThat(settlement.getUserId()).isEqualTo("user-1");
                assertThat(settlement.getOrderId()).isEqualTo("orderItem-1");
                assertThat(settlement.getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
                assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.PENDING);
                assertThat(settlement.getPayType().name()).isEqualTo("TOSS");
        }

        @Test
        @DisplayName("test2: SUCCESS가 아닌 상태는 null 반환한다")
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
                Settlement result = processor.process(source);

                // then
                assertThat(result).isNull();
        }
}
