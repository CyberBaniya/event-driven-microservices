# Design Decisions

## Hybrid I/O model

`user-service` uses blocking Spring Web; `product-service` uses reactive Spring WebFlux with a reactive MongoDB driver. New services default to the reactive model for better throughput under high load.

WebFlux uses a non-blocking, event-loop model — threads are not held waiting for I/O. This makes it well-suited for high-volume, read-heavy scenarios like product catalog queries where many concurrent requests are expected.

## Database per service

Each service owns its MongoDB database (`userdb`, `productdb`, `orderdb`, `paymentdb`, `notifdb`). Services never query each other's databases — cross-service data is exchanged through REST APIs or Kafka events.

In development, all databases share a single MongoDB instance for simplicity. In production, each service should have its own instance to eliminate data access bottlenecks — with dedicated write instances and read replicas as services scale horizontally.

## Choreography saga for order processing

Order processing is implemented as a **choreography saga**: no central orchestrator — each service reacts to events and emits the next one.

```
order-created → [Product MS] → stock-reserved  → [Payment MS] → payment-proceed → [Notif MS]
                             → stock-rejected   → [Payment MS] → payment-proceed → [Notif MS]
                                                                               ↓
                                                                          [Product MS]
                                                                      (stock compensation)
```

**Stock reservation** (`ProductService`): on receiving `order-created`, Product MS attempts an atomic `findAndModify` — it decrements stock only if `stock >= quantity`. This avoids a read-then-write race condition. If the update hits 0 modified documents, stock was insufficient and a `StockRejectedEvent` is emitted instead.

**Payment processing** (`PaymentService`): only consumes `stock-reserved` — it is never involved when stock is insufficient. It processes payment and emits `PaymentProceedEvent(status=SUCCESS|FAILED)`.

**Stock rejection short-circuit**: when stock is insufficient, Product MS emits `StockRejectedEvent` directly to `stock-rejected`. Notif MS consumes this and pushes a FAILED notification to the frontend. Payment MS is bypassed entirely, so no payment record is written for orders that never reached payment.

**Compensating transaction**: Product MS also subscribes to `payment-proceed`. If `status=FAILED`, it increments the stock back. Because `payment-proceed` is only ever emitted after a successful stock reservation, no extra flag is needed — any FAILED outcome on that topic means reserved stock must be released.

**Idempotency**: Payment MS guards against duplicate Kafka delivery by checking `paymentRecordRepository.existsById(orderId)` before processing. This prevents double-charging on redelivery.

## Asynchronous event flow

`order-service` publishes an `OrderCreatedEvent` to the `order-created` Kafka topic. From there the event drives the full saga asynchronously — `order-service` returns `200 OK` to the client immediately and the outcome (success/failure) is pushed to the frontend via SSE once the saga completes.

For decisions on event payload shape, PII handling, and notification resilience, see [Event payload & notification trade-offs](event-payload-tradeoffs.md).

## Shared contracts via `common-lib`

DTOs and Kafka event records are defined as Java records in `common-lib` and shared across services:

| Record | Topic | Description |
|---|---|---|
| `OrderCreatedEvent` | `order-created` | Emitted by Order MS when an order is accepted |
| `StockReservedEvent` | `stock-reserved` | Emitted by Product MS on successful stock decrement |
| `StockRejectedEvent` | `stock-rejected` | Emitted by Product MS when stock is insufficient |
| `PaymentProceedEvent` | `payment-proceed` | Emitted by Payment MS with final status (SUCCESS or FAILED) |

Internal domain models (`User`, `Product`) are kept separate from these shared event records.

## API Gateway as single entry point

All external traffic enters through the gateway on port `8080`. Services are not directly exposed in production. Routes are path-prefix based:

| Path | Target |
|---|---|
| `/api/users/**` | user-service :8081 |
| `/api/products/**` | product-service :8082 |
| `/api/orders/**` | order-service :8083 |
