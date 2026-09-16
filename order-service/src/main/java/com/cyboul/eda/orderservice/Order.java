package com.cyboul.eda.orderservice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    private String userId;
    private String productId;
    private int quantity;
    private double amount;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Order(String id, String userId, String productId, int quantity, double amount) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
