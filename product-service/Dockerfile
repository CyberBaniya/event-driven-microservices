FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy multi-module poms first for better caching.
COPY pom.xml ./
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY common-lib/pom.xml common-lib/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY notif-service/pom.xml notif-service/pom.xml

# Copy required sources for product-service build.
COPY common-lib/src common-lib/src
COPY product-service/src product-service/src

# Build product-service and required modules.
RUN mvn -pl product-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/product-service/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
