package com.cyboul.eda.orderservice;

import com.cyboul.eda.common.events.OrderCreatedEvent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody OrderCreation order) {
        log.info("ORDER: API Received orderId={}, => Sending 'order-created' event", order.uuid);

        orderRepository.save(new Order(order.uuid(), order.userId(), order.productId(), order.quantity(), order.amount()));

        kafkaTemplate.send("order-created", new OrderCreatedEvent(
                order.uuid(),
                order.userId(),
                order.productId(),
                order.quantity(),
                order.amount()
        ));
        return ResponseEntity.ok("Order created");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Order>> getByUser(@RequestParam String userId) {
        return ResponseEntity.ok(orderRepository.findByUserId(userId));
    }

    public record OrderCreation(
            @NotBlank String uuid,
            @NotBlank String userId,
            @NotBlank String productId,
            @Positive int quantity,
            @Positive double amount
    ) {}
}
