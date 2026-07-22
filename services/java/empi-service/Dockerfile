# ============================================================================
# EMPI Service — Dockerfile
# Build context : racine du monorepo (eHealth/)
# Usage : docker build -f services/java/empi-service/Dockerfile -t ehealth/empi-service .
# ============================================================================

# ── Stage 1 : Build Maven ────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copier le pom parent + le module empi uniquement (cache Maven optimal)
COPY pom.xml ./pom.xml
COPY services/java/empi-service/pom.xml ./services/java/empi-service/pom.xml

# Pré-télécharger les dépendances (layer cacheable)
RUN mvn -pl services/java/empi-service -am dependency:go-offline -B -q

# Copier les sources
COPY services/java/empi-service/src ./services/java/empi-service/src

# Build sans tests (les tests tournent en CI)
RUN mvn -pl services/java/empi-service -am clean package -DskipTests -B -q

# ── Stage 2 : Runtime JRE ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl

# Utilisateur non-root
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
WORKDIR /app

COPY --from=builder /build/services/java/empi-service/target/empi-service-*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

HEALTHCHECK --interval=20s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
