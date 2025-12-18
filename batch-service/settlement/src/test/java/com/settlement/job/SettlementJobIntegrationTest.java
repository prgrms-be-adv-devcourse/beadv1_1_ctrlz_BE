package com.settlement.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.settlement.common.dto.BaseResponse;
import com.settlement.common.feign.PaymentFeignClient;
import com.settlement.dto.PaymentResponse;
import com.settlement.domain.entity.Settlement;
import com.settlement.domain.entity.SettlementStatus;
import com.settlement.repository.SettlementRepository;

@SpringBatchTest
@SpringBootTest(classes = { com.settlement.TestSettlementApplication.class })
@ActiveProfiles("test")
@DisplayName("정산 배치 통합 테스트")
public class SettlementJobIntegrationTest {

        @Autowired
        private JobLauncherTestUtils jobLauncherTestUtils;

        @Autowired
        private SettlementRepository settlementRepository;

        @Autowired
        private Job settlementJob;

        @MockBean
        private PaymentFeignClient paymentFeignClient;

        @Test
        @DisplayName("test1: 결제 데이터를 가져와서 정산 데이터를 생성하고 수수료를 계산한다")
        void test1() throws Exception {
                // given
                LocalDateTime now = LocalDateTime.now();
                String paymentId = "payment1";
                String orderItemId = "item1";
                String userId = "user1";
                BigDecimal amount = new BigDecimal("10000");

                // TOSS 결제 -> 수수료 3%
                PaymentResponse paymentResponse = new PaymentResponse(paymentId, orderItemId, userId, amount, "SUCCESS",
                                now, "TOSS");

                when(paymentFeignClient.getPaymentsForSettlement(any(), any()))
                                .thenReturn(new BaseResponse<>(List.of(paymentResponse), "SUCCESS"));

                JobParameters jobParameters = new JobParametersBuilder()
                                .addString("startDate", now.minusDays(1).toString())
                                .addString("endDate", now.toString())
                                .toJobParameters();

                jobLauncherTestUtils.setJob(settlementJob);

                // when
                JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

                // then
                assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

                List<Settlement> settlements = settlementRepository.findAll();
                assertThat(settlements).hasSize(1);

                Settlement settlement = settlements.get(0);
                assertThat(settlement.getOrderId()).isEqualTo(orderItemId);
                assertThat(settlement.getUserId()).isEqualTo(userId);
                assertThat(settlement.getAmount()).isEqualByComparingTo(amount);

                // Fee Calculation Check (TOSS: 3% of 10000 = 300)
                BigDecimal expectedFee = new BigDecimal("300");
                assertThat(settlement.getFee()).isEqualByComparingTo(expectedFee);

                // Net Amount Check (10000 - 300 = 9700)
                BigDecimal expectedNet = new BigDecimal("9700");
                assertThat(settlement.getNetAmount()).isEqualByComparingTo(expectedNet);

                // PayType Check
                assertThat(settlement.getPayType().name()).isEqualTo("TOSS");

                assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETED);
        }
}
