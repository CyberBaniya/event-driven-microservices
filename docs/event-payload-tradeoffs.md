# Event Payload & Notification Trade-offs

## Context

When payment-service processes an order, a downstream notification-service needs to send a confirmation email containing order details and the user's name/email. This raised two questions: what goes in the event, and how does notification-service get PII it doesn't have?

---

## What belongs in a Kafka event

### Thin event (identifiers only)
Only IDs are published. Consumers REST-call upstream services to reconstruct the full picture.

**Pros:** minimal payload, no data duplication, consumers always get fresh data.  
**Cons:** synchronous dependencies in an async flow — if upstream services are down, the consumer is blocked.

### Fat event (full DTOs)
Full domain objects are embedded in the event payload.

**Cons:** event schema becomes a distributed API contract — any DTO change breaks all consumers simultaneously. High-volume topics become expensive to store and process. PII ends up in Kafka logs.

### Snapshot event (chosen approach)
Only the fields needed for the specific use case are embedded — not the full DTO.

```json
{
  "orderId": "...",
  "userId": "...",
  "items": [
    { "productName": "Widget A", "quantity": 2, "unitPrice": 9.99 }
  ],
  "totalAmount": 19.98,
  "currency": "USD",
  "status": "PROCESSED"
}
```

**Pros:** payload stays small, schema is decoupled from domain models, display data is frozen at the moment the event occurred (desirable for order confirmations — reflects what the user actually ordered and paid).  
**Cons:** data is stale by design — acceptable here, not acceptable in contexts where current state matters.

---

## PII in events

User email and name must **not** be included in Kafka events. Kafka retains events long-term and topics may be consumed by multiple services — PII in the event bus creates GDPR exposure around retention, access control, and right-to-erasure.

---

## How notification-service retrieves user contact info

notification-service maintains a **local projection** of user contact info by subscribing to a `user-events` topic. When a user is created or updated, notification-service stores a minimal record (`userId → email, name`) in its own database.

```
user-service  →  [UserCreatedEvent]  →  notification-service
                  {userId, email, name}    stores locally

[payment-processed event]  →  notification-service
 {orderId, userId,              looks up local store
  items snapshot,               → { email, name }
  totalAmount}                  sends email
```

This avoids any synchronous dependency on user-service at notification time — emails are sent even if user-service is down.

**Why not REST + DLQ:** a REST call at consumption time re-introduces synchronous coupling in an async flow. The local projection is the cleaner EDA pattern.

---

## PII on the user-events topic

`UserCreatedEvent` carries PII (email, name). Two mitigation strategies:

- **Short retention (default for now):** set `retention.ms` to 1 day on the `user-events` topic. notification-service consumes the event once and stores it locally; the Kafka log entry expires shortly after. Right-to-erasure = targeted delete in notification-service's own database.
- **Crypto-shredding (full GDPR compliance):** encrypt PII fields per-user with a key stored in a KMS. Deleting the key on erasure request makes the retained Kafka data unreadable.

---

## Decision summary

| Concern | Approach                                                          |
|---|-------------------------------------------------------------------|
| Order/product data in event | Snapshot fields only (no full DTOs)                               |
| User contact info retrieval | Local projection via user-events topic                            |
| PII on user-events topic | Short retention (1 day) — email flows through but expires quickly |
| Right-to-erasure | Delete from notification-service's local store                    |
