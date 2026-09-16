package com.cyboul.eda.paymentservice;

import com.cyboul.eda.common.events.PaymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "payments")
public record PaymentRecord(
        @Id String orderId,
        PaymentStatus status,
        Instant processedAt
) {}
