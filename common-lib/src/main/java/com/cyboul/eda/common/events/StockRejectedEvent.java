package com.cyboul.eda.common.events;

public record StockRejectedEvent(
        String orderId,
        String productId,
        String reason,
        OrderCreatedEvent originalOrder
) {}
