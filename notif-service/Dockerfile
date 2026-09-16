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

# Copy source files required by notif-service and shared module.
COPY common-lib/src common-lib/src
COPY notif-service/src notif-service/src

# Build only what's needed for notif-service (and its required modules).
RUN mvn -pl notif-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/notif-service/target/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
