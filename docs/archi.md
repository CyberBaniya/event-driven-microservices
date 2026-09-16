# Architecture Overview

Request flow: **Client → Frontend → API Gateway → Services → MongoDB / Kafka**

```
                      User
                       │
                ┌──────▼────────┐
                │   Frontend    │◄─── SSE ────────────────────────┐
                └──────┬────────┘                                 │
                ┌──────▼────────┐                          ┌──────┴──────┐
                │  API Gateway  │ :8080                    │   Notif MS  │ :8085
                └──────┬────────┘                          └──────▲──────┘
     ┌─────────────────┼──────────────────┐                       │
┌────▼────┐     ┌──────▼─────┐    ┌───────▼────┐           ┌──────┴──────┐
│ User MS │     │  Order MS  │    │ Product MS │           │ Payment MS  │
│  :8081  │     │   :8083    │    │   :8082    │           │   :8084     │
└─────────┘     └──────┬─────┘    └─────┬──▲───┘           └──────▲──────┘
                       │                │  │                      │
                       └────────────────┴──┴──── Kafka ───────────┘
```

## Kafka Event Flow

Order processing follows a **choreography saga**. Stock rejection short-circuits directly to Notif MS — Payment MS is never involved unless stock was successfully reserved.

| Step | Producer | Topic | Consumer(s) |
|---|---|---|---|
| 1 | Order MS | `order-created` | Product MS |
| 2a | Product MS | `stock-reserved` | Payment MS |
| 2b | Product MS | `stock-rejected` | Notif MS ← short-circuit |
| 3 | Payment MS | `payment-proceed` | Notif MS, Product MS |

**Step 2**: Product MS attempts an atomic stock decrement. On success it emits `StockReservedEvent` → Payment MS. On insufficient stock it emits `StockRejectedEvent` → Notif MS directly (no payment was attempted).

**Step 3 (compensation)**: Product MS also subscribes to `payment-proceed`. If the payment failed, it increments the stock back — the compensating transaction. Because `payment-proceed` is only emitted after a successful stock reservation, no flag is needed to distinguish the case.

For routing and design rationale see [design-decisions.md](design-decisions.md).

---

# Diagrams

## Level 2 — Container Diagram

```mermaid
C4Container
    Person(user, "User", "End user of the platform")
    System_Boundary(b, "Event-Driven Microservices Platform") {
        Container(apiGateway, "API Gateway", "Spring Cloud Gateway", "Routes external requests")
        Container(orderService, "Order Service", "Spring Boot + Kafka", "Accepts orders, emits OrderCreatedEvent")
        Container(productService, "Product Service", "Spring WebFlux + Kafka", "Stock reservation via atomic MongoDB update")
        Container(paymentService, "Payment Service", "Spring Boot + Kafka", "Payment processing, emits PaymentProceedEvent")
        Container(notifService, "Notification Service", "Spring WebFlux + Kafka", "Real-time notifications via SSE")
        Container(kafka, "Apache Kafka", "Event Streaming", "Async communication")
        ContainerDb(mongo, "MongoDB", "NoSQL Database", "Per-service databases")
    }
    Rel(user, apiGateway, "HTTP")
    Rel(apiGateway, orderService, "/api/orders")
    Rel(orderService, mongo, "orderdb")
    Rel(orderService, kafka, "OrderCreatedEvent → order-created")
    Rel(kafka, productService, "order-created")
    Rel(productService, mongo, "productdb (atomic stock decrement)")
    Rel(productService, kafka, "StockReservedEvent / StockRejectedEvent")
    Rel(kafka, paymentService, "stock-reserved")
    Rel(kafka, notifService, "stock-rejected (short-circuit)")
    Rel(paymentService, mongo, "paymentdb")
    Rel(paymentService, kafka, "PaymentProceedEvent → payment-proceed")
    Rel(kafka, notifService, "payment-proceed")
    Rel(kafka, productService, "payment-proceed (stock compensation)")
    Rel(notifService, mongo, "notifdb (user contacts)")
    Rel(notifService, user, "SSE /notifications/stream")
```

## Level 3 — Component Diagram (Notification Service)

```mermaid
C4Component
    Container(kafka, "Apache Kafka", "Event Streaming", "Message broker")
    ContainerDb(mongo, "MongoDB", "notifdb", "User contact projections")
    Person(user, "User", "End user of the platform")
    Container_Boundary(b, "Notification Service") {
        Component(paymentConsumer, "PaymentProceedEventConsumer", "Spring Kafka", "Consumes PaymentProceedEvent, routes by status")
        Component(userConsumer, "UserEventConsumer", "Spring Kafka", "Keeps local user contact projection in sync")
        Component(sink, "NotificationSinkConfig", "Reactor Sinks.Many", "Reactive bridge between Kafka thread and SSE stream")
        Component(controller, "NotificationController", "Spring WebFlux", "Exposes SSE stream to frontend")
    }
    Rel(kafka, paymentConsumer, "PaymentProceedEvent")
    Rel(kafka, userConsumer, "UserCreatedEvent / UserUpdatedEvent")
    Rel(userConsumer, mongo, "upsert UserContactProjection")
    Rel(paymentConsumer, mongo, "read UserContactProjection")
    Rel(paymentConsumer, sink, "tryEmitNext(NotificationEvent)")
    Rel(sink, controller, "asFlux()")
    Rel(controller, user, "SSE /notifications/stream")
```
