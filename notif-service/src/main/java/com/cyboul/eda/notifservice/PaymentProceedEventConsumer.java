package com.cyboul.eda.notifservice;

import com.cyboul.eda.common.events.PaymentProceedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class PaymentProceedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentProceedEventConsumer.class);

    private final Sinks.Many<NotificationEvent> notificationSink;
    private final UserContactRepository userContactRepository;

    @KafkaListener(topics = "payment-proceed", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentProceed(PaymentProceedEvent event) {
        log.info("NOTIF: Received PaymentProceedEvent, paymentId={}, status={}", event.paymentUuid(), event.status());

        switch (event.status()) {
            case FAILED, CANCELLED -> notifyFrontEnd(event);
            case SUCCESS -> {
                notifyFrontEnd(event);
                sendOrderConfirmationEmailToCustomer(event);
            }
            default -> log.warn("NOTIF: Unhandled PaymentStatus: {}", event.status());
        }
    }

    private void sendOrderConfirmationEmailToCustomer(PaymentProceedEvent event) {
        String userId = event.order().userId();
        UserContactProjection contact = userContactRepository.findById(userId).block();

        if (contact == null) {
            log.warn("NOTIF: No user contact found for userId={}, cannot send confirmation email", userId);
            return;
        }

        // contact.getName() & contact.getEmail() can be used, but don't log !
        log.info("NOTIF: Sending Order confirmation email to {} \n" +
                 "---------------------------------------\n" +
                 "Transaction: {}\nOrder: {}\n" +
                 "Order details: { Product: {} x{} for {} }\n" +
                 "Customer: {}\nCustomer Address: {}\n" +
                 "---------------------------------------",
                 userId, event.paymentUuid(), event.order().orderUuid(),
                 event.order().productId(), event.order().quantity(), event.order().amount(),
                 contact.getUserId(), "TODO");
    }

    private void notifyFrontEnd(PaymentProceedEvent event) {
        log.info("NOTIF: Notifying Frontend for order={}, status={}", event.order().orderUuid(), event.status());

        Sinks.EmitResult result = notificationSink.tryEmitNext(new NotificationEvent("payment-update",
                new OrderOutcomeNotification(event.paymentUuid(), event.order(), event.status())));

        if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
            log.warn("NOTIF: Failed to push notification to frontend: {}", result);
        }
    }
}
