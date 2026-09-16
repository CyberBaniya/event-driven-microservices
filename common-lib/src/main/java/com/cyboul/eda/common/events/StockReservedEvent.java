package com.cyboul.eda.common.events;

public record StockReservedEvent(
        String orderId,
        String productId,
        int quantity,
        OrderCreatedEvent originalOrder
) {}
