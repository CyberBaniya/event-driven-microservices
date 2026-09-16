package com.cyboul.eda.common.events;

public record PaymentProceedEvent(
    String paymentUuid,
    OrderCreatedEvent order,
    PaymentStatus status
){}
