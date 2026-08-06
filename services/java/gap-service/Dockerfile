# ============================================================================
# GAP Service — Dockerfile
# Build context : racine du monorepo (eHealth/)
# Usage : docker build -f services/java/gap-service/Dockerfile -t ehealth/gap-service .
# ============================================================================

# ── Stage 1 : Build Maven ────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
COPY --from=certs ZscalerRootCA.pem /usr/local/share/ca-certificates/ZscalerRootCA.crt
RUN update-ca-certificates
WORKDIR /build

COPY pom.xml ./pom.xml
COPY services/java/gap-service/pom.xml ./services/java/gap-service/pom.xml

RUN mvn -pl services/java/gap-service -am dependency:go-offline -B -q

COPY services/java/gap-service/src ./services/java/gap-service/src

RUN mvn -pl services/java/gap-service -am clean package -DskipTests -B -q

# ── Stage 2 : Runtime JRE ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl ca-certificates
COPY --from=certs ZscalerRootCA.pem /usr/local/share/ca-certificates/ZscalerRootCA.crt
RUN update-ca-certificates

RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
WORKDIR /app

COPY --from=builder /build/services/java/gap-service/target/gap-service-*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

HEALTHCHECK --interval=20s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
