package com.cyboul.eda.notifservice;

import com.cyboul.eda.common.events.PaymentStatus;
import com.cyboul.eda.common.events.StockRejectedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class StockRejectedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockRejectedEventConsumer.class);

    private final Sinks.Many<NotificationEvent> notificationSink;

    @KafkaListener(topics = "stock-rejected", groupId = "${spring.kafka.consumer.group-id}")
    public void onStockRejected(StockRejectedEvent event) {
        log.warn("NOTIF: Stock rejected for orderId={}, reason={}", event.orderId(), event.reason());

        Sinks.EmitResult result = notificationSink.tryEmitNext(new NotificationEvent("payment-update",
                new OrderOutcomeNotification(null, event.originalOrder(), PaymentStatus.FAILED)));

        if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
            log.warn("NOTIF: Failed to push stock-rejected notification: {}", result);
        }
    }
}
