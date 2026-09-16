# Local Development

Requires a local MongoDB and Kafka instance, and a system Maven installation (no Maven wrapper).

## Build

```bash
# Build all modules
mvn clean install

# Build a specific service and its dependencies
mvn -pl user-service -am clean install
```

## Run a single service

```bash
mvn -pl user-service spring-boot:run
```

## Run only infrastructure (Kafka + MongoDB)

```bash
docker compose up --build kafka kafka-init mongodb
```

Then start individual services with Maven, pointing them at the local infrastructure.

## MongoDB initialization

On first container startup, the following scripts run automatically from `infra/mongo/docker-entrypoint-initdb.d/`:

1. `01-init-dbs.js` — creates collections
2. `02-init-indexes.js` — creates indexes (unique email on users, etc.)
3. `03-init-data.js` — seeds sample data


## MongoDB check-ups

```bash
docker exec mongodb mongosh --eval \
"db.getSiblingDB('userdb').users.find().pretty()"   
```

```bash
docker exec mongodb mongosh --eval \
"db.getSiblingDB('notifdb').user_projections.find().pretty()"   
```