# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk

# Virtual display (Xvfb) + VNC server so the JavaFX UI is accessible
RUN apt-get update && apt-get install -y --no-install-recommends \
        xvfb \
        x11vnc \
        libxi6 \
        libxtst6 \
        libxrender1 \
        libgl1 \
        fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/appointment-scheduler-1.0.0.jar app.jar

# VNC on 5900, no password (add -passwd <pw> to x11vnc args if needed)
EXPOSE 5900

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
