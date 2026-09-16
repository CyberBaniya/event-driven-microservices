package com.cyboul.eda.common.events;

public record OrderCreatedEvent(
        String orderUuid,
        String userId,
        String productId,
        int quantity,
        double amount
) {}