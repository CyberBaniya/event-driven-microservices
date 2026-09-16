package com.cyboul.eda.notifservice;

import com.cyboul.eda.common.events.OrderCreatedEvent;
import com.cyboul.eda.common.events.PaymentStatus;

// Normalized payload pushed to the frontend via SSE for any order outcome,
// regardless of whether the failure happened at stock reservation or payment.
public record OrderOutcomeNotification(
        String paymentUuid,
        OrderCreatedEvent order,
        PaymentStatus status
) {}
