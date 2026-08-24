# ============================================================================
# DPI Service — Dockerfile
# Build context : racine du monorepo (eHealth/)
# Usage : docker build -f services/java/dpi-service/Dockerfile -t ehealth/dpi-service .
# ============================================================================

# ── Stage 1 : Build Maven ────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
COPY --from=certs ZscalerRootCA.pem /usr/local/share/ca-certificates/ZscalerRootCA.crt
RUN update-ca-certificates
RUN keytool -importcert -trustcacerts -cacerts -storepass changeit -noprompt -alias zscaler -file /usr/local/share/ca-certificates/ZscalerRootCA.crt
WORKDIR /build

# Copier le pom parent + les poms de tous les modules (nécessaire pour le reactor Maven)
COPY pom.xml ./pom.xml
COPY shared/shared-tenant-lib/pom.xml ./shared/shared-tenant-lib/pom.xml
COPY services/java/empi-service/pom.xml ./services/java/empi-service/pom.xml
COPY services/java/gap-service/pom.xml ./services/java/gap-service/pom.xml
COPY services/java/dpi-service/pom.xml ./services/java/dpi-service/pom.xml
COPY services/java/hie-router/pom.xml ./services/java/hie-router/pom.xml
COPY services/java/consent-service/pom.xml ./services/java/consent-service/pom.xml
COPY services/java/terminology-service/pom.xml ./services/java/terminology-service/pom.xml
COPY services/java/lis-service/pom.xml ./services/java/lis-service/pom.xml
COPY services/java/ris-service/pom.xml ./services/java/ris-service/pom.xml
COPY services/java/pharmacy-service/pom.xml ./services/java/pharmacy-service/pom.xml
COPY services/java/rcm-service/pom.xml ./services/java/rcm-service/pom.xml
COPY services/java/hr-service/pom.xml ./services/java/hr-service/pom.xml
COPY services/java/support-service/pom.xml ./services/java/support-service/pom.xml
COPY services/java/patient-portal-service/pom.xml ./services/java/patient-portal-service/pom.xml
COPY services/java/analytics-service/pom.xml ./services/java/analytics-service/pom.xml
COPY services/java/tenant-service/pom.xml ./services/java/tenant-service/pom.xml

# Copier la librairie partagée requise
COPY shared/shared-tenant-lib/src ./shared/shared-tenant-lib/src

RUN mvn -Dhttps.protocols=TLSv1.2 -pl services/java/dpi-service -am dependency:go-offline -B -q

COPY services/java/dpi-service/src ./services/java/dpi-service/src

RUN mvn -Dhttps.protocols=TLSv1.2 -pl services/java/dpi-service -am clean package -DskipTests -B -q

# ── Stage 2 : Runtime JRE ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
COPY --from=certs ZscalerRootCA.pem /usr/local/share/ca-certificates/ZscalerRootCA.crt
RUN update-ca-certificates
RUN keytool -importcert -trustcacerts -cacerts -storepass changeit -noprompt -alias zscaler -file /usr/local/share/ca-certificates/ZscalerRootCA.crt
RUN apk add --no-cache curl

RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
WORKDIR /app

COPY --from=builder /build/services/java/dpi-service/target/dpi-service-0.1.0.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

HEALTHCHECK --interval=20s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

EXPOSE 8083
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
