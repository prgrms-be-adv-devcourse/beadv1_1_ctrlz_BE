// package com.settlement.job.reader;
//
// import java.util.Collections;
// import java.util.List;
//
// import org.springframework.batch.item.ItemReader;
// import org.springframework.stereotype.Component;
//
// import com.settlement.domain.model.Payment;
// import com.settlement.domain.service.PaymentService;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// /**
//  * Custom ItemReader that delegates data fetching to PaymentService.
//  * Fetches completed payments in bulk.
//  */
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class PaymentServiceItemReader implements ItemReader<Payment> {
//
//     private final PaymentService paymentService;
//     private final int pageSize;
//
//     private int page = 0;
//     private List<Payment> buffer = Collections.emptyList();
//     private int nextIndex = 0;
//
//     @Override
//     public Payment read() throws Exception {
//         if (isBufferExhausted()) {
//             fetchMoreData();
//         }
//
//         if (isBufferExhausted()) {
//             return null; // End of data
//         }
//
//         return buffer.get(nextIndex++);
//     }
//
//     private boolean isBufferExhausted() {
//         return nextIndex >= buffer.size();
//     }
//
//     private void fetchMoreData() {
//         log.debug("Buffer exhausted. Fetching next page from PaymentService. Page: {}, Size: {}", page, pageSize);
//
//         List<Payment> fetchedPayments = paymentService.getCompletedPayments(page, pageSize);
//
//         if (fetchedPayments == null || fetchedPayments.isEmpty()) {
//             log.debug("No more data received/empty list.");
//             buffer = Collections.emptyList();
//         } else {
//             log.debug("Fetched {} payments.", fetchedPayments.size());
//             buffer = fetchedPayments;
//             nextIndex = 0;
//             page++;
//         }
//     }
// }
