# syntax=docker/dockerfile:1.7

# ── Build stage ──────────────────────────────────────────────────────────────
# Eclipse Temurin 17 + Maven 3.9. Cached layers: first the pom (deps), then src.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests \
    && mv target/*.jar target/app.jar

# ── Runtime stage ────────────────────────────────────────────────────────────
# JRE-only image, non-root user, smaller footprint.
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build --chown=app:app /workspace/target/app.jar app.jar

# Render injects $PORT; application.properties binds server.port=${PORT:8080}.
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
