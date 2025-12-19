package com.paymentservice.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.model.enums.PayType;
import com.paymentservice.payment.model.enums.PaymentStatus;
import com.paymentservice.payment.repository.PaymentRepository;

@SpringBootTest
public class DummyPaymentDataTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private final Random random = new Random();

    @Test
    @DisplayName("정산 테스트를 위한 더미 결제 데이터 500개 생성")
    @Transactional
    @Rollback(false)
    void generateDummyPayments() {
        for (int i = 0; i < 500; i++) {
            String userId = "user-" + (random.nextInt(10) + 1);
            String orderId = "order-" + UUID.randomUUID().toString().substring(0, 8);
            BigDecimal amount = BigDecimal.valueOf((random.nextInt(10) + 1) * 10000);

            PayType[] payTypes = PayType.values();
            PayType payType = payTypes[random.nextInt(payTypes.length)];

            BigDecimal depositAmount = BigDecimal.ZERO;
            BigDecimal tossAmount = amount;

            if (payType == PayType.DEPOSIT) {
                depositAmount = amount;
                tossAmount = BigDecimal.ZERO;
            } else if (payType == PayType.DEPOSIT_TOSS) {
                depositAmount = amount.divide(BigDecimal.valueOf(2));
                tossAmount = amount.subtract(depositAmount);
            }

            PaymentEntity payment = PaymentEntity.of(
                    userId,
                    orderId,
                    amount,
                    depositAmount,
                    tossAmount,
                    "KRW",
                    payType,
                    PaymentStatus.SUCCESS,
                    "paykey-" + UUID.randomUUID().toString().substring(0, 8),
                    OffsetDateTime.now().minusHours(random.nextInt(24)));

            paymentRepository.save(payment);
        }
        System.out.println("500 dummy payments generated successfully.");
    }
}
