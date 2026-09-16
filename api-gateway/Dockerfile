FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy the multi-module build files first for better layer caching.
COPY pom.xml ./
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY common-lib/pom.xml common-lib/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY notif-service/pom.xml notif-service/pom.xml

# Copy source files required by api-gateway.
COPY api-gateway/src api-gateway/src

# Build only what's needed for api-gateway.
RUN mvn -pl api-gateway -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/api-gateway/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
