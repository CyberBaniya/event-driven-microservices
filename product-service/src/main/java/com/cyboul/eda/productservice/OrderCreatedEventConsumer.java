package com.cyboul.eda.productservice;

import com.cyboul.eda.common.events.OrderCreatedEvent;
import com.cyboul.eda.common.events.StockRejectedEvent;
import com.cyboul.eda.common.events.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    private final ReactiveMongoTemplate mongoTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-created", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("PRODUCT: Attempting stock reservation for orderId={}, productId={}, qty={}",
                event.orderUuid(), event.productId(), event.quantity());

        Query selectProductByIdWithEnoughStock = Query.query(Criteria
                .where("_id").is(event.productId())
                .and("stock").gte(event.quantity()));

        UpdateDefinition updateProductStock = new Update()
                .inc("stock", -event.quantity());

        mongoTemplate
                .updateFirst(selectProductByIdWithEnoughStock, updateProductStock, Product.class)
                .flatMap(result -> {
                    if (result.getModifiedCount() > 0) {
                        log.info("PRODUCT: Stock reserved for orderId={}", event.orderUuid());
                        return Mono.fromFuture(kafkaTemplate.send("stock-reserved",
                                new StockReservedEvent(event.orderUuid(), event.productId(), event.quantity(), event)));
                    } else {
                        log.warn("PRODUCT: Insufficient stock for orderId={}, productId={}",
                                event.orderUuid(), event.productId());
                        return Mono.fromFuture(kafkaTemplate.send("stock-rejected",
                                new StockRejectedEvent(event.orderUuid(), event.productId(),
                                        "Insufficient stock", event)));
                    }
                })
                .block();
    }
}
