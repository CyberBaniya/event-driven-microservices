package com.cyboul.eda.paymentservice;

import com.cyboul.eda.common.events.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    private final KafkaTemplate<String, PaymentProceedEvent> kafkaTemplate;
    private final PaymentRecordRepository paymentRecordRepository;

    @KafkaListener(topics = "stock-reserved", groupId = "${spring.kafka.consumer.group-id}")
    public void onStockReserved(StockReservedEvent event, Acknowledgment ack) {
        log.info("PAYMENT: Stock reserved for orderId={}, productId={}, qty={}",
                event.orderId(), event.productId(), event.quantity());

        if (paymentRecordRepository.existsById(event.orderId())) {
            log.warn("PAYMENT: Duplicate delivery for orderId={}, skipping", event.orderId());
            ack.acknowledge();
            return;
        }

        String paymentUuid = UUID.randomUUID().toString();
        PaymentStatus status = processPayment(event.orderId());

        paymentRecordRepository.save(new PaymentRecord(event.orderId(), status, Instant.now()));
        kafkaTemplate.send("payment-proceed", new PaymentProceedEvent(paymentUuid, event.originalOrder(), status));
        ack.acknowledge();
    }

    private PaymentStatus processPayment(String orderId) {
        log.info("PAYMENT: Processing...");
        PaymentStatus status = PaymentStatus.FAILED;
        try {
            TimeUnit.SECONDS.sleep(30);
            status = PaymentStatus.SUCCESS;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("PAYMENT: (!) Processing interrupted for orderId={}", orderId);
        }
        log.info("PAYMENT: Processed for orderId={} status={}", orderId, status.name());
        return status;
    }
}
