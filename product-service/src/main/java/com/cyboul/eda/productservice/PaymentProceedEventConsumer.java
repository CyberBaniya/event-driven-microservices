package com.cyboul.eda.productservice;

import com.cyboul.eda.common.events.PaymentProceedEvent;
import com.cyboul.eda.common.events.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProceedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentProceedEventConsumer.class);

    private final ReactiveMongoTemplate mongoTemplate;

    @KafkaListener(topics = "payment-proceed", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentProceed(PaymentProceedEvent event) {
        if (event.status() == PaymentStatus.SUCCESS) {
            log.info("PRODUCT: No stock release needed for orderId={} (status={})",
                    event.order().orderUuid(), event.status());
            return;
        }

        // Payment failed after stock was reserved — release it back.
        String productId = event.order().productId();
        int quantity = event.order().quantity();

        mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id").is(productId)),
                    new Update().inc("stock", quantity),
                    Product.class
                )
                .block();

        log.info("PRODUCT: Stock released for productId={} (orderId={})", productId, event.order().orderUuid());
    }
}
