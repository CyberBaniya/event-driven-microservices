# Kafka Operations

Consume messages on the ``order-created`` topic

```bash
docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic order-created \
  --from-beginning
```

Inspect the **payment-service consumer group** for ``OrderCreated`` events

```bash
docker exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --group payment-service \
  --describe
```

Consume messages on the ``user-events`` topic

```bash
docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic user-events \
  --from-beginning
```

Inspect the **notif-service consumer group** for ``UserCreated``/``UserUpdated`` events

```bash
docker exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --group notif-service \
  --describe
```

## Topic setup

The `kafka-init` container creates the `order-created`, `payment-proceed` & `user-events` topics automatically on stack startup (1 partition, replication factor 1). No manual setup required.
