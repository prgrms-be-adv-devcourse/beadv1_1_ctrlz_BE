// package com.settlement.job.processor;
//
// import com.settlement.domain.entity.Settlement;
// import com.settlement.domain.model.Payment;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.batch.item.ItemProcessor;
//
// import java.math.BigDecimal;
//
// @Slf4j
// public class PaymentToSettlementProcessor implements ItemProcessor<Payment, Settlement> {
//
//     // 임시 수수료율 3%
//     private static final BigDecimal FEE_RATE = BigDecimal.valueOf(0.03);
//
//     @Override
//     public Settlement process(Payment payment) {
//         log.debug("Processing payment: {}", payment.getPaymentKey());
//
//         BigDecimal fee = payment.getAmount().multiply(FEE_RATE);
//         BigDecimal netAmount = payment.getAmount().subtract(fee);
//
//         return Settlement.create(
//                 payment.getOrderId(),
//                 payment.getUserId(),
//                 payment.getAmount(),
//                 fee,
//                 netAmount);
//     }
// }
