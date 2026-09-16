package com.cyboul.eda.orderservice;

import com.cyboul.eda.common.events.PaymentProceedEvent;
import com.cyboul.eda.common.events.PaymentStatus;
import com.cyboul.eda.common.events.StockRejectedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusConsumer.class);
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment-proceed", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentProceed(PaymentProceedEvent event) {
        String orderId = event.order().orderUuid();
        OrderStatus status = event.status() == PaymentStatus.SUCCESS ? OrderStatus.PAID : OrderStatus.FAILED;
        updateStatus(orderId, status);
    }

    @KafkaListener(topics = "stock-rejected", groupId = "${spring.kafka.consumer.group-id}")
    public void onStockRejected(StockRejectedEvent event) {
        updateStatus(event.orderId(), OrderStatus.CANCELLED);
    }

    private void updateStatus(String orderId, OrderStatus status) {
        orderRepository.findById(orderId)
                .ifPresentOrElse(order -> {
                    order.setStatus(status);
                    orderRepository.save(order);
                    log.info("ORDER: orderId={} status updated to {}", orderId, status);
                },() -> log.warn("ORDER: orderId={} not found for status update to {}", orderId, status));
    }
}
